package com.skillio.services.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillio.dto.LeadStatusHistoryResponse;
import com.skillio.entities.AuditLog;
import com.skillio.entities.LeadStatusHistory;
import com.skillio.entities.User;
import com.skillio.exepection.ResourceNotFoundException;
import com.skillio.repositories.AuditLogRepository;
import com.skillio.repositories.LeadRepository;
import com.skillio.repositories.LeadStatusHistoryRepository;
import com.skillio.repositories.UserRepository;
import com.skillio.services.LeadStatusHistoryService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class LeadStatusHistoryServiceImpl implements LeadStatusHistoryService {

    private final LeadStatusHistoryRepository leadStatusHistoryRepository;
    private final LeadRepository leadRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final HttpServletRequest httpServletRequest;
    private final ObjectMapper objectMapper;

    @Override
    public List<LeadStatusHistoryResponse> getHistoryByLead(Long leadId) {
        log.info("Fetching status history for lead ID: {}", leadId);

        // Validate lead exists
        if (!leadRepository.existsById(leadId)) {
            throw new ResourceNotFoundException("Lead not found with ID: " + leadId);
        }

        return leadStatusHistoryRepository.findByLeadLeadIdOrderByChangedAtDesc(leadId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<LeadStatusHistoryResponse> getHistoryByUser(Long userId) {
        log.info("Fetching status history for user ID: {}", userId);

        // Validate user exists
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with ID: " + userId);
        }

        return leadStatusHistoryRepository.findByChangedByUserIdOrderByChangedAtDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<LeadStatusHistoryResponse> getAllHistory() {
        log.info("Fetching all lead status history");
        return leadStatusHistoryRepository.findAllByOrderByChangedAtDesc()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ==================== HELPER METHODS ====================

    // Helper method to map LeadStatusHistory entity to LeadStatusHistoryResponse DTO
    private LeadStatusHistoryResponse mapToResponse(LeadStatusHistory history) {
        LeadStatusHistoryResponse response = new LeadStatusHistoryResponse();
        response.setHistoryId(history.getHistoryId());
        response.setLeadId(history.getLead().getLeadId());
        response.setLeadName(history.getLead().getFullName());
        response.setOldStatus(history.getOldStatus());
        response.setNewStatus(history.getNewStatus());
        response.setChangedByUserId(history.getChangedBy().getUserId());
        response.setChangedByUserName(history.getChangedBy().getFullName());
        response.setRemarks(history.getRemarks());
        response.setChangedAt(history.getChangedAt());
        return response;
    }

    /**
     * Get currently logged-in user from Spring Security Context
     */
    private User getLoggedInUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                String email = authentication.getName(); // Email from JWT
                return userRepository.findByEmail(email)
                        .orElse(null);
            }
        } catch (Exception e) {
            log.warn("Could not fetch logged-in user from SecurityContext", e);
        }
        return null;
    }

    /**
     * Create audit log for future DELETE operations (if needed)
     * Currently LeadStatusHistory is read-only, but this method is ready for future use
     */
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
            log.info("Audit log created: {} {} on LeadStatusHistory ID: {}", action, entityType, entityId);
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
