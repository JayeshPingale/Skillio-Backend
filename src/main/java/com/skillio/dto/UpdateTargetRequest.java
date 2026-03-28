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
public class UpdateTargetRequest {

    @Min(value = 1, message = "Minimum 1 lead")
    private Integer targetLeads;

    @Min(value = 1, message = "Minimum 1 enrollment")
    private Integer targetEnrollments;

    @DecimalMin(value = "1000.0", message = "Minimum revenue 1000")
    private BigDecimal targetRevenue;

    private LocalDate endDate;

    @Size(max = 500, message = "Remarks cannot exceed 500 characters")
    private String remarks;

    // userId, targetPeriod, startDate cannot be changed
    // achieved values updated separately
}
