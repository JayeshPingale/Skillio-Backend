package com.skillio.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RolePermissionResponse {
    
    private Long rolePermissionId;
    private Long roleId;
    private String roleName;
    private Long permissionId;
    private String permissionName;
    private String module;
    private String action;
    private LocalDateTime assignedAt;
}
