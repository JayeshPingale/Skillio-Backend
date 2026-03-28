package com.skillio.services.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillio.dto.CommissionPaymentResponse;
import com.skillio.dto.CreateCommissionPaymentRequest;
import com.skillio.dto.UpdateCommissionPaymentRequest;
import com.skillio.entities.AuditLog;
import com.skillio.entities.Commission;
import com.skillio.entities.CommissionPayment;
import com.skillio.entities.User;
import com.skillio.exepection.ResourceNotFoundException;
import com.skillio.repositories.AuditLogRepository;
import com.skillio.repositories.CommissionPaymentRepository;
import com.skillio.repositories.CommissionRepository;
import com.skillio.repositories.UserRepository;
import com.skillio.services.CommissionPaymentService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommissionPaymentServiceImpl implements CommissionPaymentService {

    private final CommissionPaymentRepository commissionPaymentRepository;
    private final CommissionRepository commissionRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final HttpServletRequest httpServletRequest;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public CommissionPaymentResponse payCommission(CreateCommissionPaymentRequest request, Long loggedInUserId) {
        log.info("Processing commission payment for commission ID: {}", request.getCommissionId());

        // Validate Commission exists
        Commission commission = commissionRepository.findById(request.getCommissionId())
                .orElseThrow(() -> new ResourceNotFoundException("Commission not found with ID: " + request.getCommissionId()));

        // Validate commission is ELIGIBLE for payment
        if (!"ELIGIBLE".equals(commission.getStatus())) {
            throw new IllegalStateException("Commission must be ELIGIBLE to make payment. Current status: " + commission.getStatus());
        }

        // Check if commission payment already exists
        commissionPaymentRepository.findByCommission(commission).ifPresent(existingPayment -> {
            throw new IllegalArgumentException("Payment already exists for commission ID: " + request.getCommissionId());
        });

        // Validate payment amount matches eligible amount
        if (request.getAmountPaid().compareTo(commission.getEligibleAmount()) != 0) {
            throw new IllegalArgumentException(
                    String.format("Payment amount ₹%.2f must match eligible amount ₹%.2f", 
                            request.getAmountPaid(), commission.getEligibleAmount()));
        }

        User paidBy = userRepository.findById(loggedInUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + loggedInUserId));

        // Create CommissionPayment entity
        CommissionPayment commissionPayment = new CommissionPayment();
        commissionPayment.setCommission(commission);
        commissionPayment.setAmountPaid(request.getAmountPaid());
        commissionPayment.setPaymentDate(request.getPaymentDate());
        commissionPayment.setPaymentMode(request.getPaymentMode());
        commissionPayment.setTransactionId(request.getTransactionId());
        commissionPayment.setPaidBy(paidBy);
        commissionPayment.setRemarks(request.getRemarks());

        CommissionPayment saved = commissionPaymentRepository.save(commissionPayment);

        // Update Commission status to PAID
        commission.setStatus("PAID");
        commission.setPaidDate(request.getPaymentDate());
        commissionRepository.save(commission);

        // Create Audit Log
        createAuditLog("CommissionPayment", saved.getCommissionPaymentId(), "CREATE", null, saved, paidBy);

        log.info("Commission payment created successfully with ID: {}", saved.getCommissionPaymentId());
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public CommissionPaymentResponse getCommissionPaymentById(Long commissionPaymentId) {
        log.info("Fetching commission payment with ID: {}", commissionPaymentId);
        CommissionPayment payment = getCommissionPaymentEntityById(commissionPaymentId);
        return mapToResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommissionPaymentResponse> getAllCommissionPayments() {
        log.info("Fetching all commission payments");
        return commissionPaymentRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CommissionPaymentResponse getCommissionPaymentByCommissionId(Long commissionId) {
        log.info("Fetching commission payment for commission ID: {}", commissionId);

        Commission commission = commissionRepository.findById(commissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Commission not found with ID: " + commissionId));

        CommissionPayment payment = commissionPaymentRepository.findByCommission(commission)
                .orElseThrow(() -> new ResourceNotFoundException("Commission payment not found for commission ID: " + commissionId));

        return mapToResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommissionPaymentResponse> getCommissionPaymentsPaidBy(Long paidByUserId) {
        log.info("Fetching commission payments paid by user ID: {}", paidByUserId);
        
        // Validate user exists
        userRepository.findById(paidByUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + paidByUserId));

        return commissionPaymentRepository.findByPaidBy_UserId(paidByUserId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommissionPaymentResponse updateCommissionPayment(Long commissionPaymentId, UpdateCommissionPaymentRequest request, Long loggedInUserId) {
        log.info("Updating commission payment with ID: {}", commissionPaymentId);

        CommissionPayment payment = getCommissionPaymentEntityById(commissionPaymentId);
        CommissionPayment oldPayment = cloneCommissionPayment(payment);

        User updatedBy = userRepository.findById(loggedInUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + loggedInUserId));

        // Update fields if provided
        if (request.getPaymentDate() != null) {
            payment.setPaymentDate(request.getPaymentDate());
            
            // Also update commission paidDate
            Commission commission = payment.getCommission();
            commission.setPaidDate(request.getPaymentDate());
            commissionRepository.save(commission);
        }

        if (request.getTransactionId() != null) {
            payment.setTransactionId(request.getTransactionId());
        }

        if (request.getRemarks() != null) {
            payment.setRemarks(request.getRemarks());
        }

        CommissionPayment updated = commissionPaymentRepository.save(payment);

        // Create Audit Log
        createAuditLog("CommissionPayment", commissionPaymentId, "UPDATE", oldPayment, updated, updatedBy);

        log.info("Commission payment updated successfully with ID: {}", commissionPaymentId);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteCommissionPayment(Long commissionPaymentId, Long loggedInUserId) {
        log.info("Deleting commission payment with ID: {}", commissionPaymentId);

        CommissionPayment payment = getCommissionPaymentEntityById(commissionPaymentId);

        User deletedBy = userRepository.findById(loggedInUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + loggedInUserId));

        // Revert Commission status to ELIGIBLE
        Commission commission = payment.getCommission();
        commission.setStatus("ELIGIBLE");
        commission.setPaidDate(null);
        commissionRepository.save(commission);

        // Create Audit Log before deletion
        createAuditLog("CommissionPayment", commissionPaymentId, "DELETE", payment, null, deletedBy);

        commissionPaymentRepository.delete(payment);
        log.info("Commission payment deleted successfully with ID: {}", commissionPaymentId);
    }

    @Override
    public CommissionPayment getCommissionPaymentEntityById(Long commissionPaymentId) {
        return commissionPaymentRepository.findById(commissionPaymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Commission payment not found with ID: " + commissionPaymentId));
    }

    // ==================== HELPER METHODS ====================

    private CommissionPaymentResponse mapToResponse(CommissionPayment payment) {
        CommissionPaymentResponse response = new CommissionPaymentResponse();
        response.setCommissionPaymentId(payment.getCommissionPaymentId());

        // Commission Info
        Commission commission = payment.getCommission();
        response.setCommissionId(commission.getCommissionId());

        // Sales Executive Info
        User salesExecutive = commission.getSalesExecutive();
        response.setSalesExecutiveId(salesExecutive.getUserId());
        response.setSalesExecutiveName(salesExecutive.getFullName());
        response.setSalesExecutiveEmail(salesExecutive.getEmail());

        // Payment Info
        response.setAmountPaid(payment.getAmountPaid());
        response.setPaymentMode(payment.getPaymentMode());
        response.setPaymentDate(payment.getPaymentDate());
        response.setTransactionId(payment.getTransactionId());

        // Admin Info
        User paidBy = payment.getPaidBy();
        response.setPaidByUserId(paidBy.getUserId());
        response.setPaidByUserName(paidBy.getFullName());

        response.setRemarks(payment.getRemarks());
        response.setCreatedAt(payment.getCreatedAt());
        response.setUpdatedAt(payment.getUpdatedAt());

        return response;
    }

    private CommissionPayment cloneCommissionPayment(CommissionPayment payment) {
        CommissionPayment clone = new CommissionPayment();
        clone.setCommissionPaymentId(payment.getCommissionPaymentId());
        clone.setCommission(payment.getCommission());
        clone.setAmountPaid(payment.getAmountPaid());
        clone.setPaymentDate(payment.getPaymentDate());
        clone.setPaymentMode(payment.getPaymentMode());
        clone.setTransactionId(payment.getTransactionId());
        clone.setPaidBy(payment.getPaidBy());
        clone.setRemarks(payment.getRemarks());
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
            log.info("Audit log created: {} {} on CommissionPayment ID: {}", action, entityType, entityId);
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
