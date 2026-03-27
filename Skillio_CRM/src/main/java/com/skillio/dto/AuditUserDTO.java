package com.skillio.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ✅ Simplified DTO for User audit logging
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditUserDTO {
    private Long userId;
    private String fullName;
    private String email;
    private String contactNumber;
    private String profilePic;
    private String roleName;
    private Boolean isActive;
}
