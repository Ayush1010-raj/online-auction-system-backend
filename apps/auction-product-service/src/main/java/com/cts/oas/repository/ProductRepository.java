package com.cts.oas.repository;

import com.cts.oas.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    List<Product> findBySellerId(Integer sellerId);
    List<Product> findByCategory(String category);
    List<Product> findByStatus(String status);
}
