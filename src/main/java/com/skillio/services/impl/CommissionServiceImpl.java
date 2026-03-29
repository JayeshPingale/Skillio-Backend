package com.skillio.services.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillio.dto.ApproveCommissionRequest;
import com.skillio.dto.CommissionResponse;
import com.skillio.dto.CreateCommissionRequest;
import com.skillio.dto.EnrolledStudentCommissionView;
import com.skillio.dto.UpdateCommissionRequest;
import com.skillio.entities.AuditLog;
import com.skillio.entities.Commission;
import com.skillio.entities.CommissionPayment;
import com.skillio.entities.Enrollment;
import com.skillio.entities.Lead;
import com.skillio.entities.Student;
import com.skillio.entities.StudentFees;
import com.skillio.entities.User;
import com.skillio.exepection.ResourceNotFoundException;
import com.skillio.repositories.AuditLogRepository;
import com.skillio.repositories.CommissionPaymentRepository;
import com.skillio.repositories.CommissionRepository;
import com.skillio.repositories.EnrollmentRepository;
import com.skillio.repositories.LeadRepository;
import com.skillio.repositories.StudentFeesRepository;
import com.skillio.repositories.UserRepository;
import com.skillio.services.CommissionService;
import com.skillio.services.NotificationService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommissionServiceImpl implements CommissionService {
    private static final int MAX_COMMISSION_ATTEMPTS = 3;

    private final CommissionRepository commissionRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final HttpServletRequest httpServletRequest;
    private final ObjectMapper objectMapper;
    private final StudentFeesRepository studentFeesRepository;
    private final LeadRepository leadRepository;
    private final CommissionPaymentRepository commissionPaymentRepository;
    private final NotificationService notificationService;


//    @Override
//    @Transactional
//    public CommissionResponse createCommission(CreateCommissionRequest request, Long loggedInUserId) {
//        log.info("Creating commission for enrollment ID: {}", request.getEnrollmentId());
//
//        // Validate Enrollment exists
//        Enrollment enrollment = enrollmentRepository.findById(request.getEnrollmentId())
//                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found with ID: " + request.getEnrollmentId()));
//
//        // Validate Sales Executive exists and has SALES_EXECUTIVE role
//        User salesExecutive = userRepository.findById(request.getSalesExecutiveId())
//                .orElseThrow(() -> new ResourceNotFoundException("Sales Executive not found with ID: " + request.getSalesExecutiveId()));
//
//        // Check if commission already exists for this enrollment
//        List<Commission> existingCommissions = commissionRepository.findByEnrollment(enrollment);
//        if (!existingCommissions.isEmpty()) {
//            throw new IllegalArgumentException("Commission already exists for enrollment ID: " + request.getEnrollmentId());
//        }
//
//        User createdBy = userRepository.findById(loggedInUserId)
//                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + loggedInUserId));
//
//        // Create Commission entity
//        Commission commission = new Commission();
//        commission.setEnrollment(enrollment);
//        commission.setSalesExecutive(salesExecutive);
//        commission.setTotalCourseFees(request.getTotalCourseFees());
//        commission.setCommissionPercentage(request.getCommissionPercentage());
//        commission.setStatus("PENDING");
//        commission.setEligibilityCondition("FULL_PAYMENT_RECEIVED");
//        commission.setRemarks(request.getRemarks());
//
//        // eligibleAmount is auto-calculated in @PrePersist
//        Commission savedCommission = commissionRepository.save(commission);
//
//        // Create Audit Log
//        createAuditLog("Commission", savedCommission.getCommissionId(), "CREATE", null, savedCommission, createdBy);
//
//        log.info("Commission created successfully with ID: {}", savedCommission.getCommissionId());
//        return mapToResponse(savedCommission);
//    }
//    @Override
//    @Transactional
//    public CommissionResponse createCommission(CreateCommissionRequest request, Long loggedInUserId) {
//        log.info("Creating commission for enrollment ID: {}", request.getEnrollmentId());
//        
//        // Validate Enrollment exists
//        Enrollment enrollment = enrollmentRepository.findById(request.getEnrollmentId())
//                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found with ID: " + request.getEnrollmentId()));
//        
//        // Validate Sales Executive exists
//        User salesExecutive = userRepository.findById(request.getSalesExecutiveId())
//                .orElseThrow(() -> new ResourceNotFoundException("Sales Executive not found with ID: " + request.getSalesExecutiveId()));
//        
//        // Check if commission already exists for this enrollment
//        List<Commission> existingCommissions = commissionRepository.findByEnrollment(enrollment);
//        if (!existingCommissions.isEmpty()) {
//            throw new IllegalArgumentException("Commission already exists for enrollment ID: " + request.getEnrollmentId());
//        }
//        
//        User createdBy = userRepository.findById(loggedInUserId)
//                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + loggedInUserId));
//        
//        // ✅ FIX: Get DISCOUNTED fees from StudentFees
//        BigDecimal finalAmount = request.getTotalCourseFees();
//        
//        // Try to fetch StudentFees to get discounted amount
//        Optional<StudentFees> studentFeesOpt = studentFeesRepository.findByEnrollment(enrollment);
//        if (studentFeesOpt.isPresent()) {
//            StudentFees fees = studentFeesOpt.get();
//            // Calculate actual fees after discount
//            BigDecimal discountedFees = fees.getTotalFees().subtract(
//                fees.getDiscountAmount() != null ? fees.getDiscountAmount() : BigDecimal.ZERO
//            );
//            finalAmount = discountedFees;
//            log.info("Using discounted fees: {} (Original: {}, Discount: {})", 
//                discountedFees, fees.getTotalFees(), fees.getDiscountAmount());
//        }
//        
//        // Create Commission entity
//        Commission commission = new Commission();
//        commission.setEnrollment(enrollment);
//        commission.setSalesExecutive(salesExecutive);
//        commission.setTotalCourseFees(finalAmount); // ✅ Set discounted amount
//        commission.setCommissionPercentage(request.getCommissionPercentage());
//        commission.setStatus("PENDING");
//        commission.setEligibilityCondition("FULL_PAYMENT_RECEIVED");
//        commission.setRemarks(request.getRemarks());
//        
//        // eligibleAmount is auto-calculated in @PrePersist
//        Commission savedCommission = commissionRepository.save(commission);
//        
//        // Create Audit Log
//        createAuditLog("Commission", savedCommission.getCommissionId(), "CREATE", null, savedCommission, createdBy);
//        
//        log.info("Commission created successfully with ID: {}", savedCommission.getCommissionId());
//        return mapToResponse(savedCommission);
//    }

    
    @Override
    @Transactional
    public CommissionResponse createCommission(CreateCommissionRequest request, Long loggedInUserId) {
        log.info("🔥 Creating commission for enrollment ID: {}", request.getEnrollmentId());
        
        // Validate Enrollment
        Enrollment enrollment = enrollmentRepository.findById(request.getEnrollmentId())
            .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found with ID: " + request.getEnrollmentId()));
        
        // Validate Sales Executive
        User salesExecutive = userRepository.findById(request.getSalesExecutiveId())
            .orElseThrow(() -> new ResourceNotFoundException("Sales Executive not found with ID: " + request.getSalesExecutiveId()));
        
        // ✅ VALIDATION: Check if this Sales Executive actually handled this student's Lead
        Student student = enrollment.getStudent();
        
        log.info("🔍 Validating Sales Executive for Student ID: {}", student.getStudentId());
        
        // Find the Lead that was converted to this Student
        Optional<Lead> leadOpt = leadRepository.findByConvertedStudent(student);
        
        if (leadOpt.isPresent()) {
            Lead lead = leadOpt.get();
            
            log.info("✅ Found Lead ID: {} for Student ID: {}", lead.getLeadId(), student.getStudentId());
            log.info("🔍 Lead's Sales Executive: {} (ID: {})", 
                lead.getSalesExecutive().getFullName(), 
                lead.getSalesExecutive().getUserId());
            log.info("🔍 Requested Sales Executive: {} (ID: {})", 
                salesExecutive.getFullName(), 
                salesExecutive.getUserId());
            
            // ✅ Compare User IDs
            if (!lead.getSalesExecutive().getUserId().equals(salesExecutive.getUserId())) {
                String errorMsg = String.format(
                    "Commission can only be created for the Sales Executive who handled the original lead. " +
                    "Expected: %s (ID: %d), but got: %s (ID: %d)",
                    lead.getSalesExecutive().getFullName(),
                    lead.getSalesExecutive().getUserId(),
                    salesExecutive.getFullName(),
                    salesExecutive.getUserId()
                );
                log.error("❌ Validation failed: {}", errorMsg);
                throw new IllegalArgumentException(errorMsg);
            }
            
            log.info("✅ Validation passed: Sales Executive {} handled Lead ID: {}", 
                salesExecutive.getFullName(), lead.getLeadId());
                
        } else {
            // If no lead found, this is a manually enrolled student
            log.warn("⚠️ No lead found for Student ID: {}. This is a manually enrolled student. Allowing commission creation.", 
                student.getStudentId());
        }
        
        // Check if commission already exists for this enrollment
        List<Commission> existingCommissions = commissionRepository.findByEnrollment(enrollment);
        if (!existingCommissions.isEmpty()) {
            throw new IllegalArgumentException("Commission already exists for enrollment ID: " + request.getEnrollmentId());
        }
        
        // Get logged-in user
        User createdBy = userRepository.findById(loggedInUserId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + loggedInUserId));
        
        // Use the discounted amount from request
        BigDecimal finalAmount = request.getTotalCourseFees();
        
        log.info("💰 Commission Details - Final Amount: {}, Percentage: {}%", 
            finalAmount, request.getCommissionPercentage());
        
        // Calculate eligible amount
        BigDecimal eligibleAmount = finalAmount
            .multiply(request.getCommissionPercentage())
            .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        
        log.info("💰 Calculated Eligible Amount: {}", eligibleAmount);
        
        // Create Commission
        Commission commission = new Commission();
        commission.setEnrollment(enrollment);
        commission.setSalesExecutive(salesExecutive);
        commission.setTotalCourseFees(finalAmount);
        commission.setCommissionPercentage(request.getCommissionPercentage());
        commission.setEligibleAmount(eligibleAmount);
        commission.setStatus("PENDING");
        commission.setEligibilityCondition("FULL_PAYMENT_RECEIVED");
        commission.setRemarks(request.getRemarks());
        
        Commission savedCommission = commissionRepository.save(commission);
        
        // Create Audit Log
        createAuditLog("Commission", savedCommission.getCommissionId(), "CREATE", 
            null, savedCommission, createdBy);
        
        log.info("✅ Commission created successfully with ID: {} for Sales Executive: {} (Amount: {})", 
            savedCommission.getCommissionId(), 
            salesExecutive.getFullName(),
            eligibleAmount);
        
        return mapToResponse(savedCommission);
    }

 // ✅ ADD: requestCommission() — Sales Exec use karega
    @Override
    @Transactional
    public CommissionResponse requestCommission(CreateCommissionRequest request, Long salesExecutiveId) {
        log.info("📋 Commission request by Sales Executive ID: {}", salesExecutiveId);

        Enrollment enrollment = enrollmentRepository.findById(request.getEnrollmentId())
            .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found"));

        User salesExecutive = userRepository.findById(salesExecutiveId)
            .orElseThrow(() -> new ResourceNotFoundException("Sales Executive not found"));

        // ✅ Validate: Is this enrollment linked to this Sales Exec?
        Optional<Lead> leadOpt = leadRepository.findByConvertedStudent(enrollment.getStudent());
        if (leadOpt.isPresent()) {
            Lead lead = leadOpt.get();
            if (lead.getSalesExecutive() == null || !lead.getSalesExecutive().getUserId().equals(salesExecutiveId)) {
                throw new IllegalArgumentException("You can only request commission for your own students");
            }
        }

        // ✅ Check duplicate — only ACTIVE requests block karo
        List<Commission> existing = commissionRepository.findByEnrollmentOrderByCreatedAtDesc(enrollment);

        boolean hasActiveRequest = existing.stream()
            .anyMatch(c -> isBlockingCommissionStatus(c.getStatus()));

        if (hasActiveRequest) {
            throw new IllegalArgumentException(
                "Commission request already exists for this enrollment"
            );
        }

        // ✅ 3 attempts check — REJECTED wale count karo
        long attemptsUsed = existing.stream()
            .filter(c -> countsAsAttempt(c.getStatus()))
            .count();

        if (attemptsUsed >= MAX_COMMISSION_ATTEMPTS) {
            throw new IllegalArgumentException(
                "Maximum " + MAX_COMMISSION_ATTEMPTS + " commission requests allowed per enrollment. All attempts have been used."
            );
        }

        // ✅ Get fees from StudentFees
        BigDecimal finalAmount = BigDecimal.ZERO;
        Optional<StudentFees> feesOpt = studentFeesRepository.findByEnrollment(enrollment);
        if (feesOpt.isPresent()) {
            StudentFees fees = feesOpt.get();
            finalAmount = fees.getTotalFees()
                .subtract(fees.getDiscountAmount() != null ? fees.getDiscountAmount() : BigDecimal.ZERO);
        }

        BigDecimal commissionPct = request.getCommissionPercentage() != null
            ? request.getCommissionPercentage()
            : new BigDecimal("10.00");

        BigDecimal eligibleAmount = finalAmount
            .multiply(commissionPct)
            .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

        // ✅ Zero amount check
        if (finalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException(
                "Cannot request commission: Student fees after discount is Rs.0 for enrollment ID: "
                + enrollment.getEnrollmentId()
                + ". Please check student fees and discount amount."
            );
        }

        Commission commission = new Commission();
        commission.setEnrollment(enrollment);
        commission.setSalesExecutive(salesExecutive);
        commission.setTotalCourseFees(finalAmount);
        commission.setCommissionPercentage(commissionPct);
        commission.setEligibleAmount(eligibleAmount);
        commission.setStatus("PENDING_APPROVAL");
        commission.setRequestedRemarks(request.getRemarks());
        commission.setEligibilityCondition("FULL_PAYMENT_RECEIVED");

        Commission saved = commissionRepository.save(commission);
        createAuditLog("Commission", saved.getCommissionId(), "REQUEST", null, saved, salesExecutive);

        // ✅ Attempt number log karo
        long currentAttemptNumber = attemptsUsed + 1;
        log.info("Commission attempt {} of {} for enrollment ID: {}",
            currentAttemptNumber, MAX_COMMISSION_ATTEMPTS, enrollment.getEnrollmentId());

        // ✅ NOTIFY all Admins
        List<User> admins = userRepository.findByRoleRoleName("ROLE_ADMIN");
        for (User admin : admins) {
            notificationService.createNotification(
                admin,
                "New Commission Request",
                salesExecutive.getFullName() + " has requested a commission for student: "
                    + enrollment.getStudent().getFullName()
                    + " (Rs." + saved.getEligibleAmount() + ")"
                    + (attemptsUsed > 0 ? " [Attempt " + currentAttemptNumber + " of " + MAX_COMMISSION_ATTEMPTS + "]" : ""),
                "COMMISSION_REQUEST",
                saved.getCommissionId(),
                "COMMISSION",
                "HIGH"
            );
        }

        log.info("✅ Commission requested by Sales Exec {}, notified admins", salesExecutiveId);
        return mapToResponse(saved);
    }
    // ✅ ADD: approveOrRejectCommission() — Admin use karega
    @Override
    @Transactional
    public CommissionResponse approveOrRejectCommission(ApproveCommissionRequest request, Long adminId) {
        Commission commission = getCommissionEntityById(request.getCommissionId());
        Commission oldCommission = cloneCommission(commission);

        if (!"PENDING_APPROVAL".equals(commission.getStatus())) {
            throw new IllegalStateException("Only PENDING_APPROVAL commissions can be actioned");
        }

        User admin = userRepository.findById(adminId)
            .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));

        commission.setAdminComments(request.getComments());

        if (request.getApproved()) {
            // ✅ APPROVE
            commission.setStatus("APPROVED");
            commission.setEligibilityDate(LocalDate.now());
            commissionRepository.save(commission);

            // ✅ AUTO: Create CommissionPayment if amount provided
            if (request.getAmountPaid() != null) {
                CommissionPayment payment = new CommissionPayment();
                payment.setCommission(commission);
                payment.setAmountPaid(request.getAmountPaid());
                payment.setPaymentDate(LocalDate.now());
                payment.setPaymentMode(request.getPaymentMode());
                payment.setTransactionId(request.getTransactionId());
                payment.setPaidBy(admin);
                payment.setRemarks(request.getComments());
                CommissionPayment savedPayment = commissionPaymentRepository.save(payment);
                createAuditLog(
                    "CommissionPayment",
                    savedPayment.getCommissionPaymentId(),
                    "CREATE",
                    null,
                    createSafeCommissionPaymentAuditData(savedPayment),
                    admin
                );

                commission.setStatus("PAID");
                commission.setPaidDate(LocalDate.now());
                commissionRepository.save(commission);
            }

            // ✅ NOTIFY Sales Executive — Approved
            notificationService.createNotification(
                commission.getSalesExecutive(),
                "Commission Approved!",
                "Admin has approved your commission request for student: "
                    + commission.getEnrollment().getStudent().getFullName()
                    + " | Amount: ₹" + commission.getEligibleAmount()
                    + " | Comment: " + request.getComments(),
                "COMMISSION_APPROVED",
                commission.getCommissionId(),
                "COMMISSION",
                "HIGH"
            );
        } else {
            // ✅ REJECT
            commission.setStatus("REJECTED");
            commissionRepository.save(commission);

            // ✅ NOTIFY Sales Executive — Rejected
            notificationService.createNotification(
                commission.getSalesExecutive(),
                "❌ Commission Rejected",
                "Admin has rejected your commission request for student: "
                    + commission.getEnrollment().getStudent().getFullName()
                    + " | Reason: " + request.getComments(),
                "COMMISSION_REJECTED",
                commission.getCommissionId(),
                "COMMISSION",
                "HIGH"
            );
        }

        createAuditLog("Commission", commission.getCommissionId(),
            request.getApproved() ? "APPROVE" : "REJECT", oldCommission, commission, admin);

        return mapToResponse(commission);
    }

    @Override
    @Transactional(readOnly = true)
    public CommissionResponse getCommissionById(Long commissionId) {
        log.info("Fetching commission with ID: {}", commissionId);
        Commission commission = getCommissionEntityById(commissionId);
        return mapToResponse(commission);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommissionResponse> getAllCommissions() {
        log.info("Fetching all commissions");
        return commissionRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommissionResponse> getCommissionsBySalesExecutive(Long salesExecutiveId) {
        log.info("Fetching commissions for sales executive ID: {}", salesExecutiveId);
        
        User salesExecutive = userRepository.findById(salesExecutiveId)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Executive not found with ID: " + salesExecutiveId));

        return commissionRepository.findBySalesExecutive(salesExecutive).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommissionResponse> getCommissionsBySalesExecutiveAndStatus(Long salesExecutiveId, String status) {
        log.info("Fetching commissions for sales executive ID: {} with status: {}", salesExecutiveId, status);
        
        User salesExecutive = userRepository.findById(salesExecutiveId)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Executive not found with ID: " + salesExecutiveId));

        return commissionRepository.findBySalesExecutiveAndStatus(salesExecutive, status).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommissionResponse> getCommissionsByStatus(String status) {
        log.info("Fetching commissions with status: {}", status);
        return commissionRepository.findByStatus(status).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommissionResponse> getCommissionsByEnrollment(Long enrollmentId) {
        log.info("Fetching commissions for enrollment ID: {}", enrollmentId);
        
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found with ID: " + enrollmentId));

        return commissionRepository.findByEnrollment(enrollment).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalEligibleAmountBySalesExecutive(Long salesExecutiveId) {
        log.info("Calculating total eligible commission for sales executive ID: {}", salesExecutiveId);
        
        User salesExecutive = userRepository.findById(salesExecutiveId)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Executive not found with ID: " + salesExecutiveId));

        BigDecimal total = commissionRepository.getTotalEligibleAmount(salesExecutive);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalPaidCommissionBySalesExecutive(Long salesExecutiveId) {
        log.info("Calculating total paid commission for sales executive ID: {}", salesExecutiveId);
        
        User salesExecutive = userRepository.findById(salesExecutiveId)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Executive not found with ID: " + salesExecutiveId));

        BigDecimal total = commissionRepository.getTotalPaidCommission(salesExecutive);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Override
    @Transactional(readOnly = true)
    public Long getCommissionCountBySalesExecutiveAndStatus(Long salesExecutiveId, String status) {
        log.info("Counting commissions for sales executive ID: {} with status: {}", salesExecutiveId, status);
        
        User salesExecutive = userRepository.findById(salesExecutiveId)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Executive not found with ID: " + salesExecutiveId));

        return commissionRepository.countBySalesExecutiveAndStatus(salesExecutive, status);
    }

    @Override
    @Transactional
    public CommissionResponse updateCommission(Long commissionId, UpdateCommissionRequest request, Long loggedInUserId) {
        log.info("Updating commission with ID: {}", commissionId);

        Commission commission = getCommissionEntityById(commissionId);
        
        // Cannot update if already paid
        if ("PAID".equals(commission.getStatus())) {
            throw new IllegalStateException("Cannot update paid commission");
        }

        Commission oldCommission = cloneCommission(commission);

        User updatedBy = userRepository.findById(loggedInUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + loggedInUserId));

        // Update fields if provided
        if (request.getTotalCourseFees() != null) {
            commission.setTotalCourseFees(request.getTotalCourseFees());
        }

        if (request.getCommissionPercentage() != null) {
            commission.setCommissionPercentage(request.getCommissionPercentage());
        }

        if (request.getRemarks() != null) {
            commission.setRemarks(request.getRemarks());
        }

        // Recalculate eligible amount
        commission.calculateEligibleAmount();

        Commission updated = commissionRepository.save(commission);

        // Create Audit Log
        createAuditLog("Commission", commissionId, "UPDATE", oldCommission, updated, updatedBy);

        log.info("Commission updated successfully with ID: {}", commissionId);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public CommissionResponse markAsEligible(Long commissionId, Long loggedInUserId) {
        log.info("Marking commission {} as ELIGIBLE", commissionId);

        Commission commission = getCommissionEntityById(commissionId);
        
        if ("PAID".equals(commission.getStatus())) {
            throw new IllegalStateException("Commission is already PAID");
        }

        if ("ELIGIBLE".equals(commission.getStatus())) {
            throw new IllegalStateException("Commission is already ELIGIBLE");
        }

        Commission oldCommission = cloneCommission(commission);

        User updatedBy = userRepository.findById(loggedInUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + loggedInUserId));

        commission.setStatus("ELIGIBLE");
        commission.setEligibilityDate(LocalDate.now());

        Commission updated = commissionRepository.save(commission);

        // Create Audit Log
        createAuditLog("Commission", commissionId, "MARK_AS_ELIGIBLE", oldCommission, updated, updatedBy);

        log.info("Commission {} marked as ELIGIBLE", commissionId);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public CommissionResponse markAsPaid(Long commissionId, Long loggedInUserId) {
        log.info("Marking commission {} as PAID", commissionId);

        Commission commission = getCommissionEntityById(commissionId);
        
        if ("PAID".equals(commission.getStatus())) {
            throw new IllegalStateException("Commission is already PAID");
        }

        if (!"ELIGIBLE".equals(commission.getStatus())) {
            throw new IllegalStateException("Commission must be ELIGIBLE before marking as PAID");
        }

        Commission oldCommission = cloneCommission(commission);

        User updatedBy = userRepository.findById(loggedInUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + loggedInUserId));

        commission.setStatus("PAID");
        commission.setPaidDate(LocalDate.now());

        Commission updated = commissionRepository.save(commission);

        // Create Audit Log
        createAuditLog("Commission", commissionId, "MARK_AS_PAID", oldCommission, updated, updatedBy);

        log.info("Commission {} marked as PAID", commissionId);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteCommission(Long commissionId, Long loggedInUserId) {
        log.info("Deleting commission with ID: {}", commissionId);

        Commission commission = getCommissionEntityById(commissionId);

        // Cannot delete paid commissions
        if ("PAID".equals(commission.getStatus())) {
            throw new IllegalStateException("Cannot delete paid commission");
        }

        User deletedBy = userRepository.findById(loggedInUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + loggedInUserId));

        Commission oldCommission = cloneCommission(commission);
        commission.setStatus("CANCELLED");
        commission.setAdminComments("Cancelled by admin");
        commissionRepository.save(commission);

        createAuditLog("Commission", commissionId, "CANCEL", oldCommission, commission, deletedBy);
        log.info("Commission marked as CANCELLED successfully with ID: {}", commissionId);
    }
 // CommissionServiceImpl.java mein ADD karo — requestCommission() ke baad
    

    @Override
    @Transactional(readOnly = true)
    public List<EnrolledStudentCommissionView> getEnrolledStudentsForCommission(Long salesExecutiveId) {
        log.info("📋 Loading enrolled students for Sales Executive ID: {}", salesExecutiveId);

        User salesExecutive = userRepository.findById(salesExecutiveId)
            .orElseThrow(() -> new ResourceNotFoundException("Sales Executive not found"));

        // Sales Exec ke jo leads CONVERTED hue, unke students ka enrollment fetch karo
        List<Lead> convertedLeads = leadRepository.findBySalesExecutiveAndStatus(salesExecutive, "CONVERTED");

        List<EnrolledStudentCommissionView> result = new java.util.ArrayList<>();

        for (Lead lead : convertedLeads) {
            if (lead.getConvertedStudent() == null) continue;

            Student student = lead.getConvertedStudent();

            // Student ke enrollments
            List<Enrollment> enrollments = enrollmentRepository.findByStudent(student);

            for (Enrollment enrollment : enrollments) {
                if (!"ACTIVE".equals(enrollment.getStatus())) continue;

                // Fees info
                BigDecimal totalFee = BigDecimal.ZERO;
                BigDecimal paid = BigDecimal.ZERO;
                BigDecimal pending = BigDecimal.ZERO;

                Optional<StudentFees> feesOpt = studentFeesRepository.findByEnrollment(enrollment);
                if (feesOpt.isPresent()) {
                    StudentFees fees = feesOpt.get();
                    totalFee = fees.getTotalFees() != null ? fees.getTotalFees() : BigDecimal.ZERO;
                    paid = fees.getPaidAmount() != null ? fees.getPaidAmount() : BigDecimal.ZERO;
                    pending = fees.getBalanceAmount() != null ? fees.getBalanceAmount() : BigDecimal.ZERO;
                }

                // Commission info (if exists)
                Long commissionId = null;
                String commissionStatus = null;
                BigDecimal eligibleAmount = null;
                String requestedRemarks = null;
                String adminComments = null;

                Integer attemptsUsed = 0;
                Integer attemptsRemaining = MAX_COMMISSION_ATTEMPTS;
                Boolean canRequestCommission = Boolean.TRUE;
                String lastAttemptStatus = null;

                List<Commission> commissions = commissionRepository.findByEnrollmentOrderByCreatedAtDesc(enrollment);
                if (!commissions.isEmpty()) {
                    attemptsUsed = Math.toIntExact(
                        commissions.stream()
                            .filter(c -> countsAsAttempt(c.getStatus()))
                            .count()
                    );
                    attemptsRemaining = Math.max(0, MAX_COMMISSION_ATTEMPTS - attemptsUsed);

                    Commission latestAttempt = commissions.get(0);
                    lastAttemptStatus = latestAttempt.getStatus();
                    requestedRemarks = latestAttempt.getRequestedRemarks();
                    adminComments = latestAttempt.getAdminComments();

                    Optional<Commission> blockingCommission = commissions.stream()
                        .filter(c -> isBlockingCommissionStatus(c.getStatus()))
                        .findFirst();

                    if (blockingCommission.isPresent()) {
                        Commission c = blockingCommission.get();
                        commissionId = c.getCommissionId();
                        commissionStatus = c.getStatus();
                        eligibleAmount = c.getEligibleAmount();
                        requestedRemarks = c.getRequestedRemarks();
                        adminComments = c.getAdminComments();
                        canRequestCommission = Boolean.FALSE;
                    } else {
                        canRequestCommission = attemptsRemaining > 0;
                    }
                }

                EnrolledStudentCommissionView view = EnrolledStudentCommissionView.builder()
                    .enrollmentId(enrollment.getEnrollmentId())
                    .studentId(student.getStudentId())
                    .studentName(student.getFullName())
                    .studentCode(student.getStudentCode())
                    .courseName(enrollment.getCourse().getCourseName())
                    .enrollmentDate(enrollment.getEnrollmentDate() != null
                        ? enrollment.getEnrollmentDate().toString() : "")
                    .totalCourseFee(totalFee)
                    .totalFeesPaid(paid)
                    .totalFeesPending(pending)
                    .commissionId(commissionId)
                    .commissionStatus(commissionStatus)
                    .eligibleAmount(eligibleAmount)
                    .requestedRemarks(requestedRemarks)
                    .adminComments(adminComments)
                    .commissionAttemptsUsed(attemptsUsed)
                    .commissionAttemptsRemaining(attemptsRemaining)
                    .canRequestCommission(canRequestCommission)
                    .lastAttemptStatus(lastAttemptStatus)
                    .build();

                result.add(view);
            }
        }

        log.info("✅ Found {} enrolled students for Sales Executive {}", result.size(), salesExecutiveId);
        return result;
    }


    @Override
    public Commission getCommissionEntityById(Long commissionId) {
        return commissionRepository.findById(commissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Commission not found with ID: " + commissionId));
    }

    // ==================== HELPER METHODS ====================

    private CommissionResponse mapToResponse(Commission commission) {
        CommissionResponse response = new CommissionResponse();
        response.setCommissionId(commission.getCommissionId());

        // Enrollment Info
        Enrollment enrollment = commission.getEnrollment();
        response.setEnrollmentId(enrollment.getEnrollmentId());

        Student student = enrollment.getStudent();
        response.setStudentId(student.getStudentId());
        response.setStudentCode(student.getStudentCode());
        response.setStudentName(student.getFullName());

        // Course Info
        response.setCourseName(enrollment.getCourse().getCourseName());

        // Sales Executive Info
        User salesExecutive = commission.getSalesExecutive();
        response.setSalesExecutiveId(salesExecutive.getUserId());
        response.setSalesExecutiveName(salesExecutive.getFullName());

        // Commission Details
        response.setTotalCourseFees(commission.getTotalCourseFees());
        response.setCommissionPercentage(commission.getCommissionPercentage());
        response.setEligibleAmount(commission.getEligibleAmount());
        response.setStatus(commission.getStatus());
        response.setEligibilityCondition(commission.getEligibilityCondition());
        response.setEligibilityDate(commission.getEligibilityDate());
        response.setPaidDate(commission.getPaidDate());
        response.setRemarks(commission.getRemarks());
        response.setCreatedAt(commission.getCreatedAt());
        response.setUpdatedAt(commission.getUpdatedAt());
        response.setAdminComments(commission.getAdminComments());
        response.setRequestedRemarks(commission.getRequestedRemarks());

        return response;
    }

    private Commission cloneCommission(Commission commission) {
        Commission clone = new Commission();
        clone.setCommissionId(commission.getCommissionId());
        clone.setEnrollment(commission.getEnrollment());
        clone.setSalesExecutive(commission.getSalesExecutive());
        clone.setTotalCourseFees(commission.getTotalCourseFees());
        clone.setCommissionPercentage(commission.getCommissionPercentage());
        clone.setEligibleAmount(commission.getEligibleAmount());
        clone.setStatus(commission.getStatus());
        clone.setEligibilityCondition(commission.getEligibilityCondition());
        clone.setEligibilityDate(commission.getEligibilityDate());
        clone.setPaidDate(commission.getPaidDate());
        clone.setRemarks(commission.getRemarks());
        return clone;
    }

    private java.util.Map<String, Object> createSafeCommissionPaymentAuditData(CommissionPayment payment) {
        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("commissionPaymentId", payment.getCommissionPaymentId());
        data.put("commissionId", payment.getCommission().getCommissionId());
        data.put("salesExecutiveId", payment.getCommission().getSalesExecutive().getUserId());
        data.put("amountPaid", payment.getAmountPaid());
        data.put("paymentDate", payment.getPaymentDate());
        data.put("paymentMode", payment.getPaymentMode());
        data.put("paidByUserId", payment.getPaidBy().getUserId());
        data.put("remarks", payment.getRemarks());
        return data;
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
            log.info("Audit log created: {} {} on Commission ID: {}", action, entityType, entityId);
        } catch (JsonProcessingException e) {
            log.error("Error creating audit log", e);
        }
    }

    @Override
    @Transactional
    public CommissionResponse approveCommission(ApproveCommissionRequest request, Long loggedInUserId) {
        log.info("Approving commission with ID: {}", request.getCommissionId());

        Commission commission = getCommissionEntityById(request.getCommissionId());
        
        // Validate current status
        if ("PAID".equals(commission.getStatus())) {
            throw new IllegalStateException("Cannot approve commission that is already PAID");
        }

        if ("ELIGIBLE".equals(commission.getStatus())) {
            throw new IllegalStateException("Commission is already ELIGIBLE");
        }

        // Only PENDING commissions can be approved
        if (!"PENDING".equals(commission.getStatus())) {
            throw new IllegalStateException("Only PENDING commissions can be approved. Current status: " + commission.getStatus());
        }

        Commission oldCommission = cloneCommission(commission);

        User approvedBy = userRepository.findById(loggedInUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + loggedInUserId));

        // Update status to ELIGIBLE
        commission.setStatus("ELIGIBLE");
        commission.setEligibilityDate(LocalDate.now());
        
        Commission updated = commissionRepository.save(commission);

        // Create Audit Log
        createAuditLog("Commission", request.getCommissionId(), "APPROVE", oldCommission, updated, approvedBy);

        log.info("Commission {} approved and marked as ELIGIBLE by user {}", request.getCommissionId(), loggedInUserId);
        return mapToResponse(updated);
    }

    private String getClientIp() {
        String xForwardedFor = httpServletRequest.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0];
        }
        return httpServletRequest.getRemoteAddr();
    }

    private boolean isBlockingCommissionStatus(String status) {
        return "PENDING_APPROVAL".equals(status)
            || "APPROVED".equals(status)
            || "PAID".equals(status)
            || "ELIGIBLE".equals(status)
            || "PENDING".equals(status);
    }

    private boolean countsAsAttempt(String status) {
        return isBlockingCommissionStatus(status)
            || "REJECTED".equals(status)
            || "CANCELLED".equals(status);
    }
}
