package com.skillio.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notificationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // Jisko notification jaani chahiye

    @Column(nullable = false)
    private String title; // Short title

    @Column(columnDefinition = "TEXT")
    private String message; // Full message

    @Column(nullable = false)
    private String type;
    // Types: COMMISSION_REQUEST, COMMISSION_APPROVED, COMMISSION_REJECTED,
    //        PAYMENT_DONE, GENERAL

    @Builder.Default
    private String status = "UNREAD"; // UNREAD, READ

    private String priority; // HIGH, MEDIUM, LOW

    private Long relatedId;       // Commission ID, Payment ID etc.
    private String relatedEntityType; // "COMMISSION", "PAYMENT" etc.

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
        if (this.status == null) this.status = "UNREAD";
        if (this.priority == null) this.priority = "MEDIUM";
    }
}
