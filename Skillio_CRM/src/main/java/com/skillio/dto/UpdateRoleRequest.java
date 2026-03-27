package com.skillio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRoleRequest {
    
    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 200)
    private String description;
    
    private String roleName;
    private Boolean isActive; // Activate/Deactivate role
    
    // roleId NAHI - From URL
    // roleName NAHI - Can't change role name after creation
}
