package com.cts.oas.bid.client;

import com.cts.oas.bid.dto.AuctionResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@FeignClient(name = "auction-product-service")
public interface AuctionClient {

    @GetMapping("/api/auctions/{id}")
    AuctionResponseDTO getAuctionById(
            @PathVariable("id") Integer auctionId
    );

    @PutMapping("/api/auctions/{id}/currentBid")
    AuctionResponseDTO updateCurrentBid(
            @PathVariable("id") Integer auctionId,
            @RequestParam("amount") BigDecimal amount
    );
}