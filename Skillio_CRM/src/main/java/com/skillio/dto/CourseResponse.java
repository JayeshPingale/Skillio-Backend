package com.skillio.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseResponse {
    
    private Long courseId;
    private String courseName;
    private String description;
    private String duration;
    private BigDecimal totalFees;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
