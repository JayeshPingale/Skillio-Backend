package com.skillio.services.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillio.dto.CreateStudentFeesRequest;
import com.skillio.dto.StudentFeesResponse;
import com.skillio.dto.UpdateStudentFeesRequest;
import com.skillio.entities.AuditLog;
import com.skillio.entities.Course;
import com.skillio.entities.Enrollment;
import com.skillio.entities.Student;
import com.skillio.entities.StudentFees;
import com.skillio.entities.User;
import com.skillio.exepection.ResourceNotFoundException;
import com.skillio.repositories.AuditLogRepository;
import com.skillio.repositories.EnrollmentRepository;
import com.skillio.repositories.StudentFeesRepository;
import com.skillio.repositories.UserRepository;
import com.skillio.services.StudentFeesService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentFeesServiceImpl implements StudentFeesService {

    private final StudentFeesRepository studentFeesRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final HttpServletRequest httpServletRequest;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public StudentFeesResponse createStudentFees(CreateStudentFeesRequest request, Long loggedInUserId) {
        log.info("Creating student fees for enrollment ID: {}", request.getEnrollmentId());

        // Validate Enrollment exists
        Enrollment enrollment = enrollmentRepository.findById(request.getEnrollmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found with ID: " + request.getEnrollmentId()));

        // Check if fees already exist for this enrollment
        studentFeesRepository.findByEnrollment(enrollment).ifPresent(existingFees -> {
            throw new IllegalArgumentException("Fees already exist for enrollment ID: " + request.getEnrollmentId());
        });

        // Validate discount doesn't exceed total fees
        if (request.getDiscountAmount().compareTo(request.getTotalFees()) > 0) {
            throw new IllegalArgumentException("Discount amount cannot exceed total fees");
        }

        User createdBy = userRepository.findById(loggedInUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + loggedInUserId));

        // Create StudentFees entity
        StudentFees studentFees = new StudentFees();
        studentFees.setEnrollment(enrollment);
        studentFees.setTotalFees(request.getTotalFees());
        studentFees.setDiscountAmount(request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO);
        studentFees.setDiscountReason(request.getDiscountReason());
        studentFees.setDueDate(request.getDueDate());
        studentFees.setRemarks(request.getRemarks());
        studentFees.setPaidAmount(BigDecimal.ZERO);
        
        // Balance and status are auto-calculated in @PrePersist
        StudentFees savedFees = studentFeesRepository.save(studentFees);

        // Create Audit Log
        createAuditLog("StudentFees", savedFees.getFeesId(), "CREATE", null, savedFees, createdBy);

        log.info("Student fees created successfully with ID: {}", savedFees.getFeesId());
        return mapToResponse(savedFees);
    }

    @Override
    @Transactional(readOnly = true)
    public StudentFeesResponse getStudentFeesById(Long feesId) {
        log.info("Fetching student fees with ID: {}", feesId);
        StudentFees studentFees = getStudentFeesEntityById(feesId);
        return mapToResponse(studentFees);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentFeesResponse> getAllStudentFees() {
        log.info("Fetching all student fees");
        return studentFeesRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public StudentFeesResponse getStudentFeesByEnrollmentId(Long enrollmentId) {
        log.info("Fetching student fees for enrollment ID: {}", enrollmentId);
        
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found with ID: " + enrollmentId));

        StudentFees studentFees = studentFeesRepository.findByEnrollment(enrollment)
                .orElseThrow(() -> new ResourceNotFoundException("Student fees not found for enrollment ID: " + enrollmentId));

        return mapToResponse(studentFees);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentFeesResponse> getStudentFeesByPaymentStatus(String paymentStatus) {
        log.info("Fetching student fees with payment status: {}", paymentStatus);
        return studentFeesRepository.findByPaymentStatusOrderByDueDateAsc(paymentStatus).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    @Override
    @Transactional(readOnly = true)
    public List<StudentFeesResponse> getStudentFeesBySalesExecutive(Long salesExecutiveId) {
        log.info("Fetching student fees for Sales Executive ID: {}", salesExecutiveId);
        
        // Get all enrollments admitted by this sales executive
        List<Enrollment> enrollments = enrollmentRepository.findByAdmittedBy_UserId(salesExecutiveId);
        
        if (enrollments.isEmpty()) {
            log.warn("No enrollments found for Sales Executive ID: {}", salesExecutiveId);
            return Collections.emptyList();
        }
        
        // Get fees for these enrollments only
        return enrollments.stream()
            .map(enrollment -> studentFeesRepository.findByEnrollment(enrollment))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }


    @Override
    @Transactional(readOnly = true)
    public List<StudentFeesResponse> getOverdueStudentFees() {
        log.info("Fetching overdue student fees");
        
        List<StudentFees> pendingFees = studentFeesRepository.findByPaymentStatusOrderByDueDateAsc("PENDING");
        List<StudentFees> partialFees = studentFeesRepository.findByPaymentStatusOrderByDueDateAsc("PARTIAL");
        
        LocalDate today = LocalDate.now();
        
        return pendingFees.stream()
                .filter(fees -> fees.getDueDate() != null && fees.getDueDate().isBefore(today))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public StudentFeesResponse updateStudentFees(Long feesId, UpdateStudentFeesRequest request, Long loggedInUserId) {
        log.info("Updating student fees with ID: {}", feesId);

        StudentFees studentFees = getStudentFeesEntityById(feesId);
        StudentFees oldFees = cloneStudentFees(studentFees);

        User updatedBy = userRepository.findById(loggedInUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + loggedInUserId));

        // Update fields if provided
        if (request.getTotalFees() != null) {
            studentFees.setTotalFees(request.getTotalFees());
        }

        // ✅ ADD THIS - Update paidAmount
        if (request.getPaidAmount() != null) {
            // Validate paidAmount doesn't exceed totalFees
            if (request.getPaidAmount().compareTo(studentFees.getTotalFees()) > 0) {
                throw new IllegalArgumentException("Paid amount cannot exceed total fees");
            }
            studentFees.setPaidAmount(request.getPaidAmount());
        }

        if (request.getDiscountAmount() != null) {
            if (request.getDiscountAmount().compareTo(studentFees.getTotalFees()) > 0) {
                throw new IllegalArgumentException("Discount amount cannot exceed total fees");
            }
            studentFees.setDiscountAmount(request.getDiscountAmount());
        }

        if (request.getDiscountReason() != null) {
            studentFees.setDiscountReason(request.getDiscountReason());
        }

        if (request.getDueDate() != null) {
            studentFees.setDueDate(request.getDueDate());
        }

        if (request.getRemarks() != null) {
            studentFees.setRemarks(request.getRemarks());
        }

        // Balance and status are auto-recalculated in @PreUpdate
        StudentFees updatedFees = studentFeesRepository.save(studentFees);

        // Create Audit Log
        createAuditLog("StudentFees", feesId, "UPDATE", oldFees, updatedFees, updatedBy);

        log.info("Student fees updated successfully with ID: {}", feesId);

        return mapToResponse(updatedFees);
    }

    @Override
    @Transactional
    public StudentFeesResponse applyDiscount(Long feesId, BigDecimal discountAmount, String discountReason, Long loggedInUserId) {
        log.info("Applying discount of {} to student fees ID: {}", discountAmount, feesId);

        StudentFees studentFees = getStudentFeesEntityById(feesId);
        StudentFees oldFees = cloneStudentFees(studentFees);

        if (discountAmount.compareTo(studentFees.getTotalFees()) > 0) {
            throw new IllegalArgumentException("Discount amount cannot exceed total fees");
        }

        User updatedBy = userRepository.findById(loggedInUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + loggedInUserId));

        studentFees.setDiscountAmount(discountAmount);
        studentFees.setDiscountReason(discountReason);

        // Balance is auto-recalculated in @PreUpdate
        StudentFees updatedFees = studentFeesRepository.save(studentFees);

        // Create Audit Log
        createAuditLog("StudentFees", feesId, "DISCOUNT_APPLIED", oldFees, updatedFees, updatedBy);

        log.info("Discount applied successfully to student fees ID: {}", feesId);
        return mapToResponse(updatedFees);
    }

    @Override
    @Transactional
    public void deleteStudentFees(Long feesId, Long loggedInUserId) {
        log.info("Deleting student fees with ID: {}", feesId);

        StudentFees studentFees = getStudentFeesEntityById(feesId);

        // Check if any payments exist
        if (studentFees.getPaidAmount().compareTo(BigDecimal.ZERO) > 0) {
            throw new IllegalStateException("Cannot delete student fees with existing payments. Balance: " + studentFees.getBalanceAmount());
        }

        User deletedBy = userRepository.findById(loggedInUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + loggedInUserId));

        // Create Audit Log before deletion
        createAuditLog("StudentFees", feesId, "DELETE", studentFees, null, deletedBy);

        studentFeesRepository.delete(studentFees);
        log.info("Student fees deleted successfully with ID: {}", feesId);
    }

    @Override
    public StudentFees getStudentFeesEntityById(Long feesId) {
        return studentFeesRepository.findById(feesId)
                .orElseThrow(() -> new ResourceNotFoundException("Student fees not found with ID: " + feesId));
    }

    // ==================== HELPER METHODS ====================

    private StudentFeesResponse mapToResponse(StudentFees fees) {
        StudentFeesResponse response = new StudentFeesResponse();
        response.setFeesId(fees.getFeesId());

        // Enrollment Info
        Enrollment enrollment = fees.getEnrollment();
        response.setEnrollmentId(enrollment.getEnrollmentId());

        // ✅ Student Info (from Student entity, not User)
        Student student = enrollment.getStudent();
        response.setStudentId(student.getStudentId());
        response.setStudentCode(student.getStudentCode());
        response.setStudentName(student.getFullName());  // ✅ Direct from Student
        response.setStudentEmail(student.getEmail());    // ✅ Direct from Student

        // ✅ Course Info
        Course course = enrollment.getCourse();
        response.setCourseId(course.getCourseId());
        response.setCourseName(course.getCourseName());

        // ✅ Batch Info (ADD THIS!)
        response.setBatchId(enrollment.getBatch().getBatchId());
        response.setBatchName(enrollment.getBatch().getBatchName());

        // Fees Details
        response.setTotalFees(fees.getTotalFees());
        response.setPaidAmount(fees.getPaidAmount());
        response.setBalanceAmount(fees.getBalanceAmount());
        response.setDiscountAmount(fees.getDiscountAmount());
        response.setDiscountReason(fees.getDiscountReason());
        response.setPaymentStatus(fees.getPaymentStatus());
        response.setDueDate(fees.getDueDate());
        response.setLastPaymentDate(fees.getLastPaymentDate());
        response.setRemarks(fees.getRemarks());
        response.setCreatedAt(fees.getCreatedAt());
        response.setUpdatedAt(fees.getUpdatedAt());

        return response;
    }

    private StudentFees cloneStudentFees(StudentFees fees) {
        StudentFees clone = new StudentFees();
        clone.setFeesId(fees.getFeesId());
        clone.setEnrollment(fees.getEnrollment());
        clone.setTotalFees(fees.getTotalFees());
        clone.setPaidAmount(fees.getPaidAmount());
        clone.setBalanceAmount(fees.getBalanceAmount());
        clone.setDiscountAmount(fees.getDiscountAmount());
        clone.setDiscountReason(fees.getDiscountReason());
        clone.setPaymentStatus(fees.getPaymentStatus());
        clone.setDueDate(fees.getDueDate());
        clone.setLastPaymentDate(fees.getLastPaymentDate());
        clone.setRemarks(fees.getRemarks());
        return clone;
    }

    private void createAuditLog(String entityType, Long entityId, String action,
                                Object oldValue, Object newValue, User performedBy) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setEntityType(entityType);
            auditLog.setEntityId(entityId);
            auditLog.setAction(action);
            auditLog.setOldValue(oldValue != null ? objectMapper.writeValueAsString(oldValue) : null);
            auditLog.setNewValue(newValue != null ? objectMapper.writeValueAsString(newValue) : null);
            auditLog.setPerformedBy(performedBy);
            auditLog.setIpAddress(getClientIp());
            auditLog.setUserAgent(httpServletRequest.getHeader("User-Agent"));
            auditLog.setPerformedAt(LocalDateTime.now());

            auditLogRepository.save(auditLog);
            log.info("Audit log created: {} {} on StudentFees ID: {}", action, entityType, entityId);
        } catch (JsonProcessingException e) {
            log.error("Error creating audit log", e);
        }
    }

    private String getClientIp() {
        String xForwardedFor = httpServletRequest.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0];
        }
        return httpServletRequest.getRemoteAddr();
    }
}
