package com.skillio.services.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillio.dto.FollowUpRequest;
import com.skillio.dto.FollowUpResponse;
import com.skillio.dto.UpdateFollowUpRequest;
import com.skillio.entities.AuditLog;
import com.skillio.entities.FollowUp;
import com.skillio.entities.Lead;
import com.skillio.entities.User;
import com.skillio.exepection.ResourceNotFoundException;
import com.skillio.repositories.AuditLogRepository;
import com.skillio.repositories.FollowUpRepository;
import com.skillio.repositories.LeadRepository;
import com.skillio.repositories.UserRepository;
import com.skillio.services.FollowUpService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class FollowUpServiceImpl implements FollowUpService {

    private final FollowUpRepository followUpRepository;
    private final LeadRepository leadRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final HttpServletRequest httpServletRequest;
    private final ObjectMapper objectMapper;

    @Override
    public FollowUpResponse createFollowUp(FollowUpRequest request, Long userId) {
        log.info("Creating follow-up for lead ID: {} by user ID: {}", request.getLeadId(), userId);

        // 1. Validate lead exists
        Lead lead = leadRepository.findById(request.getLeadId())
                .orElseThrow(() -> new ResourceNotFoundException("Lead not found with ID: " + request.getLeadId()));

        // 2. Validate user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        // 3. Business validation: nextFollowUpDate must be after followUpDate
        if (request.getNextFollowUpDate().isBefore(request.getFollowUpDate())) {
            throw new IllegalArgumentException("Next follow-up date must be after follow-up date");
        }

        // 4. Create new follow-up
        FollowUp followUp = new FollowUp();
        followUp.setLead(lead);
        followUp.setFollowUpDate(request.getFollowUpDate());
        followUp.setFollowUpType(request.getFollowUpType());
        followUp.setNotes(request.getNotes());
        followUp.setNextFollowUpDate(request.getNextFollowUpDate());
        followUp.setStatus("SCHEDULED"); // Default status
        followUp.setCreatedBy(user);

        // 5. Save to database
        FollowUp savedFollowUp = followUpRepository.save(followUp);

        // 6. Create Audit Log
        User performedBy = getLoggedInUser();
        createAuditLog("FollowUp", savedFollowUp.getFollowUpId(), "CREATE", null, savedFollowUp, performedBy);

        log.info("Follow-up created successfully with ID: {}", savedFollowUp.getFollowUpId());

        // 7. Map to response DTO
        return mapToResponse(savedFollowUp);
    }

    @Override
    public FollowUpResponse updateFollowUp(Long followUpId, UpdateFollowUpRequest request) {
        log.info("Updating follow-up with ID: {}", followUpId);

        // 1. Fetch existing follow-up
        FollowUp followUp = followUpRepository.findById(followUpId)
                .orElseThrow(() -> new ResourceNotFoundException("Follow-up not found with ID: " + followUpId));

        // 2. Business validation: nextFollowUpDate must be after followUpDate
        if (request.getNextFollowUpDate().isBefore(request.getFollowUpDate())) {
            throw new IllegalArgumentException("Next follow-up date must be after follow-up date");
        }

        // 3. Prevent updating completed follow-ups
        if (followUp.getStatus().equals("COMPLETED")) {
            throw new IllegalStateException("Cannot update completed follow-up");
        }

        // Clone old follow-up for audit log
        FollowUp oldFollowUp = cloneFollowUp(followUp);

        // 4. Update fields
        followUp.setFollowUpDate(request.getFollowUpDate());
        followUp.setFollowUpType(request.getFollowUpType());
        followUp.setNotes(request.getNotes());
        followUp.setNextFollowUpDate(request.getNextFollowUpDate());
        followUp.setStatus(request.getStatus());

        // 5. Save and return
        FollowUp updatedFollowUp = followUpRepository.save(followUp);

        // 6. Create Audit Log
        User performedBy = getLoggedInUser();
        createAuditLog("FollowUp", followUpId, "UPDATE", oldFollowUp, updatedFollowUp, performedBy);

        log.info("Follow-up updated successfully with ID: {}", followUpId);

        return mapToResponse(updatedFollowUp);
    }

    @Override
    @Transactional(readOnly = true)
    public FollowUpResponse getFollowUpById(Long followUpId) {
        log.info("Fetching follow-up with ID: {}", followUpId);
        FollowUp followUp = followUpRepository.findById(followUpId)
                .orElseThrow(() -> new ResourceNotFoundException("Follow-up not found with ID: " + followUpId));
        return mapToResponse(followUp);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FollowUpResponse> getAllFollowUps() {
        log.info("Fetching all follow-ups");
        return followUpRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FollowUpResponse> getFollowUpsByLead(Long leadId) {
        log.info("Fetching follow-ups for lead ID: {}", leadId);

        // Validate lead exists
        if (!leadRepository.existsById(leadId)) {
            throw new ResourceNotFoundException("Lead not found with ID: " + leadId);
        }

        return followUpRepository.findByLeadLeadIdOrderByFollowUpDateDesc(leadId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FollowUpResponse> getFollowUpsByUser(Long userId) {
        log.info("Fetching follow-ups for user ID: {}", userId);

        // Validate user exists
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with ID: " + userId);
        }

        return followUpRepository.findByCreatedByUserIdOrderByFollowUpDateDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FollowUpResponse> getFollowUpsByStatus(String status) {
        log.info("Fetching follow-ups with status: {}", status);
        return followUpRepository.findByStatusOrderByFollowUpDateDesc(status)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FollowUpResponse> getFollowUpsDueToday() {
        log.info("Fetching follow-ups due today");
        LocalDate today = LocalDate.now();
        return followUpRepository.findByFollowUpDateAndStatusOrderByFollowUpDateAsc(today, "SCHEDULED")
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FollowUpResponse> getOverdueFollowUps() {
        log.info("Fetching overdue follow-ups");
        LocalDate today = LocalDate.now();
        return followUpRepository.findByFollowUpDateBeforeAndStatusOrderByFollowUpDateAsc(today, "SCHEDULED")
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void markFollowUpCompleted(Long followUpId) {
        log.info("Marking follow-up as completed: {}", followUpId);

        FollowUp followUp = followUpRepository.findById(followUpId)
                .orElseThrow(() -> new ResourceNotFoundException("Follow-up not found with ID: " + followUpId));

        if (followUp.getStatus().equals("COMPLETED")) {
            throw new IllegalStateException("Follow-up is already completed");
        }

        // Clone old follow-up for audit log
        FollowUp oldFollowUp = cloneFollowUp(followUp);

        followUp.setStatus("COMPLETED");
        FollowUp updated = followUpRepository.save(followUp);

        // Create Audit Log
        User performedBy = getLoggedInUser();
        createAuditLog("FollowUp", followUpId, "MARK_COMPLETED", oldFollowUp, updated, performedBy);

        log.info("Follow-up marked as completed with ID: {}", followUpId);
    }

    @Override
    public void deleteFollowUp(Long followUpId) {
        log.info("Deleting follow-up with ID: {}", followUpId);

        FollowUp followUp = followUpRepository.findById(followUpId)
                .orElseThrow(() -> new ResourceNotFoundException("Follow-up not found with ID: " + followUpId));

        // Create Audit Log before deletion
        User performedBy = getLoggedInUser();
        createAuditLog("FollowUp", followUpId, "DELETE", followUp, null, performedBy);

        followUpRepository.delete(followUp);
        log.info("Follow-up deleted successfully with ID: {}", followUpId);
    }

    // ==================== HELPER METHODS ====================

    // Helper method to map FollowUp entity to FollowUpResponse DTO
    private FollowUpResponse mapToResponse(FollowUp followUp) {
        FollowUpResponse response = new FollowUpResponse();
        response.setFollowUpId(followUp.getFollowUpId());
        response.setLeadId(followUp.getLead().getLeadId());
        response.setLeadName(followUp.getLead().getFullName());
        response.setFollowUpDate(followUp.getFollowUpDate());
        response.setFollowUpType(followUp.getFollowUpType());
        response.setNotes(followUp.getNotes());
        response.setNextFollowUpDate(followUp.getNextFollowUpDate());
        response.setStatus(followUp.getStatus());
        response.setCreatedByUserId(followUp.getCreatedBy().getUserId());
        response.setCreatedByUserName(followUp.getCreatedBy().getFullName());
        response.setCreatedAt(followUp.getCreatedAt());
        response.setUpdatedAt(followUp.getUpdatedAt());
        return response;
    }

    private FollowUp cloneFollowUp(FollowUp followUp) {
        FollowUp clone = new FollowUp();
        clone.setFollowUpId(followUp.getFollowUpId());
        clone.setLead(followUp.getLead());
        clone.setFollowUpDate(followUp.getFollowUpDate());
        clone.setFollowUpType(followUp.getFollowUpType());
        clone.setNotes(followUp.getNotes());
        clone.setNextFollowUpDate(followUp.getNextFollowUpDate());
        clone.setStatus(followUp.getStatus());
        clone.setCreatedBy(followUp.getCreatedBy());
        return clone;
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
            log.info("Audit log created: {} {} on FollowUp ID: {}", action, entityType, entityId);
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
