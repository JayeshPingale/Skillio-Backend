package com.skillio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangeStudentStatusRequest {
    
    @NotNull(message = "Student ID is required")
    private Long studentId;
    
    @NotBlank(message = "Status is required")
    private String status; // ACTIVE, COMPLETED, DROPPED, ON_HOLD
    
    @NotBlank(message = "Remarks are required")
    private String remarks; // Why status changed
}
