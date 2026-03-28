package com.skillio.services.impl;

import com.skillio.entities.Notification;
import com.skillio.entities.User;
import com.skillio.exepection.ResourceNotFoundException;
import com.skillio.repositories.NotificationRepository;
import com.skillio.services.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    @Transactional
    public void createNotification(User recipient, String title, String message,
                                   String type, Long relatedId,
                                   String relatedEntityType, String priority) {
        if (recipient == null) {
            log.error("❌ Cannot create notification: recipient is null");
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
        log.info("✅ Notification created [ID: {}] for user: {} | Type: {}",
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
        notification.setStatus("READ");
        notificationRepository.save(notification);
        log.info("✅ Notification {} marked as READ", notificationId);
    }

    @Override
    @Transactional
    public void markAllAsRead(User user) {
        notificationRepository.markAllAsReadForUser(user);
        log.info("✅ All notifications marked as READ for user: {}", user.getEmail());
    }

    @Override
    @Transactional
    public void deleteNotification(Long notificationId) {
        notificationRepository.deleteById(notificationId);
        log.info("🗑️ Notification {} deleted", notificationId);
    }
}
