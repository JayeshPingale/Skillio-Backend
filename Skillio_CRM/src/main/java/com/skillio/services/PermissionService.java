package com.skillio.services;

import java.util.List;

import com.skillio.dto.PermissionRequest;
import com.skillio.dto.PermissionResponse;

public interface PermissionService {
    PermissionResponse createPermission(PermissionRequest request);
    PermissionResponse getPermissionById(Long permissionId);
    PermissionResponse getPermissionByName(String permissionName);
    List<PermissionResponse> getAllPermissions();
    List<PermissionResponse> getPermissionsByModule(String module);
    void deletePermission(Long permissionId);
}
