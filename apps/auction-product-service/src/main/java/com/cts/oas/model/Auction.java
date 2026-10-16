package com.cts.oas.model;

import com.cts.oas.util.AuctionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "auction")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Auction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AuctionID")
    private Integer auctionId;

    @OneToOne
    @JoinColumn(name = "ProductID", nullable = false, unique = true)
    private Product product;

    @Column(name = "StartDate", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "EndDate", nullable = false)
    private LocalDateTime endDate;

    @Column(name = "CurrentBid", precision = 10, scale = 2)
    private BigDecimal currentBid;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", length = 50)
    private AuctionStatus status;
}