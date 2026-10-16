package com.cts.oas.controller;

import com.cts.oas.dto.request.AuctionRequestDTO;
import com.cts.oas.dto.response.AuctionResponseDTO;
import com.cts.oas.exception.BadRequestException;
import com.cts.oas.exception.GlobalExceptionHandler;
import com.cts.oas.exception.ResourceNotFoundException;
import com.cts.oas.service.AuctionService;
import com.cts.oas.util.AuctionStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuctionController Tests")
class AuctionControllerTest {

    @Mock private AuctionService auctionService;

    @InjectMocks private AuctionController auctionController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private AuctionResponseDTO responseDTO;
    private AuctionRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(auctionController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        // Register JavaTimeModule for LocalDateTime serialization in tests
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end   = LocalDateTime.now().plusDays(30);

        responseDTO = new AuctionResponseDTO();
        responseDTO.setAuctionId(1);
        responseDTO.setProductId(1);
        responseDTO.setProductTitle("MacBook Pro M2");
        responseDTO.setStartDate(start);
        responseDTO.setEndDate(end);
        responseDTO.setCurrentBid(new BigDecimal("1200.00"));
        responseDTO.setStatus("ACTIVE");

        requestDTO = new AuctionRequestDTO();
        requestDTO.setProductId(1);
        requestDTO.setStartDate(start);
        requestDTO.setEndDate(end);
    }

    // POST /api/auctions
    @Nested
    @DisplayName("POST /api/auctions")
    class CreateAuction {

        @Test
        @DisplayName("Should return 201 when auction created successfully")
        void shouldReturn201WhenCreated() throws Exception {
            when(auctionService.createAuction(any())).thenReturn(responseDTO);

            mockMvc.perform(post("/api/auctions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.auctionId").value(1))
                    .andExpect(jsonPath("$.status").value("ACTIVE"))
                    .andExpect(jsonPath("$.productTitle").value("MacBook Pro M2"));
        }

        @Test
        @DisplayName("Should return 400 when productId is missing")
        void shouldReturn400WhenProductIdMissing() throws Exception {
            requestDTO.setProductId(null);

            mockMvc.perform(post("/api/auctions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.productId").value("Product ID is required"));
        }

        @Test
        @DisplayName("Should return 400 when duplicate auction for same product")
        void shouldReturn400WhenDuplicateAuction() throws Exception {
            when(auctionService.createAuction(any()))
                    .thenThrow(new BadRequestException("An auction already exists for product id: 1"));

            mockMvc.perform(post("/api/auctions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(containsString("already exists")));
        }

        @Test
        @DisplayName("Should return 404 when product not found")
        void shouldReturn404WhenProductNotFound() throws Exception {
            when(auctionService.createAuction(any()))
                    .thenThrow(new ResourceNotFoundException("Product not found with id: 1"));

            mockMvc.perform(post("/api/auctions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isNotFound());
        }
    }

    // GET /api/auctions
    @Nested
    @DisplayName("GET /api/auctions")
    class GetAllAuctions {

        @Test
        @DisplayName("Should return 200 with list of auctions")
        void shouldReturn200WithAuctions() throws Exception {
            when(auctionService.getAllAuctions()).thenReturn(List.of(responseDTO));

            mockMvc.perform(get("/api/auctions"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].auctionId").value(1));
        }

        @Test
        @DisplayName("Should return 200 with empty list")
        void shouldReturn200WithEmptyList() throws Exception {
            when(auctionService.getAllAuctions()).thenReturn(List.of());

            mockMvc.perform(get("/api/auctions"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }
    }

    // GET /api/auctions/status/{status}
    @Nested
    @DisplayName("GET /api/auctions/status/{status}")
    class GetByStatus {

        @Test
        @DisplayName("Should return 200 with ACTIVE auctions")
        void shouldReturnActiveAuctions() throws Exception {
            when(auctionService.getAuctionsByStatus(AuctionStatus.ACTIVE))
                    .thenReturn(List.of(responseDTO));

            mockMvc.perform(get("/api/auctions/status/ACTIVE"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].status").value("ACTIVE"));
        }
    }

    // PATCH /api/auctions/{id}/status
    @Nested
    @DisplayName("PATCH /api/auctions/{id}/status")
    class UpdateStatus {

        @Test
        @DisplayName("Should return 200 when status updated to SOLD")
        void shouldReturn200WhenStatusUpdatedToSold() throws Exception {
            responseDTO.setStatus("SOLD");
            when(auctionService.updateAuctionStatus(1, AuctionStatus.SOLD)).thenReturn(responseDTO);

            mockMvc.perform(patch("/api/auctions/1/status")
                            .param("status", "SOLD"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("SOLD"));
        }

        @Test
        @DisplayName("Should return 404 when auction not found")
        void shouldReturn404WhenNotFound() throws Exception {
            when(auctionService.updateAuctionStatus(eq(999), any()))
                    .thenThrow(new ResourceNotFoundException("Auction not found with id: 999"));

            mockMvc.perform(patch("/api/auctions/999/status")
                            .param("status", "SOLD"))
                    .andExpect(status().isNotFound());
        }
    }

    // DELETE /api/auctions/{id}
    @Nested
    @DisplayName("DELETE /api/auctions/{id}")
    class DeleteAuction {

        @Test
        @DisplayName("Should return 204 when auction deleted")
        void shouldReturn204WhenDeleted() throws Exception {
            doNothing().when(auctionService).deleteAuction(1);

            mockMvc.perform(delete("/api/auctions/1"))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("Should return 404 when auction not found")
        void shouldReturn404WhenNotFound() throws Exception {
            doThrow(new ResourceNotFoundException("Auction not found with id: 999"))
                    .when(auctionService).deleteAuction(999);

            mockMvc.perform(delete("/api/auctions/999"))
                    .andExpect(status().isNotFound());
        }
    }
}