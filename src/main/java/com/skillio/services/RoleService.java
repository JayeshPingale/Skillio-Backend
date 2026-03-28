package com.skillio.services;

import java.util.List;

import com.skillio.dto.AssignPermissionToRoleRequest;
import com.skillio.dto.RolePermissionResponse;
import com.skillio.dto.RoleRequest;
import com.skillio.dto.RoleResponse;
import com.skillio.dto.UpdateRoleRequest;

public interface RoleService {
    RoleResponse createRole(RoleRequest request);
    RoleResponse updateRole(Long roleId, UpdateRoleRequest request);
    RoleResponse getRoleById(Long roleId);
    List<RoleResponse> getAllRoles();
    void deleteRole(Long roleId);
    
    // Permission management
    List<RolePermissionResponse> assignPermissionsToRole(AssignPermissionToRoleRequest request);
    List<RolePermissionResponse> getPermissionsForRole(Long roleId);
}
