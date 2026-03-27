package com.skillio.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTargetRequest {
    
    @NotNull(message = "Sales Executive ID is required")
    private Long userId;
    
    @NotBlank(message = "Target period is required")
    private String targetPeriod; // MONTHLY, QUARTERLY
    
    @NotNull(message = "Target leads is required")
    @Min(value = 1, message = "Minimum 1 lead")
    private Integer targetLeads;
    
    @NotNull(message = "Target enrollments is required")
    @Min(value = 1, message = "Minimum 1 enrollment")
    private Integer targetEnrollments;
    
    @NotNull(message = "Target revenue is required")
    @DecimalMin(value = "1000.0", message = "Minimum revenue 1000")
    private BigDecimal targetRevenue;
    
    @NotNull(message = "Start date is required")
    private LocalDate startDate;
    
    @NotNull(message = "End date is required")
    private LocalDate endDate;
    
    
    @Size(max = 500, message = "Remarks cannot exceed 500 characters")
    private String remarks;
    
    // targetId NAHI - Auto-generated
}
