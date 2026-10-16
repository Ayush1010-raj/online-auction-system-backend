package com.cts.oas.dto.response;

import lombok.Data;

@Data
public class UserResponseDTO {
    private Integer userId;
    private String name;
    private String email;
    private String role;   // BUYER, SELLER, ADMIN
    private String contactNumber;
}