package com.cts.oas.bid;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class BiddingServiceApplication {

    public static void main(String[] args) {
        loadEnv();  // load .env before Spring starts
        SpringApplication.run(BiddingServiceApplication.class, args);
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