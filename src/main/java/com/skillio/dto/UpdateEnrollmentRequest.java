package com.skillio.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEnrollmentRequest {
    
    @NotNull(message = "Total course fees is required")
    private BigDecimal totalCourseFees;
    
    private String status; // ACTIVE, COMPLETED, CANCELLED, ON_HOLD
    private Double discountPercentage;
    private Double discountAmount;
    private String discountReason;

    private String remarks;
    

}
