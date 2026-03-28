package com.skillio.services.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillio.dto.AssignPermissionToRoleRequest;
import com.skillio.dto.RolePermissionResponse;
import com.skillio.dto.RoleRequest;
import com.skillio.dto.RoleResponse;
import com.skillio.dto.UpdateRoleRequest;
import com.skillio.entities.AuditLog;
import com.skillio.entities.Permission;
import com.skillio.entities.Role;
import com.skillio.entities.RolePermission;
import com.skillio.entities.User;
import com.skillio.exepection.ResourceNotFoundException;
import com.skillio.repositories.AuditLogRepository;
import com.skillio.repositories.PermissionRepository;
import com.skillio.repositories.RolePermissionRepository;
import com.skillio.repositories.RoleRepository;
import com.skillio.repositories.UserRepository;
import com.skillio.security.PermissionNameNormalizer;
import com.skillio.services.RoleService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final HttpServletRequest httpServletRequest;
    private final ObjectMapper objectMapper;

    @Override
    public RoleResponse createRole(RoleRequest request) {
        log.info("Creating new role: {}", request.getRoleName());

        // 1. Check if role already exists
        if (roleRepository.findByRoleName(request.getRoleName()).isPresent()) {
            throw new IllegalStateException("Role already exists: " + request.getRoleName());
        }

        // 2. Create new role
        Role role = new Role();
        role.setRoleName(request.getRoleName());
        role.setDescription(request.getDescription());
        role.setIsActive(true);

        // 3. Save to database
        Role savedRole = roleRepository.save(role);

        // 4. Create Audit Log
        User performedBy = getLoggedInUser();
        createAuditLog("Role", savedRole.getRoleId(), "CREATE", null, savedRole, performedBy);

        log.info("Role created successfully with ID: {}", savedRole.getRoleId());

        // 5. Map to response DTO
        return mapToResponse(savedRole);
    }

    @Override
    public RoleResponse updateRole(Long roleId, UpdateRoleRequest request) {
        log.info("Updating role with ID: {}", roleId);

        // 1. Fetch existing role
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with ID: " + roleId));

        // Clone old role for audit log
        Role oldRole = cloneRole(role);

        // Check duplicate role name (if changed)
        if (request.getRoleName() != null && !role.getRoleName().equals(request.getRoleName())) {
            if (roleRepository.findByRoleName(request.getRoleName()).isPresent()) {
                throw new IllegalStateException("Role already exists: " + request.getRoleName());
            }
            role.setRoleName(request.getRoleName());
        }

        // 2. Update fields
        if (request.getDescription() != null) {
            role.setDescription(request.getDescription());
        }

        if (request.getIsActive() != null) {
            role.setIsActive(request.getIsActive());
        }

        // 3. Save and return
        Role updatedRole = roleRepository.save(role);

        // 4. Create Audit Log
        User performedBy = getLoggedInUser();
        createAuditLog("Role", roleId, "UPDATE", oldRole, updatedRole, performedBy);

        log.info("Role updated successfully with ID: {}", roleId);

        return mapToResponse(updatedRole);
    }

    @Override
    @Transactional(readOnly = true)
    public RoleResponse getRoleById(Long roleId) {
        log.info("Fetching role with ID: {}", roleId);
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with ID: " + roleId));
        return mapToResponse(role);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleResponse> getAllRoles() {
        log.info("Fetching all roles");
        return roleRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteRole(Long roleId) {
        log.info("Deleting role with ID: {}", roleId);

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with ID: " + roleId));

        // Create Audit Log before deletion
        User performedBy = getLoggedInUser();
        createAuditLog("Role", roleId, "DELETE", role, null, performedBy);

        roleRepository.delete(role);
        log.info("Role deleted successfully with ID: {}", roleId);
    }

    @Override
    public List<RolePermissionResponse> assignPermissionsToRole(AssignPermissionToRoleRequest request) {
        log.info("Assigning permissions to role: {}", request.getRoleName());

        // 1. Fetch role by roleName
        Role role = roleRepository.findByRoleName(request.getRoleName())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + request.getRoleName()));

        // Get old permissions for audit log
        List<RolePermission> oldPermissions = rolePermissionRepository.findByRoleRoleId(role.getRoleId());
        
        // Replace semantics: reconcile current mappings with requested mappings.
        Set<String> requestedPermissionNames = request.getPermissionNames() == null
                ? Set.of()
                : request.getPermissionNames().stream()
                        .map(PermissionNameNormalizer::normalize)
                        .collect(Collectors.toCollection(LinkedHashSet::new));

        Map<String, RolePermission> existingByPermissionName = oldPermissions.stream()
                .collect(Collectors.toMap(
                        rp -> PermissionNameNormalizer.normalize(rp.getPermission().getPermissionName()),
                        rp -> rp,
                        (left, right) -> left));

        List<RolePermission> mappingsToDelete = oldPermissions.stream()
                .filter(rp -> !requestedPermissionNames.contains(
                        PermissionNameNormalizer.normalize(rp.getPermission().getPermissionName())))
                .toList();

        if (!mappingsToDelete.isEmpty()) {
            rolePermissionRepository.deleteAllInBatch(mappingsToDelete);
        }

        List<RolePermission> rolePermissions = new ArrayList<>();

        for (String permissionName : requestedPermissionNames) {
            RolePermission existingMapping = existingByPermissionName.get(permissionName);
            if (existingMapping != null) {
                rolePermissions.add(existingMapping);
                continue;
            }

            Permission permission = findPermissionByName(permissionName)
                    .orElseThrow(() -> new ResourceNotFoundException("Permission not found: " + permissionName));

            RolePermission rolePermission = new RolePermission();
            rolePermission.setRole(role);
            rolePermission.setPermission(permission);
            rolePermissions.add(rolePermissionRepository.save(rolePermission));
        }

        // 4. Create Audit Log for permission assignment
        User performedBy = getLoggedInUser();
        createAuditLog("RolePermission", role.getRoleId(), "ASSIGN_PERMISSIONS", oldPermissions, rolePermissions, performedBy);

        log.info("Permissions assigned successfully to role: {}", request.getRoleName());

        // 5. Map to response
        return rolePermissions.stream()
                .map(this::mapToRolePermissionResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RolePermissionResponse> getPermissionsForRole(Long roleId) {
        log.info("Fetching permissions for role ID: {}", roleId);
        List<RolePermission> rolePermissions = rolePermissionRepository.findByRoleRoleId(roleId);
        return rolePermissions.stream()
                .map(this::mapToRolePermissionResponse)
                .collect(Collectors.toList());
    }

    // ==================== HELPER METHODS ====================

    // Helper method: Map Role to RoleResponse
    private RoleResponse mapToResponse(Role role) {
        RoleResponse response = new RoleResponse();
        response.setRoleId(role.getRoleId());
        response.setRoleName(role.getRoleName());
        response.setDescription(role.getDescription());
        response.setIsActive(role.getIsActive());
        response.setCreatedAt(role.getCreatedAt());
        response.setUpdatedAt(role.getUpdatedAt());
        return response;
    }

    // Helper method: Map RolePermission to RolePermissionResponse
    private RolePermissionResponse mapToRolePermissionResponse(RolePermission rp) {
        RolePermissionResponse response = new RolePermissionResponse();
        response.setRolePermissionId(rp.getRolePermissionId());
        response.setRoleId(rp.getRole().getRoleId());
        response.setRoleName(rp.getRole().getRoleName());
        response.setPermissionId(rp.getPermission().getPermissionId());
        response.setPermissionName(PermissionNameNormalizer.normalize(rp.getPermission().getPermissionName()));
        response.setModule(rp.getPermission().getModule());
        response.setAction(rp.getPermission().getAction());
        response.setAssignedAt(rp.getAssignedAt());
        return response;
    }

    private java.util.Optional<Permission> findPermissionByName(String permissionName) {
        return permissionRepository.findByPermissionName(permissionName)
                .or(() -> permissionRepository.findByPermissionName(PermissionNameNormalizer.toLegacy(permissionName)));
    }

    private Role cloneRole(Role role) {
        Role clone = new Role();
        clone.setRoleId(role.getRoleId());
        clone.setRoleName(role.getRoleName());
        clone.setDescription(role.getDescription());
        clone.setIsActive(role.getIsActive());
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
            log.info("Audit log created: {} {} on {} ID: {}", action, entityType, entityType, entityId);
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
