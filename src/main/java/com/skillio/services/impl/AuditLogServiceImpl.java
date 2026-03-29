package com.skillio.services.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillio.dto.AuditLogResponse;
import com.skillio.entities.AuditLog;
import com.skillio.entities.User;
import com.skillio.exepection.ResourceNotFoundException;
import com.skillio.repositories.AuditLogRepository;
import com.skillio.repositories.UserRepository;
import com.skillio.services.AuditLogService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final HttpServletRequest httpServletRequest;
    private final PlatformTransactionManager transactionManager;

    @Override
    public void createAuditLog(String entityType, Long entityId, String action,
                               Object oldValue, Object newValue, User performedBy) {
        try {
            TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
            transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            transactionTemplate.executeWithoutResult(status -> {
                AuditLog auditLog = new AuditLog();
                auditLog.setEntityType(entityType);
                auditLog.setEntityId(entityId);
                auditLog.setAction(action);
                auditLog.setOldValue(oldValue != null ? convertToJson(oldValue) : null);
                auditLog.setNewValue(newValue != null ? convertToJson(newValue) : null);
                auditLog.setPerformedBy(resolvePerformedBy(performedBy));
                auditLog.setIpAddress(getClientIp());
                auditLog.setUserAgent(httpServletRequest.getHeader("User-Agent"));
                auditLog.setPerformedAt(LocalDateTime.now());
                auditLogRepository.save(auditLog);
            });

            log.info("Audit log saved: {} {} on {} ID: {}",
                    action, entityType, entityType, entityId);
        } catch (Exception e) {
            log.error("Failed to create audit log for {} {}: {}",
                    entityType, entityId, e.getMessage(), e);
        }
    }

    private User resolvePerformedBy(User performedBy) {
        if (performedBy == null || performedBy.getUserId() == null) {
            return null;
        }
        return userRepository.findById(performedBy.getUserId()).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogResponse> getAllAuditLogs() {
        log.info("Fetching all audit logs");
        return auditLogRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogResponse> getAuditLogsByEntityType(String entityType) {
        log.info("Fetching audit logs for entity type: {}", entityType);
        return auditLogRepository.findByEntityTypeOrderByPerformedAtDesc(entityType).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogResponse> getAuditLogsByEntityTypeAndId(String entityType, Long entityId) {
        log.info("Fetching audit logs for entity type: {} and entity ID: {}", entityType, entityId);
        return auditLogRepository.findByEntityTypeAndEntityId(entityType, entityId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogResponse> getAuditLogsByUser(Long userId) {
        log.info("Fetching audit logs for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        return auditLogRepository.findByPerformedBy(user).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogResponse> getAuditLogsByAction(String action) {
        log.info("Fetching audit logs for action: {}", action);
        return auditLogRepository.findByAction(action).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogResponse> getAuditLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Fetching audit logs between {} and {}", startDate, endDate);
        return auditLogRepository.findByPerformedAtBetween(startDate, endDate).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AuditLogResponse getAuditLogById(Long auditId) {
        log.info("Fetching audit log with ID: {}", auditId);
        AuditLog auditLog = auditLogRepository.findById(auditId)
                .orElseThrow(() -> new ResourceNotFoundException("Audit log not found with ID: " + auditId));
        return mapToResponse(auditLog);
    }

    private String convertToJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.warn("Failed to convert object to JSON: {}", e.getMessage());
            return "{\"error\": \"Serialization failed\"}";
        }
    }

    private String getClientIp() {
        String xForwardedFor = httpServletRequest.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0];
        }
        return httpServletRequest.getRemoteAddr();
    }

    private AuditLogResponse mapToResponse(AuditLog auditLog) {
        AuditLogResponse response = new AuditLogResponse();
        response.setAuditId(auditLog.getAuditId());
        response.setEntityType(auditLog.getEntityType());
        response.setEntityId(auditLog.getEntityId());
        response.setAction(auditLog.getAction());
        response.setOldValue(auditLog.getOldValue());
        response.setNewValue(auditLog.getNewValue());

        User performedBy = auditLog.getPerformedBy();
        if (performedBy != null) {
            response.setPerformedByUserId(performedBy.getUserId());
            response.setPerformedByUserName(performedBy.getFullName());
            response.setPerformedByUserEmail(performedBy.getEmail());
        }

        response.setIpAddress(auditLog.getIpAddress());
        response.setUserAgent(auditLog.getUserAgent());
        response.setPerformedAt(auditLog.getPerformedAt());

        return response;
    }
}
