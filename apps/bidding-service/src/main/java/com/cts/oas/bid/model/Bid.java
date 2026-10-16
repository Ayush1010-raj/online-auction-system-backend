package com.cts.oas.bid.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
@Table(name="Bid")
public class Bid {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="BidID")
	private Integer bidId;
	@Column(name="AuctionID")
	private Integer auctionId;
	@Column(name="BuyerID")
	private Integer buyerId;
	@Column(name="Amount")
	private BigDecimal amount;
	@Column(name="BidTime")
	private LocalDateTime bidTime;
	
	
}
