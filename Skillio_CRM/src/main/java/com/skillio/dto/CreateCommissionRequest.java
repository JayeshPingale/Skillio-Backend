package com.skillio.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCommissionRequest {

    @NotNull(message = "Enrollment ID is required")
    private Long enrollmentId;

    @NotNull(message = "Sales Executive ID is required")
    private Long salesExecutiveId;

    @NotNull(message = "Total course fees is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Total course fees must be greater than 0")
    private BigDecimal totalCourseFees;

    @DecimalMin(value = "0.0", message = "Commission percentage cannot be negative")
    @DecimalMax(value = "100.0", message = "Commission percentage cannot exceed 100")
    private BigDecimal commissionPercentage = new BigDecimal("10.00"); // Default 10%

    @Size(max = 500, message = "Remarks cannot exceed 500 characters")
    private String remarks;

    // status will be auto-set to PENDING
    // eligibleAmount will be auto-calculated
    // eligibilityCondition will be set to FULL_PAYMENT_RECEIVED
}
