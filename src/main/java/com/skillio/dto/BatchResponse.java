package com.skillio.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchResponse {
    
    private Long batchId;
    private String batchCode;
    private String batchName;
    
    private Long courseId;
    private String courseName;
    
    private LocalDate startDate;
    private LocalDate endDate;
    private String timing;
    private String modeOfClass; // ONLINE, OFFLINE, HYBRID
    
	//    private Integer capacity;

    
    private String instructor;
    private String description;
    private String status; // UPCOMING, ONGOING, COMPLETED, CANCELLED

    private Integer enrolledCount;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
