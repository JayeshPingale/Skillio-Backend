package com.skillio.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateBatchRequest {
    
    @NotBlank(message = "Batch code is required")
    @Size(min = 3, max = 50)
    private String batchCode; // Daffodil25A, Java_2025_Jan
    
    @NotBlank(message = "Batch name is required")
    @Size(min = 3, max = 100)
    private String batchName;
    
    @NotNull(message = "Course ID is required")
    private Long courseId;
    
    @NotNull(message = "Start date is required")
    @FutureOrPresent(message = "Start date must be today or future")
    private LocalDate startDate;
    
    @NotNull(message = "End date is required")
    private LocalDate endDate;
    
    @NotBlank(message = "Timing is required")
    private String timing; // 10:00 AM - 12:00 PM
    
    @NotBlank(message = "Mode of class is required")
    private String modeOfClass; // ONLINE, OFFLINE, HYBRID
    
//    @NotNull(message = "Capacity is required")
//    @Min(value = 5, message = "Minimum capacity is 5")
//    @Max(value = 100, message = "Maximum capacity is 100")
//    private Integer capacity;
    
    @NotBlank(message = "Instructor name is required")
    private String instructor;
    
    private String description;
    
    // batchId NAHI - Auto-generated
    // status NAHI - Default "UPCOMING"
    // enrolledCount NAHI - Default 0
}
