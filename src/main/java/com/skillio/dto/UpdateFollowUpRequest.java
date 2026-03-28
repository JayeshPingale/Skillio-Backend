package com.skillio.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateFollowUpRequest {
    
    @NotNull(message = "Follow-up date is required")
    private LocalDate followUpDate;
    
    @NotBlank(message = "Follow-up type is required")
    @Pattern(regexp = "CALL|EMAIL|VISIT|WHATSAPP", message = "Follow-up type must be CALL, EMAIL, VISIT, or WHATSAPP")
    private String followUpType;
    
    @NotBlank(message = "Notes are required")
    @Size(min = 5, max = 500, message = "Notes must be between 5 and 500 characters")
    private String notes;
    
    @NotNull(message = "Next follow-up date is required")
    private LocalDate nextFollowUpDate;
    
    @NotBlank(message = "Status is required")
    @Pattern(regexp = "SCHEDULED|COMPLETED|MISSED", message = "Status must be SCHEDULED, COMPLETED, or MISSED")
    private String status;
}
