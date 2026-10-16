package com.cts.user_service.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import com.cts.user_service.model.User;

@Data
public class RegisterRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[@#$%^&+=!]).*$",
            message = "Password must contain at least one digit and one special character"
    )
    private String password;

    @NotNull(message = "Role is required")
    private User.Role role;

    @NotBlank(message = "Contact number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Contact number must be exactly 10 digits")
    private String contactNumber;
}