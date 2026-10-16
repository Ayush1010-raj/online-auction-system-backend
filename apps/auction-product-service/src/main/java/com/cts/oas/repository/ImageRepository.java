package com.cts.oas.repository;

import com.cts.oas.model.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ImageRepository extends JpaRepository<Image, Integer> {
    List<Image> findByProduct_ProductId(Integer productId);

    @Modifying
    @Transactional
    void deleteByProduct_ProductId(Integer productId);
}