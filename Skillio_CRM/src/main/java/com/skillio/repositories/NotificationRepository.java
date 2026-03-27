package com.skillio.repositories;

import com.skillio.entities.Notification;
import com.skillio.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // User ki saari notifications — latest first
    List<Notification> findByUserOrderByCreatedAtDesc(User user);

    // Sirf unread
    List<Notification> findByUserAndStatusOrderByCreatedAtDesc(User user, String status);

    // Unread count
    Long countByUserAndStatus(User user, String status);

    // Mark all read for a user
    @Modifying
    @Query("UPDATE Notification n SET n.status = 'READ' WHERE n.user = :user AND n.status = 'UNREAD'")
    void markAllAsReadForUser(@Param("user") User user);
}
