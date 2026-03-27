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
public class CreateStudentFeesRequest {

    @NotNull(message = "Enrollment ID is required")
    private Long enrollmentId;

    @NotNull(message = "Total fees is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Total fees must be greater than 0")
    private BigDecimal totalFees;

    @DecimalMin(value = "0.0", message = "Discount amount cannot be negative")
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Size(max = 255, message = "Discount reason cannot exceed 255 characters")
    private String discountReason;

    @NotNull(message = "Due date is required")
    @Future(message = "Due date must be in the future")
    private LocalDate dueDate;

    @Size(max = 500, message = "Remarks cannot exceed 500 characters")
    private String remarks;

    // paidAmount is auto-set to 0
    // balanceAmount is auto-calculated
    // paymentStatus is auto-calculated
}
