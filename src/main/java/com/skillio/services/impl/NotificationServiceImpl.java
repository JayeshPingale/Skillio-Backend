package com.skillio.services.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillio.entities.Notification;
import com.skillio.entities.User;
import com.skillio.exepection.ResourceNotFoundException;
import com.skillio.repositories.NotificationRepository;
import com.skillio.services.AuditLogService;
import com.skillio.services.NotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public void createNotification(User recipient, String title, String message,
                                   String type, Long relatedId,
                                   String relatedEntityType, String priority) {
        if (recipient == null) {
            log.error("Cannot create notification: recipient is null");
            return;
        }

        Notification notification = Notification.builder()
                .user(recipient)
                .title(title)
                .message(message)
                .type(type)
                .status("UNREAD")
                .relatedId(relatedId)
                .relatedEntityType(relatedEntityType)
                .priority(priority != null ? priority : "MEDIUM")
                .build();

        Notification saved = notificationRepository.save(notification);
        auditLogService.createAuditLog(
                "Notification",
                saved.getNotificationId(),
                "CREATE",
                null,
                createSafeNotificationAuditData(saved),
                recipient
        );
        log.info("Notification created [ID: {}] for user: {} | Type: {}",
                saved.getNotificationId(), recipient.getEmail(), type);
    }

    @Override
    public List<Notification> getNotificationsForUser(User user) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Override
    public Long getUnreadCount(User user) {
        return notificationRepository.countByUserAndStatus(user, "UNREAD");
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found: " + notificationId));

        Map<String, Object> oldData = createSafeNotificationAuditData(notification);
        notification.setStatus("READ");
        Notification updated = notificationRepository.save(notification);

        auditLogService.createAuditLog(
                "Notification",
                notificationId,
                "MARK_AS_READ",
                oldData,
                createSafeNotificationAuditData(updated),
                notification.getUser()
        );
        log.info("Notification {} marked as READ", notificationId);
    }

    @Override
    @Transactional
    public void markAllAsRead(User user) {
        notificationRepository.markAllAsReadForUser(user);
        auditLogService.createAuditLog(
                "Notification",
                user.getUserId(),
                "MARK_ALL_AS_READ",
                null,
                Map.of("userId", user.getUserId(), "email", user.getEmail(), "status", "READ"),
                user
        );
        log.info("All notifications marked as READ for user: {}", user.getEmail());
    }

    @Override
    @Transactional
    public void deleteNotification(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found: " + notificationId));

        auditLogService.createAuditLog(
                "Notification",
                notificationId,
                "DELETE",
                createSafeNotificationAuditData(notification),
                null,
                notification.getUser()
        );
        notificationRepository.delete(notification);
        log.info("Notification {} deleted", notificationId);
    }

    private Map<String, Object> createSafeNotificationAuditData(Notification notification) {
        Map<String, Object> data = new HashMap<>();
        data.put("notificationId", notification.getNotificationId());
        data.put("userId", notification.getUser().getUserId());
        data.put("userEmail", notification.getUser().getEmail());
        data.put("title", notification.getTitle());
        data.put("type", notification.getType());
        data.put("status", notification.getStatus());
        data.put("priority", notification.getPriority());
        data.put("relatedId", notification.getRelatedId());
        data.put("relatedEntityType", notification.getRelatedEntityType());
        data.put("createdAt", notification.getCreatedAt());
        return data;
    }
}
