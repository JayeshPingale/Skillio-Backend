package com.skillio.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PermissionRequest {
    
    @NotBlank(message = "Permission name is required")
    private String permissionName; // CREATE_LEAD, READ_LEAD, UPDATE_LEAD, DELETE_LEAD
    
    @NotBlank(message = "Module is required")
    private String module; // LEAD, ENROLLMENT, PAYMENT, BATCH, STUDENT, COMMISSION
    
    @NotBlank(message = "Action is required")
    private String action; // CREATE, READ, UPDATE, DELETE
    
    @NotBlank(message = "Description is required")
    private String description;
    
    // permissionId NAHI - Auto-generated
}
