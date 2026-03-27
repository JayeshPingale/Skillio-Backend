package com.skillio.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTargetAchievementRequest {

    @Min(value = 0, message = "Achieved leads cannot be negative")
    private Integer achievedLeads;

    @Min(value = 0, message = "Achieved enrollments cannot be negative")
    private Integer achievedEnrollments;

    @DecimalMin(value = "0.0", message = "Achieved revenue cannot be negative")
    private BigDecimal achievedRevenue;
}
