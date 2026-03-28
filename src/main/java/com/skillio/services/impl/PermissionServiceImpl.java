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
import com.skillio.dto.PermissionRequest;
import com.skillio.dto.PermissionResponse;
import com.skillio.entities.AuditLog;
import com.skillio.entities.Permission;
import com.skillio.entities.User;
import com.skillio.exepection.ResourceNotFoundException;
import com.skillio.repositories.AuditLogRepository;
import com.skillio.repositories.PermissionRepository;
import com.skillio.repositories.UserRepository;
import com.skillio.security.PermissionNameNormalizer;
import com.skillio.services.PermissionService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final HttpServletRequest httpServletRequest;
    private final ObjectMapper objectMapper;

    @Override
    public PermissionResponse createPermission(PermissionRequest request) {
        String normalizedPermissionName = PermissionNameNormalizer.normalize(request.getPermissionName());
        log.info("Creating new permission: {}", normalizedPermissionName);

        // 1. Check if permission already exists
        if (findPermissionByName(normalizedPermissionName).isPresent()) {
            throw new IllegalStateException("Permission already exists: " + normalizedPermissionName);
        }

        // 2. Create new permission
        Permission permission = new Permission();
        permission.setPermissionName(normalizedPermissionName);
        permission.setModule(request.getModule());
        permission.setAction(request.getAction());
        permission.setDescription(request.getDescription());

        // 3. Save to database
        Permission savedPermission = permissionRepository.save(permission);

        // 4. Create Audit Log
        User performedBy = getLoggedInUser();
        createAuditLog("Permission", savedPermission.getPermissionId(), "CREATE", null, savedPermission, performedBy);

        log.info("Permission created successfully with ID: {}", savedPermission.getPermissionId());

        // 5. Map to response DTO
        return mapToResponse(savedPermission);
    }

    @Override
    @Transactional(readOnly = true)
    public PermissionResponse getPermissionById(Long permissionId) {
        log.info("Fetching permission with ID: {}", permissionId);
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found with ID: " + permissionId));
        return mapToResponse(permission);
    }

    @Override
    @Transactional(readOnly = true)
    public PermissionResponse getPermissionByName(String permissionName) {
        String normalizedPermissionName = PermissionNameNormalizer.normalize(permissionName);
        log.info("Fetching permission by name: {}", normalizedPermissionName);
        Permission permission = findPermissionByName(normalizedPermissionName)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found: " + normalizedPermissionName));
        return mapToResponse(permission);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionResponse> getAllPermissions() {
        log.info("Fetching all permissions");
        return permissionRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionResponse> getPermissionsByModule(String module) {
        log.info("Fetching permissions for module: {}", module);
        return permissionRepository.findByModule(module)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deletePermission(Long permissionId) {
        log.info("Deleting permission with ID: {}", permissionId);

        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found with ID: " + permissionId));

        // Create Audit Log before deletion
        User performedBy = getLoggedInUser();
        createAuditLog("Permission", permissionId, "DELETE", permission, null, performedBy);

        permissionRepository.delete(permission);
        log.info("Permission deleted successfully with ID: {}", permissionId);
    }

    // ==================== HELPER METHODS ====================

    // Helper method to map Permission entity to PermissionResponse DTO
    private PermissionResponse mapToResponse(Permission permission) {
        PermissionResponse response = new PermissionResponse();
        response.setPermissionId(permission.getPermissionId());
        response.setPermissionName(PermissionNameNormalizer.normalize(permission.getPermissionName()));
        response.setModule(permission.getModule());
        response.setAction(permission.getAction());
        response.setDescription(permission.getDescription());
        response.setCreatedAt(permission.getCreatedAt());
        return response;
    }

    private Permission clonePermission(Permission permission) {
        Permission clone = new Permission();
        clone.setPermissionId(permission.getPermissionId());
        clone.setPermissionName(PermissionNameNormalizer.normalize(permission.getPermissionName()));
        clone.setModule(permission.getModule());
        clone.setAction(permission.getAction());
        clone.setDescription(permission.getDescription());
        return clone;
    }

    private java.util.Optional<Permission> findPermissionByName(String permissionName) {
        return permissionRepository.findByPermissionName(permissionName)
                .or(() -> permissionRepository.findByPermissionName(PermissionNameNormalizer.toLegacy(permissionName)));
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
            log.info("Audit log created: {} {} on Permission ID: {}", action, entityType, entityId);
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
