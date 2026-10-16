package com.cts.oas.bid.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BidResponseDTO {

	private Integer bidId;
	private Integer auctionId;
	private BigDecimal amount;
	private Integer buyerId;
	private String message;
	private LocalDateTime bidTime;
}
