package com.skillio.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.skillio.dto.AssignPermissionToRoleRequest;
import com.skillio.dto.RolePermissionResponse;
import com.skillio.dto.RoleRequest;
import com.skillio.dto.RoleResponse;
import com.skillio.dto.UpdateRoleRequest;
import com.skillio.services.RoleService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/roles")
@CrossOrigin(value = "http://localhost:4200")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    // Create Role (Admin only)
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_CREATE')")
    public ResponseEntity<RoleResponse> createRole(@Valid @RequestBody RoleRequest request) {
        RoleResponse response = roleService.createRole(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Update Role (Admin only)
    @PutMapping("/{roleId}")
    @PreAuthorize("hasAuthority('ROLE_UPDATE')")
    public ResponseEntity<RoleResponse> updateRole(
            @PathVariable Long roleId,
            @Valid @RequestBody UpdateRoleRequest request) {
        RoleResponse response = roleService.updateRole(roleId, request);
        return ResponseEntity.ok(response);
    }

    // Get Role by ID
    @GetMapping("/{roleId}")
    @PreAuthorize("hasAuthority('ROLE_READ')")
    public ResponseEntity<RoleResponse> getRoleById(@PathVariable Long roleId) {
        RoleResponse response = roleService.getRoleById(roleId);
        return ResponseEntity.ok(response);
    }

    // Get All Roles
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_LIST')")
    public ResponseEntity<List<RoleResponse>> getAllRoles() {
        List<RoleResponse> roles = roleService.getAllRoles();
        return ResponseEntity.ok(roles);
    }

    // Delete Role (Admin only)
    @DeleteMapping("/{roleId}")
    @PreAuthorize("hasAuthority('ROLE_DELETE')")
    public ResponseEntity<Void> deleteRole(@PathVariable Long roleId) {
        roleService.deleteRole(roleId);
        return ResponseEntity.noContent().build();
    }

    // Assign Permissions to Role (Admin only)
    @PostMapping("/assign-permissions")
    @PreAuthorize("hasAuthority('ROLE_UPDATE')")
    public ResponseEntity<List<RolePermissionResponse>> assignPermissions(
            @Valid @RequestBody AssignPermissionToRoleRequest request) {
        List<RolePermissionResponse> response = roleService.assignPermissionsToRole(request);
        return ResponseEntity.ok(response);
    }

    // Get Permissions for Role
    @GetMapping("/{roleId}/permissions")
    @PreAuthorize("hasAuthority('ROLE_READ')")
    public ResponseEntity<List<RolePermissionResponse>> getPermissionsForRole(@PathVariable Long roleId) {
        List<RolePermissionResponse> permissions = roleService.getPermissionsForRole(roleId);
        return ResponseEntity.ok(permissions);
    }
}
