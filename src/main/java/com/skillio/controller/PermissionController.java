package com.skillio.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.skillio.dto.PermissionRequest;
import com.skillio.dto.PermissionResponse;
import com.skillio.services.PermissionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    // Create Permission (Admin only)
    @PostMapping
    @PreAuthorize("hasAuthority('PERMISSION_CREATE')")
    public ResponseEntity<PermissionResponse> createPermission(@Valid @RequestBody PermissionRequest request) {
        PermissionResponse response = permissionService.createPermission(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Get Permission by ID
    @GetMapping("/{permissionId}")
    @PreAuthorize("hasAuthority('PERMISSION_READ')")
    public ResponseEntity<PermissionResponse> getPermissionById(@PathVariable Long permissionId) {
        PermissionResponse response = permissionService.getPermissionById(permissionId);
        return ResponseEntity.ok(response);
    }

    // Get Permission by Name
    @GetMapping("/name/{permissionName}")
    @PreAuthorize("hasAuthority('PERMISSION_READ')")
    public ResponseEntity<PermissionResponse> getPermissionByName(@PathVariable String permissionName) {
        PermissionResponse response = permissionService.getPermissionByName(permissionName);
        return ResponseEntity.ok(response);
    }

    // Get All Permissions
    @GetMapping
    @PreAuthorize("hasAuthority('PERMISSION_LIST')")
    public ResponseEntity<List<PermissionResponse>> getAllPermissions() {
        List<PermissionResponse> permissions = permissionService.getAllPermissions();
        return ResponseEntity.ok(permissions);
    }

    // Get Permissions by Module (e.g., LEAD, PAYMENT, etc.)
    @GetMapping("/module/{module}")
    @PreAuthorize("hasAuthority('PERMISSION_LIST')")
    public ResponseEntity<List<PermissionResponse>> getPermissionsByModule(@PathVariable String module) {
        List<PermissionResponse> permissions = permissionService.getPermissionsByModule(module);
        return ResponseEntity.ok(permissions);
    }

    // Delete Permission (Admin only)
    @DeleteMapping("/{permissionId}")
    @PreAuthorize("hasAuthority('PERMISSION_DELETE')")
    public ResponseEntity<Void> deletePermission(@PathVariable Long permissionId) {
        permissionService.deletePermission(permissionId);
        return ResponseEntity.noContent().build();
    }
}
