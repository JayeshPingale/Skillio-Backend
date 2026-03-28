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
import com.skillio.dto.CreateLeadSourceRequest;
import com.skillio.dto.LeadSourceResponse;
import com.skillio.entities.AuditLog;
import com.skillio.entities.LeadSource;
import com.skillio.entities.User;
import com.skillio.exepection.ResourceNotFoundException;
import com.skillio.repositories.AuditLogRepository;
import com.skillio.repositories.LeadSourceRepository;
import com.skillio.repositories.UserRepository;
import com.skillio.services.LeadSourceService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class LeadSourceServiceImpl implements LeadSourceService {

    private final LeadSourceRepository leadSourceRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final HttpServletRequest httpServletRequest;
    private final ObjectMapper objectMapper;

    @Override
    public LeadSourceResponse createLeadSource(CreateLeadSourceRequest request) {
        log.info("Creating new lead source: {}", request.getName());

        // 1. Check if lead source already exists
        if (leadSourceRepository.findByName(request.getName()).isPresent()) {
            throw new IllegalStateException("Lead source already exists: " + request.getName());
        }

        // 2. Create new lead source
        LeadSource leadSource = new LeadSource();
        leadSource.setName(request.getName());
        leadSource.setChannel(request.getChannel());
        leadSource.setDescription(request.getDescription());
        leadSource.setIsActive(true);

        // 3. Save to database
        LeadSource savedLeadSource = leadSourceRepository.save(leadSource);

        // 4. Create Audit Log
        User performedBy = getLoggedInUser();
        createAuditLog("LeadSource", savedLeadSource.getSourceId(), "CREATE", null, savedLeadSource, performedBy);

        log.info("Lead source created successfully with ID: {}", savedLeadSource.getSourceId());

        // 5. Map to response DTO
        return mapToResponse(savedLeadSource);
    }

    @Override
    @Transactional(readOnly = true)
    public LeadSourceResponse getLeadSourceById(Long sourceId) {
        log.info("Fetching lead source with ID: {}", sourceId);
        LeadSource leadSource = leadSourceRepository.findById(sourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Lead source not found with ID: " + sourceId));
        return mapToResponse(leadSource);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeadSourceResponse> getAllLeadSources() {
        log.info("Fetching all lead sources");
        return leadSourceRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeadSourceResponse> getActiveLeadSources() {
        log.info("Fetching active lead sources");
        return leadSourceRepository.findByIsActiveTrue()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public LeadSourceResponse updateLeadSource(Long sourceId, CreateLeadSourceRequest request) {
        log.info("Updating lead source with ID: {}", sourceId);

        // 1. Fetch existing lead source
        LeadSource leadSource = leadSourceRepository.findById(sourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Lead source not found with ID: " + sourceId));

        // Clone old lead source for audit log
        LeadSource oldLeadSource = cloneLeadSource(leadSource);

        // 2. Check if name is being changed and if it already exists
        if (!leadSource.getName().equals(request.getName())) {
            if (leadSourceRepository.findByName(request.getName()).isPresent()) {
                throw new IllegalStateException("Lead source name already taken: " + request.getName());
            }
        }

        // 3. Update fields
        leadSource.setName(request.getName());
        leadSource.setChannel(request.getChannel());
        leadSource.setDescription(request.getDescription());

        // 4. Save and return
        LeadSource updatedLeadSource = leadSourceRepository.save(leadSource);

        // 5. Create Audit Log
        User performedBy = getLoggedInUser();
        createAuditLog("LeadSource", sourceId, "UPDATE", oldLeadSource, updatedLeadSource, performedBy);

        log.info("Lead source updated successfully with ID: {}", sourceId);

        return mapToResponse(updatedLeadSource);
    }

    @Override
    public void deleteLeadSource(Long sourceId) {
        log.info("Deleting lead source with ID: {}", sourceId);

        LeadSource leadSource = leadSourceRepository.findById(sourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Lead source not found with ID: " + sourceId));

        // Create Audit Log before deletion
        User performedBy = getLoggedInUser();
        createAuditLog("LeadSource", sourceId, "DELETE", leadSource, null, performedBy);

        leadSourceRepository.delete(leadSource);
        log.info("Lead source deleted successfully with ID: {}", sourceId);
    }

    @Override
    public void toggleLeadSourceStatus(Long sourceId) {
        log.info("Toggling status for lead source ID: {}", sourceId);

        LeadSource leadSource = leadSourceRepository.findById(sourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Lead source not found with ID: " + sourceId));

        // Clone old lead source for audit log
        LeadSource oldLeadSource = cloneLeadSource(leadSource);

        leadSource.setIsActive(!leadSource.getIsActive());
        LeadSource updated = leadSourceRepository.save(leadSource);

        // Create Audit Log
        User performedBy = getLoggedInUser();
        createAuditLog("LeadSource", sourceId, "TOGGLE_STATUS", oldLeadSource, updated, performedBy);

        log.info("Lead source status toggled to: {} for ID: {}", updated.getIsActive(), sourceId);
    }

    // ==================== HELPER METHODS ====================

    // Helper method to map LeadSource entity to LeadSourceResponse DTO
    private LeadSourceResponse mapToResponse(LeadSource leadSource) {
        LeadSourceResponse response = new LeadSourceResponse();
        response.setSourceId(leadSource.getSourceId());
        response.setName(leadSource.getName());
        response.setChannel(leadSource.getChannel());
        response.setDescription(leadSource.getDescription());
        response.setIsActive(leadSource.getIsActive());
        response.setCreatedAt(leadSource.getCreatedAt());
        response.setUpdatedAt(leadSource.getUpdatedAt());
        return response;
    }

    private LeadSource cloneLeadSource(LeadSource leadSource) {
        LeadSource clone = new LeadSource();
        clone.setSourceId(leadSource.getSourceId());
        clone.setName(leadSource.getName());
        clone.setChannel(leadSource.getChannel());
        clone.setDescription(leadSource.getDescription());
        clone.setIsActive(leadSource.getIsActive());
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
            log.info("Audit log created: {} {} on LeadSource ID: {}", action, entityType, entityId);
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
