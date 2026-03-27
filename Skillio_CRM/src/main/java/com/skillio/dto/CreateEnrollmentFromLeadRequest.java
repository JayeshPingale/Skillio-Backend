package com.skillio.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateEnrollmentFromLeadRequest {

    @NotNull(message = "Lead ID is required")
    private Long leadId;

    @NotNull(message = "Batch ID is required")
    private Long batchId;

    @NotNull(message = "Course ID is required")
    private Long courseId;

    @NotNull(message = "Enrollment date is required")
    private LocalDate enrollmentDate;

    @NotNull(message = "Total course fees is required")
    private BigDecimal totalCourseFees;

    private Double discountPercentage;
    private Double discountAmount;
    private String discountReason;

    private String remarks;
}
