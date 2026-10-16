package com.cts.oas.transaction.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;  // ← PUT
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "auction-product-service")
public interface AuctionClient {

    @PutMapping("/api/auctions/{id}/status")  // ← PUT
    void updateAuctionStatus(
            @PathVariable("id") Integer auctionId,
            @RequestParam("status") String status
    );
}