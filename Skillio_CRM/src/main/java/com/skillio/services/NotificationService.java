package com.skillio.services;

import com.skillio.entities.Notification;
import com.skillio.entities.User;
import java.util.List;

public interface NotificationService {

    void createNotification(User recipient, String title, String message,
                            String type, Long relatedId,
                            String relatedEntityType, String priority);

    List<Notification> getNotificationsForUser(User user);

    Long getUnreadCount(User user);

    void markAsRead(Long notificationId);

    void markAllAsRead(User user);

    void deleteNotification(Long notificationId);
}