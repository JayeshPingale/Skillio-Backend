package com.skillio.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignLeadRequest {
    
    @NotNull(message = "Lead ID is required")
    private Long leadId;
    
    @NotNull(message = "Sales Executive ID is required")
    private Long salesExecutiveId;
    
    private String remarks; // Why assigning this lead
    
    // assignedTo NAHI - Will be set from salesExecutiveId
}
	