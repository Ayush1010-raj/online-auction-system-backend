package com.cts.oas.review.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "transaction-service")
public interface TransactionClient {

    @GetMapping("/api/transactions/verify/{buyerId}/{auctionId}")
    Boolean hasCompletedTransaction(
            @PathVariable("buyerId") Long buyerId,
            @PathVariable("auctionId") Long auctionId
    );
}