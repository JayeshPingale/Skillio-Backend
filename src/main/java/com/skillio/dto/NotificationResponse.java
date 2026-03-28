package com.skillio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {

    private Long notificationId;
    private String title;
    private String message;
    private String type;
    private String status;   // UNREAD / READ
    private String priority; // HIGH / MEDIUM / LOW
    private Long relatedId;
    private String relatedEntityType;
    private LocalDateTime createdAt;
}
