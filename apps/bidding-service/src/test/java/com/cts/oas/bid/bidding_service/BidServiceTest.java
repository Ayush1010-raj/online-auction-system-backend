//package com.cts.oas.bid.bidding_service;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.when;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.util.Optional;
//
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import com.cts.oas.bid.dto.BidRequestDTO;
//import com.cts.oas.bid.dto.BidResponseDTO;
//import com.cts.oas.bid.exception.AuctionNotFoundException;
//import com.cts.oas.bid.exception.BidValidationException;
//import com.cts.oas.bid.model.Bid;
//import com.cts.oas.bid.repository.BidRepository;
//import com.cts.oas.bid.service.BidService;
//
//@ExtendWith(MockitoExtension.class)
//public class BidServiceTest {
//
//    @Mock
//    private BidRepository bidRepository;
//
//    @Mock
//    private AuctionRepository auctionRepository;
//
//    @InjectMocks
//    private BidService bidService;
//
//    // test methods here
//
//    @Test
//    void placeBid_auctionNotFound_throwsException() {
//
//        // ARRANGE — set up fake data
//        when(auctionRepository.findById(99))
//            .thenReturn(Optional.empty());
//
//        BidRequestDTO request = new BidRequestDTO(99, new BigDecimal("600"));
//
//        // ACT + ASSERT — call method and check exception
//        assertThrows(AuctionNotFoundException.class, () -> {
//            bidService.placeBid(2, request);
//        });
//    }
//    @Test
//    void placeBid_auctionNotOngoing_throwsException() {
//
//        // Create fake auction with Closed status
//        Auction auction = new Auction();
//        auction.setAuctionId(1);
//        auction.setStatus("Closed");
//
//        when(auctionRepository.findById(1))
//            .thenReturn(Optional.of(auction));
//
//        BidRequestDTO request = new BidRequestDTO(1, new BigDecimal("600"));
//
//        assertThrows(BidValidationException.class, () -> {
//            bidService.placeBid(2, request);
//        });
//    }
//    @Test
//    void placeBid_amountTooLow_throwsException() {
//
//        Auction auction = new Auction();
//        auction.setAuctionId(1);
//        auction.setStatus("Ongoing");
//        auction.setStartDate(LocalDateTime.now().minusDays(1));
//        auction.setEndDate(LocalDateTime.now().plusDays(1));
//        auction.setCurrentBid(new BigDecimal("700"));
//
//        when(auctionRepository.findById(1))
//            .thenReturn(Optional.of(auction));
//
//        BidRequestDTO request = new BidRequestDTO(1, new BigDecimal("500"));
//
//        assertThrows(BidValidationException.class, () -> {
//            bidService.placeBid(2, request);
//        });
//    }
//    @Test
//    void placeBid_success_returnsBidResponse() {
//
//        Auction auction = new Auction();
//        auction.setAuctionId(1);
//        auction.setStatus("Ongoing");
//        auction.setStartDate(LocalDateTime.now().minusDays(1));
//        auction.setEndDate(LocalDateTime.now().plusDays(1));
//        auction.setCurrentBid(new BigDecimal("500"));
//
//        Bid savedBid = new Bid();
//        savedBid.setBidId(1);
//        savedBid.setAuctionId(1);
//        savedBid.setBuyerId(2);
//        savedBid.setAmount(new BigDecimal("600"));
//        savedBid.setBidTime(LocalDateTime.now());
//
//        when(auctionRepository.findById(1))
//            .thenReturn(Optional.of(auction));
//        when(bidRepository.findHighestBidByAuctionId(1))
//            .thenReturn(Optional.empty());
//        when(bidRepository.save(any(Bid.class)))
//            .thenReturn(savedBid);
//
//        BidRequestDTO request = new BidRequestDTO(1, new BigDecimal("600"));
//
//        BidResponseDTO response = bidService.placeBid(2, request);
//
//        assertEquals(new BigDecimal("600"), response.getAmount());
//        assertEquals(2, response.getBuyerId());
//        assertEquals("Bid placed successfully! You are the highest bidder.",
//                     response.getMessage());
//    }
//
//}
