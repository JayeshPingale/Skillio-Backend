package com.skillio.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeadStatusHistoryResponse {
    
    private Long historyId;
    private Long leadId;
    private String leadName;
    
    private String oldStatus;
    private String newStatus;
    
    private Long changedByUserId;
    private String changedByUserName;
    
    private String remarks;
    private LocalDateTime changedAt;
}