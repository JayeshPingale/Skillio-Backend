package com.skillio.services.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillio.dto.CreatePaymentInstallmentRequest;
import com.skillio.dto.PaymentInstallmentResponse;
import com.skillio.dto.UpdatePaymentInstallmentRequest;
import com.skillio.entities.AuditLog;
import com.skillio.entities.Payment;
import com.skillio.entities.PaymentInstallment;
import com.skillio.entities.Student;
import com.skillio.entities.StudentFees;
import com.skillio.entities.User;
import com.skillio.exepection.ResourceNotFoundException;
import com.skillio.repositories.AuditLogRepository;
import com.skillio.repositories.PaymentInstallmentRepository;
import com.skillio.repositories.PaymentRepository;
import com.skillio.repositories.StudentFeesRepository;
import com.skillio.repositories.UserRepository;
import com.skillio.services.PaymentInstallmentService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentInstallmentServiceImpl implements PaymentInstallmentService {

    private final PaymentInstallmentRepository installmentRepository;
    private final StudentFeesRepository studentFeesRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final HttpServletRequest httpServletRequest;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public List<PaymentInstallmentResponse> createInstallmentPlan(CreatePaymentInstallmentRequest request, Long loggedInUserId) {
        log.info("Creating installment plan for fees ID: {} with {} installments", 
                request.getFeesId(), request.getNumberOfInstallments());

        // Validate StudentFees exists
        StudentFees studentFees = studentFeesRepository.findById(request.getFeesId())
                .orElseThrow(() -> new ResourceNotFoundException("StudentFees not found with ID: " + request.getFeesId()));

        // Check if installments already exist
        List<PaymentInstallment> existingInstallments = installmentRepository.findByStudentFees(studentFees);
        if (!existingInstallments.isEmpty()) {
            throw new IllegalArgumentException("Installment plan already exists for fees ID: " + request.getFeesId());
        }

        // Get balance amount to split into installments
        BigDecimal balanceAmount = studentFees.getBalanceAmount();
        if (balanceAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("No balance amount to create installments");
        }

        User createdBy = userRepository.findById(loggedInUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + loggedInUserId));

        // Calculate installment amount
        BigDecimal installmentAmount = balanceAmount.divide(
                new BigDecimal(request.getNumberOfInstallments()), 
                2, 
                RoundingMode.HALF_UP
        );

        List<PaymentInstallment> installments = new ArrayList<>();
        LocalDate currentDueDate = LocalDate.now().plusDays(30); // First installment due in 30 days

        for (int i = 1; i <= request.getNumberOfInstallments(); i++) {
            PaymentInstallment installment = new PaymentInstallment();
            installment.setStudentFees(studentFees);
            installment.setInstallmentNumber(i);
            installment.setAmount(installmentAmount);
            installment.setDueDate(currentDueDate);
            installment.setStatus("PENDING");
            installment.setRemarks("Auto-generated installment " + i + " of " + request.getNumberOfInstallments());

            PaymentInstallment saved = installmentRepository.save(installment);
            installments.add(saved);

            // Create Audit Log
            createAuditLog("PaymentInstallment", saved.getInstallmentId(), "CREATE", null, saved, createdBy);

            // Next installment due 30 days later
            currentDueDate = currentDueDate.plusDays(30);
        }

        log.info("Created {} installments for fees ID: {}", installments.size(), request.getFeesId());
        return installments.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentInstallmentResponse getInstallmentById(Long installmentId) {
        log.info("Fetching installment with ID: {}", installmentId);
        PaymentInstallment installment = getInstallmentEntityById(installmentId);
        return mapToResponse(installment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentInstallmentResponse> getAllInstallments() {
        log.info("Fetching all installments");
        return installmentRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentInstallmentResponse> getInstallmentsByStudentFeesId(Long feesId) {
        log.info("Fetching installments for fees ID: {}", feesId);
        
        StudentFees studentFees = studentFeesRepository.findById(feesId)
                .orElseThrow(() -> new ResourceNotFoundException("StudentFees not found with ID: " + feesId));

        return installmentRepository.findByStudentFees(studentFees).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentInstallmentResponse> getInstallmentsByStatus(String status) {
        log.info("Fetching installments with status: {}", status);
        return installmentRepository.findByStatus(status).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentInstallmentResponse> getOverdueInstallments() {
        log.info("Fetching overdue installments");
        LocalDate today = LocalDate.now();
        
        return installmentRepository.findByDueDateBeforeAndStatus(today, "PENDING").stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentInstallmentResponse> getPendingInstallments() {
        log.info("Fetching pending installments");
        return installmentRepository.findByStatus("PENDING").stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PaymentInstallmentResponse updateInstallment(Long installmentId, UpdatePaymentInstallmentRequest request, Long loggedInUserId) {
        log.info("Updating installment with ID: {}", installmentId);

        PaymentInstallment installment = getInstallmentEntityById(installmentId);
        
        // Cannot update if already paid
        if ("PAID".equals(installment.getStatus())) {
            throw new IllegalStateException("Cannot update paid installment");
        }

        PaymentInstallment oldInstallment = cloneInstallment(installment);

        User updatedBy = userRepository.findById(loggedInUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + loggedInUserId));

        // Update fields if provided
        if (request.getDueDate() != null) {
            installment.setDueDate(request.getDueDate());
        }

        if (request.getAmount() != null) {
            installment.setAmount(request.getAmount());
        }

        if (request.getRemarks() != null) {
            installment.setRemarks(request.getRemarks());
        }

        PaymentInstallment updated = installmentRepository.save(installment);

        // Create Audit Log
        createAuditLog("PaymentInstallment", installmentId, "UPDATE", oldInstallment, updated, updatedBy);

        log.info("Installment updated successfully with ID: {}", installmentId);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public PaymentInstallmentResponse markAsPaid(Long installmentId, Long paymentId, Long loggedInUserId) {
        log.info("Marking installment {} as PAID with payment ID: {}", installmentId, paymentId);

        PaymentInstallment installment = getInstallmentEntityById(installmentId);
        
        if ("PAID".equals(installment.getStatus())) {
            throw new IllegalStateException("Installment is already marked as PAID");
        }

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with ID: " + paymentId));

        PaymentInstallment oldInstallment = cloneInstallment(installment);

        User updatedBy = userRepository.findById(loggedInUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + loggedInUserId));

        installment.setStatus("PAID");
        installment.setPaidDate(LocalDate.now());
        installment.setPayment(payment);

        PaymentInstallment updated = installmentRepository.save(installment);

        // Create Audit Log
        createAuditLog("PaymentInstallment", installmentId, "MARK_AS_PAID", oldInstallment, updated, updatedBy);

        log.info("Installment {} marked as PAID", installmentId);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public PaymentInstallmentResponse markAsOverdue(Long installmentId, Long loggedInUserId) {
        log.info("Marking installment {} as OVERDUE", installmentId);

        PaymentInstallment installment = getInstallmentEntityById(installmentId);
        
        if ("PAID".equals(installment.getStatus())) {
            throw new IllegalStateException("Cannot mark paid installment as overdue");
        }

        PaymentInstallment oldInstallment = cloneInstallment(installment);

        User updatedBy = userRepository.findById(loggedInUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + loggedInUserId));

        installment.setStatus("OVERDUE");

        PaymentInstallment updated = installmentRepository.save(installment);

        // Create Audit Log
        createAuditLog("PaymentInstallment", installmentId, "MARK_AS_OVERDUE", oldInstallment, updated, updatedBy);

        log.info("Installment {} marked as OVERDUE", installmentId);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteInstallment(Long installmentId, Long loggedInUserId) {
        log.info("Deleting installment with ID: {}", installmentId);

        PaymentInstallment installment = getInstallmentEntityById(installmentId);

        // Cannot delete paid installments
        if ("PAID".equals(installment.getStatus())) {
            throw new IllegalStateException("Cannot delete paid installment");
        }

        User deletedBy = userRepository.findById(loggedInUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + loggedInUserId));

        // Create Audit Log before deletion
        createAuditLog("PaymentInstallment", installmentId, "DELETE", installment, null, deletedBy);

        installmentRepository.delete(installment);
        log.info("Installment deleted successfully with ID: {}", installmentId);
    }

    @Override
    @Transactional
    public void deleteAllInstallmentsForFees(Long feesId, Long loggedInUserId) {
        log.info("Deleting all installments for fees ID: {}", feesId);

        StudentFees studentFees = studentFeesRepository.findById(feesId)
                .orElseThrow(() -> new ResourceNotFoundException("StudentFees not found with ID: " + feesId));

        List<PaymentInstallment> installments = installmentRepository.findByStudentFees(studentFees);

        // Check if any installment is paid
        boolean hasPaidInstallments = installments.stream()
                .anyMatch(inst -> "PAID".equals(inst.getStatus()));

        if (hasPaidInstallments) {
            throw new IllegalStateException("Cannot delete installment plan with paid installments");
        }

        User deletedBy = userRepository.findById(loggedInUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + loggedInUserId));

        // Create audit logs and delete
        installments.forEach(installment -> {
            createAuditLog("PaymentInstallment", installment.getInstallmentId(), "DELETE", installment, null, deletedBy);
            installmentRepository.delete(installment);
        });

        log.info("Deleted {} installments for fees ID: {}", installments.size(), feesId);
    }

    @Override
    public PaymentInstallment getInstallmentEntityById(Long installmentId) {
        return installmentRepository.findById(installmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment installment not found with ID: " + installmentId));
    }

    // ==================== HELPER METHODS ====================

    private PaymentInstallmentResponse mapToResponse(PaymentInstallment installment) {
        PaymentInstallmentResponse response = new PaymentInstallmentResponse();
        response.setInstallmentId(installment.getInstallmentId());

        // Fees Info
        StudentFees fees = installment.getStudentFees();
        response.setFeesId(fees.getFeesId());

        // Student Info
        Student student = fees.getEnrollment().getStudent();
        response.setStudentId(student.getStudentId());
        response.setStudentCode(student.getStudentCode());
//        response.setStudentName(student.getUser().getFullName());

        // Installment Info
        response.setInstallmentNumber(installment.getInstallmentNumber());
        response.setDueDate(installment.getDueDate());
        response.setAmount(installment.getAmount());
        response.setStatus(installment.getStatus());
        response.setPaidDate(installment.getPaidDate());
        response.setPaymentId(installment.getPayment() != null ? installment.getPayment().getPaymentId() : null);
        response.setRemarks(installment.getRemarks());
        response.setCreatedAt(installment.getCreatedAt());
        response.setUpdatedAt(installment.getUpdatedAt());

        return response;
    }

    private PaymentInstallment cloneInstallment(PaymentInstallment installment) {
        PaymentInstallment clone = new PaymentInstallment();
        clone.setInstallmentId(installment.getInstallmentId());
        clone.setStudentFees(installment.getStudentFees());
        clone.setInstallmentNumber(installment.getInstallmentNumber());
        clone.setDueDate(installment.getDueDate());
        clone.setAmount(installment.getAmount());
        clone.setStatus(installment.getStatus());
        clone.setPaidDate(installment.getPaidDate());
        clone.setPayment(installment.getPayment());
        clone.setRemarks(installment.getRemarks());
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
            log.info("Audit log created: {} {} on PaymentInstallment ID: {}", action, entityType, entityId);
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
