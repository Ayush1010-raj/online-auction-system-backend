package com.cts.oas.bid.repository;

import com.cts.oas.bid.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /** All unread notifications for a user, newest first */
    List<Notification> findByUserIdAndReadFalseOrderByCreatedAtDesc(Integer userId);

    /** All notifications for a user (read + unread), newest first */
    List<Notification> findByUserIdOrderByCreatedAtDesc(Integer userId);
}
