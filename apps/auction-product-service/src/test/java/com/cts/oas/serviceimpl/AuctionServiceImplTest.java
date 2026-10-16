package com.cts.oas.serviceimpl;

import com.cts.oas.dto.request.AuctionRequestDTO;
import com.cts.oas.dto.response.AuctionResponseDTO;
import com.cts.oas.exception.BadRequestException;
import com.cts.oas.exception.ResourceNotFoundException;
import com.cts.oas.model.Auction;
import com.cts.oas.model.Product;
import com.cts.oas.repository.AuctionRepository;
import com.cts.oas.repository.ProductRepository;
import com.cts.oas.util.AuctionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuctionServiceImpl Tests")
class AuctionServiceImplTest {

    @Mock private AuctionRepository auctionRepo;
    @Mock private ProductRepository productRepo;

    @InjectMocks
    private AuctionServiceImpl auctionService;

    private Product product;
    private Auction auction;
    private AuctionRequestDTO requestDTO;

    private final LocalDateTime START = LocalDateTime.now().plusDays(1);
    private final LocalDateTime END   = LocalDateTime.now().plusDays(30);

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .productId(1)
                .sellerId(1)
                .title("MacBook Pro M2")
                .startPrice(new BigDecimal("1200.00"))
                .status("AVAILABLE")
                .build();

        auction = Auction.builder()
                .auctionId(1)
                .product(product)
                .startDate(START)
                .endDate(END)
                .currentBid(new BigDecimal("1200.00"))
                .status(AuctionStatus.ACTIVE)
                .build();

