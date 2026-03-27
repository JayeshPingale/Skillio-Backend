package com.skillio.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PermissionResponse {
    
    private Long permissionId;
    private String permissionName; // CREATE_LEAD, READ_LEAD, UPDATE_LEAD, etc.
    private String module; // LEAD, PAYMENT, COMMISSION, etc.
    private String action; // CREATE, READ, UPDATE, DELETE
    private String description;
    private LocalDateTime createdAt;
}
