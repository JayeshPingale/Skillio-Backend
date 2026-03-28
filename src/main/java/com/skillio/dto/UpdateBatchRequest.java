package com.skillio.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBatchRequest {
    
    @NotBlank(message = "Batch name is required")
    @Size(min = 3, max = 100, message = "Batch name must be 3-100 characters")
    private String batchName;
    
    @NotNull(message = "Start date is required")
    private LocalDate startDate;
    
    @NotNull(message = "End date is required")
    private LocalDate endDate;
    
    @NotBlank(message = "Timing is required")
    @Size(min = 5, max = 50, message = "Timing must be 5-50 characters")
    private String timing;
    
    @NotBlank(message = "Mode of class is required")
    @Pattern(regexp = "ONLINE|OFFLINE|HYBRID", message = "Mode must be ONLINE, OFFLINE, or HYBRID")
    private String modeOfClass;
    
    @NotBlank(message = "Instructor name is required")
    @Size(min = 2, max = 100, message = "Instructor name must be 2-100 characters")
    private String instructor;
    
    @Size(max = 500, message = "Description must be max 500 characters")
    private String description;
    
    // batchCode CANNOT be updated
    // courseId CANNOT be updated
}
