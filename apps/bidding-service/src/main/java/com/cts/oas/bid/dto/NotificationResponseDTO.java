package com.cts.oas.bid.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponseDTO {
    private Long   notificationId;
    private Integer userId;
    private Integer auctionId;
    private String  message;
    private boolean read;
    private LocalDateTime createdAt;
}
