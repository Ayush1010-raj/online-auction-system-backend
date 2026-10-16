package com.cts.oas.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "product")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ProductID")
    private Integer productId;

    @Column(name = "SellerID", nullable = false)
    private Integer sellerId;

    @Column(name = "Title", nullable = false, length = 255)
    private String title;

    @Column(name = "Description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "StartPrice", nullable = false, precision = 10, scale = 2)
    private BigDecimal startPrice;

    @Column(name = "Category", length = 100)
    private String category;

    @Column(name = "Status", length = 50)
    private String status; // e.g. AVAILABLE, SOLD, REMOVED

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Image> images;

    @OneToOne(mappedBy = "product", cascade = CascadeType.ALL)
    private Auction auction;
}