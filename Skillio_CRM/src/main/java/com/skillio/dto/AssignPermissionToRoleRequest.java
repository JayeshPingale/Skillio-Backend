package com.skillio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignPermissionToRoleRequest {
    
    @NotBlank(message = "Role name is required")
    private String roleName; // "ROLE_ADMIN", "ROLE_SALES_EXECUTIVE"
    
    @NotNull(message = "At least one permission is required")
    private List<String> permissionNames; // ["CREATE_LEAD", "READ_LEAD", "UPDATE_LEAD"]
}
