package com.skillio.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplyDiscountRequest {
    
    @NotNull(message = "Fees ID is required")
    private Long feesId;
    
    @NotNull(message = "Discount amount is required")
    @DecimalMin(value = "0.1", message = "Discount must be greater than 0")
    private BigDecimal discountAmount;
    
    @NotBlank(message = "Discount reason is required")
    private String discountReason; // Scholarship, Merit, etc.
}
