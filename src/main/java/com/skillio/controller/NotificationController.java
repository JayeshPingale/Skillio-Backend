package com.skillio.controller;

import com.skillio.dto.NotificationResponse;
import com.skillio.entities.Notification;
import com.skillio.entities.User;
import com.skillio.exepection.ResourceNotFoundException;
import com.skillio.repositories.UserRepository;
import com.skillio.services.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    // ✅ Get all notifications for logged-in user
    @GetMapping
    @PreAuthorize("hasAuthority('NOTIFICATION_LIST')")
    public ResponseEntity<List<NotificationResponse>> getMyNotifications(Authentication authentication) {
        User user = extractUser(authentication);
        List<Notification> notifications = notificationService.getNotificationsForUser(user);
        List<NotificationResponse> response = notifications.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    // ✅ Get unread count
    @GetMapping("/unread-count")
    @PreAuthorize("hasAuthority('NOTIFICATION_LIST')")
    public ResponseEntity<Map<String, Long>> getUnreadCount(Authentication authentication) {
        User user = extractUser(authentication);
        Long count = notificationService.getUnreadCount(user);
        Map<String, Long> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }

    // ✅ Mark single notification as read
    @PutMapping("/{notificationId}/mark-read")
    @PreAuthorize("hasAuthority('NOTIFICATION_UPDATE')")
    public ResponseEntity<Map<String, String>> markAsRead(
            @PathVariable Long notificationId,
            Authentication authentication) {
        notificationService.markAsRead(notificationId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Notification marked as read");
        return ResponseEntity.ok(response);
    }

    // ✅ Mark all as read
    @PutMapping("/mark-all-read")
    @PreAuthorize("hasAuthority('NOTIFICATION_UPDATE')")
    public ResponseEntity<Map<String, String>> markAllAsRead(Authentication authentication) {
        User user = extractUser(authentication);
        notificationService.markAllAsRead(user);
        Map<String, String> response = new HashMap<>();
        response.put("message", "All notifications marked as read");
        return ResponseEntity.ok(response);
    }

    // ✅ Delete notification
    @DeleteMapping("/{notificationId}")
    @PreAuthorize("hasAuthority('NOTIFICATION_DELETE')")
    public ResponseEntity<Map<String, String>> deleteNotification(@PathVariable Long notificationId) {
        notificationService.deleteNotification(notificationId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Notification deleted");
        return ResponseEntity.ok(response);
    }

    // ==================== HELPERS ====================

    private User extractUser(Authentication authentication) {
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    private NotificationResponse mapToResponse(Notification n) {
        return NotificationResponse.builder()
                .notificationId(n.getNotificationId())
                .title(n.getTitle())
                .message(n.getMessage())
                .type(n.getType())
                .status(n.getStatus())
                .priority(n.getPriority())
                .relatedId(n.getRelatedId())
                .relatedEntityType(n.getRelatedEntityType())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
