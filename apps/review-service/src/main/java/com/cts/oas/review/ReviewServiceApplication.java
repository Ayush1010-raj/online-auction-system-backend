package com.cts.oas.review;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
@EnableDiscoveryClient
public class ReviewServiceApplication {

    public static void main(String[] args) {
        loadEnv();
        SpringApplication.run(ReviewServiceApplication.class, args);
    }

    private static void loadEnv() {
        try (var reader = new java.io.BufferedReader(new java.io.FileReader(".env"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                int idx = line.indexOf("=");
                if (idx == -1) continue;
                System.setProperty(line.substring(0, idx).trim(), line.substring(idx + 1).trim());
            }
        } catch (java.io.IOException e) {
            System.out.println(".env file not found");
        }
    }
}