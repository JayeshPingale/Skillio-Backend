package com.skillio.services.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillio.dto.ConvertLeadAndEnrollResponse;
import com.skillio.dto.CreateEnrollmentFromLeadRequest;
import com.skillio.dto.CreateEnrollmentRequest;
import com.skillio.dto.EnrollmentResponse;
import com.skillio.dto.UpdateEnrollmentRequest;
import com.skillio.entities.Batch;
import com.skillio.entities.Course;
import com.skillio.entities.Enrollment;
import com.skillio.entities.Lead;
import com.skillio.entities.LeadStatusHistory;
import com.skillio.entities.Student;
import com.skillio.entities.StudentFees;
import com.skillio.entities.User;
import com.skillio.exepection.ResourceNotFoundException;
import com.skillio.repositories.BatchRepository;
import com.skillio.repositories.CourseRepository;
import com.skillio.repositories.EnrollmentRepository;
import com.skillio.repositories.LeadRepository;
import com.skillio.repositories.LeadStatusHistoryRepository;
import com.skillio.repositories.StudentRepository;
import com.skillio.repositories.StudentFeesRepository;
import com.skillio.repositories.UserRepository;
import com.skillio.services.AuditLogService;
import com.skillio.services.EnrollmentService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final BatchRepository batchRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final LeadRepository leadRepository;
    private final LeadStatusHistoryRepository leadStatusHistoryRepository;
    private final AuditLogService auditLogService;
    private final HttpServletRequest httpServletRequest;
    private final StudentFeesRepository studentFeesRepository; // ✅ ADD THIS

    	@Override
    	public EnrollmentResponse createEnrollment(CreateEnrollmentRequest request, Long admittedByUserId) {
    	    log.info("Creating enrollment for student ID: {} in batch ID: {}",
    	            request.getStudentId(), request.getBatchId());

    	    // unique student-batch check
    	    if (enrollmentRepository.existsByStudentStudentIdAndBatchBatchId(
    	            request.getStudentId(), request.getBatchId())) {
    	        throw new IllegalStateException(
    	                "Enrollment already exists for this student in the selected batch");
    	    }

    	    Student student = studentRepository.findById(request.getStudentId())
    	            .orElseThrow(() -> new ResourceNotFoundException(
    	                    "Student not found: " + request.getStudentId()));
    	    Batch batch = batchRepository.findById(request.getBatchId())
    	            .orElseThrow(() -> new ResourceNotFoundException(
    	                    "Batch not found: " + request.getBatchId()));
    	    Course course = courseRepository.findById(request.getCourseId())
    	            .orElseThrow(() -> new ResourceNotFoundException(
    	                    "Course not found: " + request.getCourseId()));
    	    User admittedBy = userRepository.findById(admittedByUserId)
    	            .orElseThrow(() -> new ResourceNotFoundException(
    	                    "Admin user not found: " + admittedByUserId));

    	    // Validate discount
    	    validateDiscount(
    	            request.getDiscountPercentage(),
    	            request.getDiscountAmount(),
    	            request.getDiscountReason(),
    	            request.getTotalCourseFees()
    	    );

    	    Enrollment enrollment = new Enrollment();
    	    enrollment.setStudent(student);
    	    enrollment.setBatch(batch);
    	    enrollment.setCourse(course);
    	    enrollment.setEnrollmentDate(request.getEnrollmentDate());
    	    enrollment.setTotalCourseFees(request.getTotalCourseFees());
    	    enrollment.setStatus("ACTIVE");
    	    enrollment.setAdmittedBy(admittedBy);
    	    enrollment.setRemarks(request.getRemarks());

    	    // Set discount fields
    	    enrollment.setDiscountPercentage(request.getDiscountPercentage());
    	    enrollment.setDiscountAmount(request.getDiscountAmount());
    	    enrollment.setDiscountReason(request.getDiscountReason());

    	    Enrollment saved = enrollmentRepository.save(enrollment);

    	    // ✅ Increment batch enrolledCount
    	    batch.setEnrolledCount(batch.getEnrolledCount() + 1);
    	    batchRepository.save(batch);
    	    log.info("Batch {} enrolledCount incremented to {}", batch.getBatchId(), batch.getEnrolledCount());

    	    User performedBy = getLoggedInUser();
    	    auditLogService.createAuditLog(
    	            "Enrollment",
    	            saved.getEnrollmentId(),
    	            "CREATE",
    	            null,
    	            saved,
    	            performedBy
    	    );

    	    log.info("Enrollment created successfully with ID: {}", saved.getEnrollmentId());

    	    return mapToResponse(saved);
    	}


    @Override
    public EnrollmentResponse updateEnrollment(Long enrollmentId, UpdateEnrollmentRequest request) {
        log.info("Updating enrollment with ID: {}", enrollmentId);

        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Enrollment not found: " + enrollmentId));

        Enrollment oldEnrollment = cloneEnrollment(enrollment);

        // Validate discount
        validateDiscount(
                request.getDiscountPercentage(),
                request.getDiscountAmount(),
                request.getDiscountReason(),
                request.getTotalCourseFees()
        );

        enrollment.setTotalCourseFees(request.getTotalCourseFees());
        if (request.getStatus() != null) {
            enrollment.setStatus(request.getStatus());
        }
        enrollment.setRemarks(request.getRemarks());

        // Update discount fields
        enrollment.setDiscountPercentage(request.getDiscountPercentage());
        enrollment.setDiscountAmount(request.getDiscountAmount());
        enrollment.setDiscountReason(request.getDiscountReason());

        Enrollment updated = enrollmentRepository.save(enrollment);

        // ✅ UPDATE: Update StudentFees if exists
        updateStudentFeesIfExists(updated);

        User performedBy = getLoggedInUser();
        auditLogService.createAuditLog(
                "Enrollment",
                enrollmentId,
                "UPDATE",
                oldEnrollment,
                updated,
                performedBy
        );

        log.info("Enrollment updated successfully with ID: {}", enrollmentId);

        return mapToResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public EnrollmentResponse getEnrollmentById(Long enrollmentId) {
        log.info("Fetching enrollment with ID: {}", enrollmentId);
        return enrollmentRepository.findById(enrollmentId)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Enrollment not found: " + enrollmentId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getAllEnrollments() {
        log.info("Fetching all enrollments");
        return enrollmentRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getEnrollmentsByStudent(Long studentId) {
        log.info("Fetching enrollments for student ID: {}", studentId);
        return enrollmentRepository.findByStudentStudentId(studentId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getEnrollmentsByBatch(Long batchId) {
        log.info("Fetching enrollments for batch ID: {}", batchId);
        return enrollmentRepository.findByBatchBatchId(batchId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteEnrollment(Long enrollmentId) {
        log.info("Deleting enrollment with ID: {}", enrollmentId);

        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Enrollment not found: " + enrollmentId));

        Student student = enrollment.getStudent();
        User performedBy = getLoggedInUser();

        // 1) Delete StudentFees if exists
        studentFeesRepository.findByEnrollment(enrollment).ifPresent(fees -> {
            studentFeesRepository.delete(fees);
            log.info("StudentFees deleted for enrollment {}", enrollmentId);
        });

        // 2) Enrollment delete audit
        auditLogService.createAuditLog(
                "Enrollment",
                enrollmentId,
                "DELETE",
                enrollment,
                null,
                performedBy
        );

        // 3) Delete enrollment
        enrollmentRepository.delete(enrollment);
        log.info("Enrollment deleted successfully with ID: {}", enrollmentId);

        // 4) Lead unlink + status revert + history
        Lead lead = leadRepository
                .findByConvertedStudentStudentId(student.getStudentId())
                .orElse(null);

        if (lead != null) {
            String oldStatus = lead.getStatus();

            lead.setConvertedStudent(null);
            lead.setStatus("IN_PROGRESS");
            lead.setConversionDate(null);
            lead = leadRepository.save(lead);

            // lead status history
            LeadStatusHistory history = new LeadStatusHistory();
            history.setLead(lead);
            history.setOldStatus(oldStatus);
            history.setNewStatus("IN_PROGRESS");
            history.setChangedBy(performedBy);
            history.setRemarks("Enrollment deleted, lead unconverted");
            history.setChangedAt(LocalDateTime.now());
            leadStatusHistoryRepository.save(history);

            // lead audit
            auditLogService.createAuditLog(
                    "Lead",
                    lead.getLeadId(),
                    "UNCONVERT",
                    null,
                    lead,
                    performedBy
            );

            log.info("Lead {} unconverted from student {}", lead.getLeadId(), student.getStudentId());
        }

        // 5) Delete student if no other enrollments
        boolean hasOtherEnrollments =
                enrollmentRepository.existsByStudentStudentId(student.getStudentId());

        if (!hasOtherEnrollments) {
            auditLogService.createAuditLog(
                    "Student",
                    student.getStudentId(),
                    "DELETE",
                    student,
                    null,
                    performedBy
            );
            studentRepository.delete(student);
            log.info("Student {} deleted because no more enrollments",
                    student.getStudentId());
        }
    }

    @Override
    public void changeEnrollmentStatus(Long enrollmentId, String status) {
        log.info("Changing enrollment status to {} for ID: {}", status, enrollmentId);

        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Enrollment not found: " + enrollmentId));

        Enrollment oldEnrollment = cloneEnrollment(enrollment);

        enrollment.setStatus(status);
        Enrollment updated = enrollmentRepository.save(enrollment);

        User performedBy = getLoggedInUser();
        auditLogService.createAuditLog(
                "Enrollment",
                enrollmentId,
                "STATUS_CHANGE",
                oldEnrollment,
                updated,
                performedBy
        );

        log.info("Enrollment status changed to: {} for ID: {}", status, enrollmentId);
    }
    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getEnrollmentsByAdmittedUser(Long userId) {
        log.info("Fetching enrollments created by user ID: {}", userId);
        
        // Validate user exists
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with ID: " + userId);
        }
        
        return enrollmentRepository.findByAdmittedByUserIdOrderByEnrollmentDateDesc(userId)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    @Override
    public ConvertLeadAndEnrollResponse convertLeadAndEnroll(
            CreateEnrollmentFromLeadRequest request,
            Long admittedByUserId) {

        log.info("Converting lead {} and creating enrollment in batch {}",
                request.getLeadId(), request.getBatchId());

        Lead lead = leadRepository.findById(request.getLeadId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Lead not found with ID: " + request.getLeadId()));

        String oldStatus = lead.getStatus();
        Lead oldLeadSnapshot = cloneLead(lead);

        Student student;
        if (lead.getConvertedStudent() != null) {
            student = lead.getConvertedStudent();
            log.info("Lead {} already converted to student {}, reusing",
                    lead.getLeadId(), student.getStudentId());
        } else {
            student = new Student();
            student.setFullName(lead.getFullName());
            student.setEmail(lead.getEmail());
            student.setContactNumber(lead.getContactNumber());
            student.setEnrollmentDate(request.getEnrollmentDate());
            student.setStatus("ACTIVE");
            student.setRemarks(lead.getComments());

            student = studentRepository.save(student);

            lead.setConvertedStudent(student);
            lead.setStatus("CONVERTED");
            lead.setConversionDate(request.getEnrollmentDate());
            lead = leadRepository.save(lead);

            log.info("Lead {} converted to student {}", lead.getLeadId(), student.getStudentId());

            User changedBy = getLoggedInUser();

            // LeadStatusHistory
            LeadStatusHistory history = new LeadStatusHistory();
            history.setLead(lead);
            history.setOldStatus(oldStatus);
            history.setNewStatus("CONVERTED");
            history.setChangedBy(changedBy);
            history.setRemarks(
                    (request.getRemarks() != null && !request.getRemarks().isBlank())
                            ? request.getRemarks()
                            : "Converted via enrollment"
            );
            history.setChangedAt(LocalDateTime.now());
            leadStatusHistoryRepository.save(history);

            // Lead audit
            auditLogService.createAuditLog(
                    "Lead",
                    lead.getLeadId(),
                    "CONVERT_AND_ENROLL",
                    oldLeadSnapshot,
                    lead,
                    changedBy
            );
        }

        // Validate discount for lead conversion
        validateDiscount(
                request.getDiscountPercentage(),
                request.getDiscountAmount(),
                request.getDiscountReason(),
                request.getTotalCourseFees()
        );

        CreateEnrollmentRequest enrollmentRequest = new CreateEnrollmentRequest(
                student.getStudentId(),
                request.getBatchId(),
                request.getCourseId(),
                request.getEnrollmentDate(),
                request.getTotalCourseFees(),
                request.getRemarks(),
                request.getDiscountPercentage(),
                request.getDiscountAmount(),
                request.getDiscountReason()
        );

        EnrollmentResponse enrollmentResponse =
                createEnrollment(enrollmentRequest, admittedByUserId);

        // ✅ Get the saved enrollment entity for StudentFees creation
        Enrollment savedEnrollment = enrollmentRepository.findById(enrollmentResponse.getEnrollmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found"));

        // ✅ Auto-create StudentFees record with discounted amount
        createStudentFeesForEnrollment(savedEnrollment);

        return new ConvertLeadAndEnrollResponse(
                enrollmentResponse.getEnrollmentId(),
                student.getStudentId()
        );
    }

    // ==================== DISCOUNT VALIDATION ====================

    private void validateDiscount(Double discountPercentage, Double discountAmount,
                                   String discountReason, BigDecimal totalFees) {
        boolean hasDiscount = (discountPercentage != null && discountPercentage > 0)
                || (discountAmount != null && discountAmount > 0);

        if (!hasDiscount) {
            return;
        }

        if (discountReason == null || discountReason.isBlank()) {
            throw new IllegalArgumentException(
                    "Discount reason is mandatory when discount is applied");
        }

        if (discountPercentage != null && discountPercentage > 0) {
            if (discountPercentage > 50) {
                throw new IllegalArgumentException(
                        "Discount percentage cannot exceed 50%");
            }
        }

        if (discountAmount != null && discountAmount > 0) {
            if (discountAmount > 25000) {
                throw new IllegalArgumentException(
                        "Discount amount cannot exceed ₹25,000");
            }

            if (totalFees != null && discountAmount > totalFees.doubleValue()) {
                throw new IllegalArgumentException(
                        "Discount amount cannot exceed total course fees");
            }
        }

        log.info("Discount validated: percentage={}, amount={}, reason={}",
                discountPercentage, discountAmount, discountReason);
    }

    // ==================== STUDENT FEES AUTO-CREATION ====================

    /**
     * ✅ Automatically creates StudentFees record after enrollment with discounted fees
     */
    private void createStudentFeesForEnrollment(Enrollment enrollment) {
        try {
            // Check if StudentFees already exists
            if (studentFeesRepository.findByEnrollment(enrollment).isPresent()) {
                log.info("StudentFees already exists for enrollment {}", enrollment.getEnrollmentId());
                return;
            }

            // Calculate discounted fees
            BigDecimal discountedFees = calculateDiscountedFees(
                    enrollment.getTotalCourseFees(),
                    enrollment.getDiscountPercentage(),
                    enrollment.getDiscountAmount()
            );

            // Create StudentFees
            StudentFees studentFees = new StudentFees();
            studentFees.setEnrollment(enrollment);
            studentFees.setTotalFees(discountedFees);
            studentFees.setPaidAmount(BigDecimal.ZERO);
            studentFees.setBalanceAmount(discountedFees);

            // Set discount info
            if (enrollment.getDiscountPercentage() != null && enrollment.getDiscountPercentage() > 0) {
                BigDecimal discountAmountValue = enrollment.getTotalCourseFees()
                        .multiply(BigDecimal.valueOf(enrollment.getDiscountPercentage() / 100));
                studentFees.setDiscountAmount(discountAmountValue);
                studentFees.setDiscountReason(enrollment.getDiscountReason());
            } else if (enrollment.getDiscountAmount() != null && enrollment.getDiscountAmount() > 0) {
                studentFees.setDiscountAmount(BigDecimal.valueOf(enrollment.getDiscountAmount()));
                studentFees.setDiscountReason(enrollment.getDiscountReason());
            } else {
                studentFees.setDiscountAmount(BigDecimal.ZERO);
            }

            studentFees.setPaymentStatus("PENDING");

            // Due date = enrollment date + 30 days
            LocalDate dueDate = enrollment.getEnrollmentDate().plusDays(30);
            studentFees.setDueDate(dueDate);

            studentFees.setRemarks("Auto-generated from enrollment with discount applied");

            studentFeesRepository.save(studentFees);

            log.info("StudentFees created automatically for enrollment {} with discounted fees: {}",
                    enrollment.getEnrollmentId(), discountedFees);

        } catch (Exception e) {
            log.error("Error creating StudentFees for enrollment {}: {}",
                    enrollment.getEnrollmentId(), e.getMessage());
            // Don't throw exception, just log error
        }
    }

    /**
     * ✅ Updates StudentFees when enrollment is updated
     */
    private void updateStudentFeesIfExists(Enrollment enrollment) {
        studentFeesRepository.findByEnrollment(enrollment).ifPresent(fees -> {
            BigDecimal discountedFees = calculateDiscountedFees(
                    enrollment.getTotalCourseFees(),
                    enrollment.getDiscountPercentage(),
                    enrollment.getDiscountAmount()
            );

            fees.setTotalFees(discountedFees);
            
            // Update discount info
            if (enrollment.getDiscountPercentage() != null && enrollment.getDiscountPercentage() > 0) {
                BigDecimal discountAmountValue = enrollment.getTotalCourseFees()
                        .multiply(BigDecimal.valueOf(enrollment.getDiscountPercentage() / 100));
                fees.setDiscountAmount(discountAmountValue);
                fees.setDiscountReason(enrollment.getDiscountReason());
            } else if (enrollment.getDiscountAmount() != null && enrollment.getDiscountAmount() > 0) {
                fees.setDiscountAmount(BigDecimal.valueOf(enrollment.getDiscountAmount()));
                fees.setDiscountReason(enrollment.getDiscountReason());
            } else {
                fees.setDiscountAmount(BigDecimal.ZERO);
            }

            // Re-calculate balance
            fees.calculateBalance();
            fees.updatePaymentStatus();

            studentFeesRepository.save(fees);
            log.info("StudentFees updated for enrollment {}", enrollment.getEnrollmentId());
        });
    }

    /**
     * ✅ Calculate final discounted fees
     */
    private BigDecimal calculateDiscountedFees(BigDecimal totalFees,
                                                Double discountPercentage,
                                                Double discountAmount) {
        BigDecimal finalFees = totalFees;

        if (discountPercentage != null && discountPercentage > 0) {
            BigDecimal discount = totalFees.multiply(BigDecimal.valueOf(discountPercentage / 100));
            finalFees = totalFees.subtract(discount);
        } else if (discountAmount != null && discountAmount > 0) {
            finalFees = totalFees.subtract(BigDecimal.valueOf(discountAmount));
        }

        return finalFees.max(BigDecimal.ZERO); // ensure non-negative
    }

    // ==================== HELPER METHODS ====================

    private EnrollmentResponse mapToResponse(Enrollment e) {
        EnrollmentResponse res = new EnrollmentResponse();
        res.setEnrollmentId(e.getEnrollmentId());
        res.setStudentId(e.getStudent().getStudentId());
        res.setStudentCode(e.getStudent().getStudentCode());
        res.setStudentName(e.getStudent().getFullName());
        res.setStudentEmail(e.getStudent().getEmail());
        res.setBatchId(e.getBatch().getBatchId());
        res.setBatchCode(e.getBatch().getBatchCode());
        res.setBatchName(e.getBatch().getBatchName());
        res.setCourseId(e.getCourse().getCourseId());
        res.setCourseName(e.getCourse().getCourseName());
        res.setEnrollmentDate(e.getEnrollmentDate());
        res.setTotalCourseFees(e.getTotalCourseFees());
        res.setStatus(e.getStatus());

        // Discount fields
        res.setDiscountPercentage(e.getDiscountPercentage());
        res.setDiscountAmount(e.getDiscountAmount());
        res.setDiscountReason(e.getDiscountReason());

        res.setAdmittedByUserId(e.getAdmittedBy().getUserId());
        res.setAdmittedByUserName(e.getAdmittedBy().getFullName());
        res.setRemarks(e.getRemarks());
        res.setCreatedAt(e.getCreatedAt());
        res.setUpdatedAt(e.getUpdatedAt());
        return res;
    }

    private Enrollment cloneEnrollment(Enrollment enrollment) {
        Enrollment clone = new Enrollment();
        clone.setEnrollmentId(enrollment.getEnrollmentId());
        clone.setStudent(enrollment.getStudent());
        clone.setBatch(enrollment.getBatch());
        clone.setCourse(enrollment.getCourse());
        clone.setEnrollmentDate(enrollment.getEnrollmentDate());
        clone.setTotalCourseFees(enrollment.getTotalCourseFees());
        clone.setStatus(enrollment.getStatus());
        clone.setAdmittedBy(enrollment.getAdmittedBy());
        clone.setRemarks(enrollment.getRemarks());

        // Clone discount fields
        clone.setDiscountPercentage(enrollment.getDiscountPercentage());
        clone.setDiscountAmount(enrollment.getDiscountAmount());
        clone.setDiscountReason(enrollment.getDiscountReason());

        return clone;
    }

    private Lead cloneLead(Lead lead) {
        Lead clone = new Lead();
        clone.setLeadId(lead.getLeadId());
        clone.setFullName(lead.getFullName());
        clone.setContactNumber(lead.getContactNumber());
        clone.setEmail(lead.getEmail());
        clone.setCourseInterested(lead.getCourseInterested());
        clone.setCollegeName(lead.getCollegeName());
        clone.setQualification(lead.getQualification());
        clone.setExperience(lead.getExperience());
        clone.setStatus(lead.getStatus());
        clone.setInterestLevel(lead.getInterestLevel());
        clone.setAssignedTo(lead.getAssignedTo());
        clone.setLeadSource(lead.getLeadSource());
        clone.setComments(lead.getComments());
        clone.setCreatedDate(lead.getCreatedDate());
        clone.setLastContactDate(lead.getLastContactDate());
        clone.setConversionDate(lead.getConversionDate());
        return clone;
    }

    private User getLoggedInUser() {
        try {
            Authentication authentication =
                    SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                String email = authentication.getName();
                return userRepository.findByEmail(email).orElse(null);
            }
        } catch (Exception e) {
            log.warn("Could not fetch logged-in user from SecurityContext", e);
        }
        return null;
    }
}
