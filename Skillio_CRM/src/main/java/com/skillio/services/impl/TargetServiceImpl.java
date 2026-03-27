package com.skillio.services.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillio.dto.CreateTargetRequest;
import com.skillio.dto.TargetResponse;
import com.skillio.dto.UpdateTargetAchievementRequest;
import com.skillio.dto.UpdateTargetRequest;
import com.skillio.entities.AuditLog;
import com.skillio.entities.Target;
import com.skillio.entities.User;
import com.skillio.exepection.ResourceNotFoundException;
import com.skillio.repositories.AuditLogRepository;
import com.skillio.repositories.TargetRepository;
import com.skillio.repositories.UserRepository;
import com.skillio.services.TargetService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TargetServiceImpl implements TargetService {

    private final TargetRepository targetRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final HttpServletRequest httpServletRequest;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public TargetResponse createTarget(CreateTargetRequest request, Long loggedInUserId) {
        log.info("Creating target for user ID: {}", request.getUserId());

        // Validate User exists
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + request.getUserId()));

        // Validate dates
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }

        // Check for duplicate target for same user and period
        targetRepository.findByUserAndStartDateAndEndDate(user, request.getStartDate(), request.getEndDate())
                .ifPresent(existingTarget -> {
                    throw new IllegalArgumentException("Target already exists for this user and period");
                });

        User createdBy = userRepository.findById(loggedInUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + loggedInUserId));

        // Create Target entity
        Target target = new Target();
        target.setUser(user);
        target.setTargetPeriod(request.getTargetPeriod());
        target.setTargetLeads(request.getTargetLeads());
        target.setTargetEnrollments(request.getTargetEnrollments());
        target.setTargetRevenue(request.getTargetRevenue());
        target.setStartDate(request.getStartDate());
        target.setEndDate(request.getEndDate());
        target.setStatus("ACTIVE");
        target.setAchievedLeads(0);
        target.setAchievedEnrollments(0);
        target.setAchievedRevenue(BigDecimal.ZERO);
        target.setRemarks(request.getRemarks());

        Target savedTarget = targetRepository.save(target);

        // Create Audit Log
        createAuditLog("Target", savedTarget.getTargetId(), "CREATE", null, savedTarget, createdBy);

        log.info("Target created successfully with ID: {}", savedTarget.getTargetId());
        return mapToResponse(savedTarget);
    }

    @Override
    @Transactional(readOnly = true)
    public TargetResponse getTargetById(Long targetId) {
        log.info("Fetching target with ID: {}", targetId);
        Target target = getTargetEntityById(targetId);
        return mapToResponse(target);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TargetResponse> getAllTargets() {
        log.info("Fetching all targets");
        return targetRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TargetResponse> getTargetsByUser(Long userId) {
        log.info("Fetching targets for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        return targetRepository.findByUser(user).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TargetResponse> getTargetsByStatus(String status) {
        log.info("Fetching targets with status: {}", status);
        return targetRepository.findByStatus(status).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TargetResponse> getActiveTargets() {
        log.info("Fetching active targets");
        LocalDate today = LocalDate.now();
        return targetRepository.findByEndDateAfter(today).stream()
                .filter(target -> "ACTIVE".equals(target.getStatus()) || "ON_TRACK".equals(target.getStatus()) || "AT_RISK".equals(target.getStatus()))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TargetResponse updateTarget(Long targetId, UpdateTargetRequest request, Long loggedInUserId) {
        log.info("Updating target with ID: {}", targetId);

        Target target = getTargetEntityById(targetId);

        // Cannot update completed targets
        if ("COMPLETED".equals(target.getStatus())) {
            throw new IllegalStateException("Cannot update completed target");
        }

        Target oldTarget = cloneTarget(target);

        User updatedBy = userRepository.findById(loggedInUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + loggedInUserId));

        // Update fields if provided
        if (request.getTargetLeads() != null) {
            target.setTargetLeads(request.getTargetLeads());
        }

        if (request.getTargetEnrollments() != null) {
            target.setTargetEnrollments(request.getTargetEnrollments());
        }

        if (request.getTargetRevenue() != null) {
            target.setTargetRevenue(request.getTargetRevenue());
        }

        if (request.getEndDate() != null) {
            if (request.getEndDate().isBefore(target.getStartDate())) {
                throw new IllegalArgumentException("End date cannot be before start date");
            }
            target.setEndDate(request.getEndDate());
        }

        if (request.getRemarks() != null) {
            target.setRemarks(request.getRemarks());
        }

        // Re-evaluate status after update
        evaluateTargetStatus(target);

        Target updated = targetRepository.save(target);

        // Create Audit Log
        createAuditLog("Target", targetId, "UPDATE", oldTarget, updated, updatedBy);

        log.info("Target updated successfully with ID: {}", targetId);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public TargetResponse updateTargetAchievement(Long targetId, UpdateTargetAchievementRequest request, Long loggedInUserId) {
        log.info("Updating target achievement for target ID: {}", targetId);

        Target target = getTargetEntityById(targetId);

        // Cannot update completed targets
        if ("COMPLETED".equals(target.getStatus())) {
            throw new IllegalStateException("Cannot update completed target");
        }

        Target oldTarget = cloneTarget(target);

        User updatedBy = userRepository.findById(loggedInUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + loggedInUserId));

        // Update achieved values if provided
        if (request.getAchievedLeads() != null) {
            target.setAchievedLeads(request.getAchievedLeads());
        }

        if (request.getAchievedEnrollments() != null) {
            target.setAchievedEnrollments(request.getAchievedEnrollments());
        }

        if (request.getAchievedRevenue() != null) {
            target.setAchievedRevenue(request.getAchievedRevenue());
        }

        // Auto-evaluate status based on achievement
        evaluateTargetStatus(target);

        Target updated = targetRepository.save(target);

        // Create Audit Log
        createAuditLog("Target", targetId, "UPDATE_ACHIEVEMENT", oldTarget, updated, updatedBy);

        log.info("Target achievement updated successfully for target ID: {}", targetId);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public TargetResponse markAsCompleted(Long targetId, Long loggedInUserId) {
        log.info("Marking target {} as COMPLETED", targetId);

        Target target = getTargetEntityById(targetId);

        if ("COMPLETED".equals(target.getStatus())) {
            throw new IllegalStateException("Target is already COMPLETED");
        }

        Target oldTarget = cloneTarget(target);

        User updatedBy = userRepository.findById(loggedInUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + loggedInUserId));

        target.setStatus("COMPLETED");

        Target updated = targetRepository.save(target);

        // Create Audit Log
        createAuditLog("Target", targetId, "MARK_COMPLETED", oldTarget, updated, updatedBy);

        log.info("Target {} marked as COMPLETED", targetId);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteTarget(Long targetId, Long loggedInUserId) {
        log.info("Deleting target with ID: {}", targetId);

        Target target = getTargetEntityById(targetId);

        User deletedBy = userRepository.findById(loggedInUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + loggedInUserId));

        // Create Audit Log before deletion
        createAuditLog("Target", targetId, "DELETE", target, null, deletedBy);

        targetRepository.delete(target);
        log.info("Target deleted successfully with ID: {}", targetId);
    }

    @Override
    public Target getTargetEntityById(Long targetId) {
        return targetRepository.findById(targetId)
                .orElseThrow(() -> new ResourceNotFoundException("Target not found with ID: " + targetId));
    }

    @Override
    public void evaluateTargetStatus(Target target) {
        // Skip if already completed
        if ("COMPLETED".equals(target.getStatus())) {
            return;
        }

        // Check if target period has ended
        if (LocalDate.now().isAfter(target.getEndDate())) {
            target.setStatus("COMPLETED");
            return;
        }

        // Calculate achievement percentages
        double leadsPercentage = calculatePercentage(target.getAchievedLeads(), target.getTargetLeads());
        double enrollmentsPercentage = calculatePercentage(target.getAchievedEnrollments(), target.getTargetEnrollments());
        double revenuePercentage = calculatePercentage(target.getAchievedRevenue(), target.getTargetRevenue());

        // Average achievement percentage
        double avgPercentage = (leadsPercentage + enrollmentsPercentage + revenuePercentage) / 3;

        // Set status based on achievement
        if (avgPercentage >= 80) {
            target.setStatus("ON_TRACK");
        } else if (avgPercentage >= 50) {
            target.setStatus("AT_RISK");
        } else {
            target.setStatus("ACTIVE");
        }

        log.info("Target {} status evaluated as: {} (avg: {}%)", target.getTargetId(), target.getStatus(), avgPercentage);
    }

    // ==================== HELPER METHODS ====================

    private TargetResponse mapToResponse(Target target) {
        TargetResponse response = new TargetResponse();
        response.setTargetId(target.getTargetId());

        // User Info
        User user = target.getUser();
        response.setUserId(user.getUserId());
        response.setUserName(user.getFullName());
        response.setUserEmail(user.getEmail());

        // Target Info
        response.setTargetPeriod(target.getTargetPeriod());
        response.setTargetLeads(target.getTargetLeads());
        response.setTargetEnrollments(target.getTargetEnrollments());
        response.setTargetRevenue(target.getTargetRevenue());

        // Achieved Values
        response.setAchievedLeads(target.getAchievedLeads());
        response.setAchievedEnrollments(target.getAchievedEnrollments());
        response.setAchievedRevenue(target.getAchievedRevenue());

        // Calculate Percentages
        response.setLeadsAchievementPercentage(calculatePercentage(target.getAchievedLeads(), target.getTargetLeads()));
        response.setEnrollmentsAchievementPercentage(calculatePercentage(target.getAchievedEnrollments(), target.getTargetEnrollments()));
        response.setRevenueAchievementPercentage(calculatePercentage(target.getAchievedRevenue(), target.getTargetRevenue()));

        response.setStartDate(target.getStartDate());
        response.setEndDate(target.getEndDate());
        response.setStatus(target.getStatus());
        response.setRemarks(target.getRemarks());
        response.setCreatedAt(target.getCreatedAt());
        response.setUpdatedAt(target.getUpdatedAt());

        return response;
    }

    private double calculatePercentage(Integer achieved, Integer target) {
        if (target == null || target == 0) {
            return 0.0;
        }
        return BigDecimal.valueOf(achieved)
                .divide(BigDecimal.valueOf(target), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
    }

    private double calculatePercentage(BigDecimal achieved, BigDecimal target) {
        if (target == null || target.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        return achieved.divide(target, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
    }

    private Target cloneTarget(Target target) {
        Target clone = new Target();
        clone.setTargetId(target.getTargetId());
        clone.setUser(target.getUser());
        clone.setTargetPeriod(target.getTargetPeriod());
        clone.setTargetLeads(target.getTargetLeads());
        clone.setTargetEnrollments(target.getTargetEnrollments());
        clone.setTargetRevenue(target.getTargetRevenue());
        clone.setStartDate(target.getStartDate());
        clone.setEndDate(target.getEndDate());
        clone.setAchievedLeads(target.getAchievedLeads());
        clone.setAchievedEnrollments(target.getAchievedEnrollments());
        clone.setAchievedRevenue(target.getAchievedRevenue());
        clone.setStatus(target.getStatus());
        clone.setRemarks(target.getRemarks());
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
            log.info("Audit log created: {} {} on Target ID: {}", action, entityType, entityId);
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
