//package com.cts.oas.config;
//
//import com.cts.oas.repository.ProductRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.boot.actuate.health.Health;
//import org.springframework.boot.actuate.health.HealthIndicator;
//import org.springframework.stereotype.Component;
//
//@Component("auctionDB")
//@RequiredArgsConstructor
//public class ActuatorConfig implements HealthIndicator {
//
//    private final ProductRepository productRepository;
//
//    @Override
//    public Health health() {
//        try {
//            long count = productRepository.count();
//            return Health.up()
//                    .withDetail("products_in_db", count)
//                    .withDetail("status", "Auction DB reachable")
//                    .build();
//        } catch (Exception e) {
//            return Health.down()
//                    .withDetail("error", e.getMessage())
//                    .build();
//        }
//    }
//}