package com.cts.oas.service;

import com.cts.oas.dto.request.AuctionRequestDTO;
import com.cts.oas.dto.response.AuctionResponseDTO;
import com.cts.oas.util.AuctionStatus;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

public interface AuctionService {
    AuctionResponseDTO createAuction(AuctionRequestDTO dto);

    @Transactional
    AuctionResponseDTO getAuctionById(Integer auctionId);
    AuctionResponseDTO getAuctionByProductId(Integer productId);
    List<AuctionResponseDTO> getAllAuctions();
    List<AuctionResponseDTO> getAuctionsByStatus(AuctionStatus status);
    AuctionResponseDTO updateAuctionStatus(Integer auctionId, AuctionStatus status);
    void deleteAuction(Integer auctionId);
    AuctionResponseDTO updateCurrentBid(Integer auctionId, BigDecimal amount);
}
