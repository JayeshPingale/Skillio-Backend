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
public class UpdateStudentFeesRequest {

    @DecimalMin(value = "0.0", message = "Total fees must be 0 or greater")
    private BigDecimal totalFees;

    // ✅ ADD THIS - Amount Paid field
    @DecimalMin(value = "0.0", message = "Paid amount must be 0 or greater")
    private BigDecimal paidAmount;

    @DecimalMin(value = "0.0", message = "Discount amount must be 0 or greater")
    private BigDecimal discountAmount;

    @Size(max = 255, message = "Discount reason cannot exceed 255 characters")
    private String discountReason;

    private LocalDate dueDate;

    @Size(max = 500, message = "Remarks cannot exceed 500 characters")
    private String remarks;
}
