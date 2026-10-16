package com.cts.user_service.service;

import com.cts.user_service.dto.AuthResponse;
import com.cts.user_service.dto.LoginRequest;
import com.cts.user_service.dto.RegisterRequest;
import com.cts.user_service.model.User;

import java.util.List;

public interface UserService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    User getUserById(Long id);
    List<User> getAllUsers();                                       // Admin only
    void deleteUser(Long id);                                      // Admin only
    User updateUser(Long id, String name, String email,
                    String role, String contactNumber);            // Admin only
}