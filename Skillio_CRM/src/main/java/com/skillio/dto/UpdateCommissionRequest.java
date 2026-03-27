package com.skillio.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCommissionRequest {

    @DecimalMin(value = "0.0", inclusive = false, message = "Total course fees must be greater than 0")
    private BigDecimal totalCourseFees;

    @DecimalMin(value = "0.0", message = "Commission percentage cannot be negative")
    @DecimalMax(value = "100.0", message = "Commission percentage cannot exceed 100")
    private BigDecimal commissionPercentage;

    @Size(max = 500, message = "Remarks cannot exceed 500 characters")
    private String remarks;

    // Status will be updated via separate endpoints (markAsEligible, markAsPaid)
    // salesExecutive cannot be changed after creation
}
