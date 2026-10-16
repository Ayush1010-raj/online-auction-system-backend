package com.cts.oas.bid.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Notification")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "NotificationID")
    private Long notificationId;

    @Column(name = "UserID", nullable = false)
    private Integer userId;

    @Column(name = "AuctionID", nullable = false)
    private Integer auctionId;

    @Column(name = "Message", nullable = false, length = 500)
    private String message;

    /** false = unread, true = read */
    @Column(name = "IsRead", nullable = false)
    private boolean read;

    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt;
}