        requestDTO = new AuctionRequestDTO();
        requestDTO.setProductId(1);
        requestDTO.setStartDate(START);
        requestDTO.setEndDate(END);
    }

    // CREATE AUCTION
    @Nested
    @DisplayName("createAuction()")
    class CreateAuction {

        @Test
        @DisplayName("Should create auction successfully")
        void shouldCreateAuctionSuccessfully() {
            when(productRepo.findById(1)).thenReturn(Optional.of(product));
            when(auctionRepo.findByProduct_ProductId(1)).thenReturn(Optional.empty());
            when(auctionRepo.save(any(Auction.class))).thenReturn(auction);
            when(productRepo.save(any(Product.class))).thenReturn(product);

            AuctionResponseDTO response = auctionService.createAuction(requestDTO);

            assertThat(response).isNotNull();
            assertThat(response.getAuctionId()).isEqualTo(1);
            assertThat(response.getStatus()).isEqualTo("ACTIVE");
            assertThat(response.getCurrentBid()).isEqualByComparingTo("1200.00");
            verify(productRepo).save(argThat(p -> "IN_AUCTION".equals(p.getStatus())));
        }

        @Test
        @DisplayName("Should throw BadRequestException when end date is before start date")
        void shouldThrowWhenEndDateBeforeStartDate() {
            requestDTO.setStartDate(LocalDateTime.now().plusDays(10));
            requestDTO.setEndDate(LocalDateTime.now().plusDays(1));

            assertThatThrownBy(() -> auctionService.createAuction(requestDTO))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("End date must be after start date");

            verify(auctionRepo, never()).save(any());
        }

        @Test
        @DisplayName("Should throw BadRequestException when auction already exists for product")
        void shouldThrowWhenDuplicateAuction() {
            when(productRepo.findById(1)).thenReturn(Optional.of(product));
            when(auctionRepo.findByProduct_ProductId(1)).thenReturn(Optional.of(auction));

            assertThatThrownBy(() -> auctionService.createAuction(requestDTO))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("auction already exists");

            verify(auctionRepo, never()).save(any());
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when product not found")
        void shouldThrowWhenProductNotFound() {
            when(productRepo.findById(1)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> auctionService.createAuction(requestDTO))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Product not found");
        }
    }

    // GET AUCTION BY ID
    @Nested
    @DisplayName("getAuctionById()")
    class GetAuctionById {

        @Test
        @DisplayName("Should return auction when found")
        void shouldReturnAuctionWhenFound() {
            when(auctionRepo.findById(1)).thenReturn(Optional.of(auction));

            AuctionResponseDTO response = auctionService.getAuctionById(1);

            assertThat(response.getAuctionId()).isEqualTo(1);
            assertThat(response.getProductTitle()).isEqualTo("MacBook Pro M2");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when auction not found")
        void shouldThrowWhenAuctionNotFound() {
            when(auctionRepo.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> auctionService.getAuctionById(999))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("999");
        }
    }

    // GET AUCTIONS BY STATUS
    @Nested
    @DisplayName("getAuctionsByStatus()")
    class GetAuctionsByStatus {

        @Test
        @DisplayName("Should return active auctions")
        void shouldReturnActiveAuctions() {
            when(auctionRepo.findByStatus(AuctionStatus.ACTIVE)).thenReturn(List.of(auction));

            List<AuctionResponseDTO> result = auctionService.getAuctionsByStatus(AuctionStatus.ACTIVE);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getStatus()).isEqualTo("ACTIVE");
        }

        @Test
        @DisplayName("Should return empty list when no auctions match status")
        void shouldReturnEmptyWhenNoMatch() {
            when(auctionRepo.findByStatus(AuctionStatus.SOLD)).thenReturn(List.of());

            List<AuctionResponseDTO> result = auctionService.getAuctionsByStatus(AuctionStatus.SOLD);

            assertThat(result).isEmpty();
        }
    }

    // UPDATE AUCTION STATUS
    @Nested
    @DisplayName("updateAuctionStatus()")
    class UpdateAuctionStatus {

        @Test
        @DisplayName("Should mark auction SOLD and sync product status to SOLD")
        void shouldMarkAsSoldAndSyncProduct() {
            when(auctionRepo.findById(1)).thenReturn(Optional.of(auction));
            when(auctionRepo.save(any(Auction.class))).thenAnswer(i -> i.getArgument(0));
            when(productRepo.save(any(Product.class))).thenReturn(product);

            AuctionResponseDTO response = auctionService.updateAuctionStatus(1, AuctionStatus.SOLD);

            assertThat(response.getStatus()).isEqualTo("SOLD");
            verify(productRepo).save(argThat(p -> "SOLD".equals(p.getStatus())));
        }

        @Test
        @DisplayName("Should mark auction CANCELLED and revert product to AVAILABLE")
        void shouldCancelAuctionAndRevertProduct() {
            when(auctionRepo.findById(1)).thenReturn(Optional.of(auction));
            when(auctionRepo.save(any(Auction.class))).thenAnswer(i -> i.getArgument(0));
            when(productRepo.save(any(Product.class))).thenReturn(product);

            AuctionResponseDTO response = auctionService.updateAuctionStatus(1, AuctionStatus.CANCELLED);

            assertThat(response.getStatus()).isEqualTo("CANCELLED");
            verify(productRepo).save(argThat(p -> "AVAILABLE".equals(p.getStatus())));
        }

        @Test
        @DisplayName("Should update to CLOSED without changing product status")
        void shouldCloseAuctionWithoutChangingProduct() {
            when(auctionRepo.findById(1)).thenReturn(Optional.of(auction));
            when(auctionRepo.save(any(Auction.class))).thenAnswer(i -> i.getArgument(0));

            AuctionResponseDTO response = auctionService.updateAuctionStatus(1, AuctionStatus.CLOSED);

            assertThat(response.getStatus()).isEqualTo("CLOSED");
            verify(productRepo, never()).save(any());
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when auction not found")
        void shouldThrowWhenAuctionNotFound() {
            when(auctionRepo.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> auctionService.updateAuctionStatus(999, AuctionStatus.SOLD))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("999");
        }
    }

    // DELETE AUCTION
    @Nested
    @DisplayName("deleteAuction()")
    class DeleteAuction {

        @Test
        @DisplayName("Should delete auction successfully")
        void shouldDeleteAuctionSuccessfully() {
            when(auctionRepo.findById(1)).thenReturn(Optional.of(auction));
            doNothing().when(auctionRepo).delete(auction);

            assertThatCode(() -> auctionService.deleteAuction(1)).doesNotThrowAnyException();
            verify(auctionRepo, times(1)).delete(auction);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when deleting non-existent auction")
        void shouldThrowWhenDeletingNonExistentAuction() {
            when(auctionRepo.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> auctionService.deleteAuction(999))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(auctionRepo, never()).delete(any());
        }
    }
}