package com.skillio.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCourseRequest {
    
    @NotBlank(message = "Course name is required")
    @Size(min = 3, max = 100)
    private String courseName;
    
    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 500)
    private String description;
    
    @NotBlank(message = "Duration is required")
    private String duration; // 6 months, 3 months, etc.
    
    @NotNull(message = "Total fees is required")
    @DecimalMin(value = "100.0", message = "Fees must be at least 100")
    @DecimalMax(value = "999999.99", message = "Fees must be less than 999999.99")
    private BigDecimal totalFees;
    
    // courseId NAHI - Auto-generated
}
