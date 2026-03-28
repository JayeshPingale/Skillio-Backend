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
public class CreateEnrollmentRequest {
    
    @NotNull(message = "Student ID is required")
    private Long studentId;
    
    @NotNull(message = "Batch ID is required")
    private Long batchId;
    
    @NotNull(message = "Course ID is required")
    private Long courseId;
    
    @NotNull(message = "Enrollment date is required")
    private LocalDate enrollmentDate;
    
    @NotNull(message = "Total course fees is required")
    private BigDecimal totalCourseFees;
    
    private String remarks;
    
    private Double discountPercentage;
    private Double discountAmount;
    private String discountReason;

}
