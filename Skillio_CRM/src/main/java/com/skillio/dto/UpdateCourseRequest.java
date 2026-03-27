package com.skillio.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCourseRequest {
    
    @NotBlank(message = "Course name is required")
    @Size(min = 3, max = 100, message = "Course name must be 3-100 characters")
    private String courseName;
    
    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 500, message = "Description must be 10-500 characters")
    private String description;
    
    @NotBlank(message = "Duration is required")
    @Size(min = 2, max = 50, message = "Duration must be 2-50 characters")
    private String duration; // 6 months, 3 months, etc.
    
    @NotNull(message = "Total fees is required")
    @DecimalMin(value = "100.0", message = "Fees must be at least 100")
    @DecimalMax(value = "999999.99", message = "Fees must be less than 999999.99")
    private BigDecimal totalFees;
    
    private Boolean isActive; // ✅ Add this for status update
    
    // courseId NAHI - From URL
}
