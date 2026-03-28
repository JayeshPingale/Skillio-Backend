package com.skillio.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeadSourceResponse {
    
    private Long sourceId;
    private String name; // Instagram, Facebook, Website, Referral, Email, Cold_Call
    private String channel; // SOCIAL_MEDIA, ORGANIC, PAID_ADS, REFERRAL, COLD_CALLING
    private String description;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
