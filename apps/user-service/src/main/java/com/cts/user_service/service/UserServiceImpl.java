package com.cts.user_service.service;

import com.cts.user_service.dto.AuthResponse;
import com.cts.user_service.dto.LoginRequest;
import com.cts.user_service.dto.RegisterRequest;
import com.cts.user_service.model.User;
import com.cts.user_service.jwt.JwtUtil;
import com.cts.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered.");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .contactNumber(request.getContactNumber())
                .build();

        User savedUser = userRepository.save(user);

        return new AuthResponse(null, savedUser.getUserId(), savedUser.getRole().name(), savedUser.getEmail(), savedUser.getName());
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found."));

        // Fixed: pass userId so it gets embedded in the JWT claim
        String token = jwtUtil.generateToken(
                user.getEmail(), user.getRole().name(), user.getUserId());

        return new AuthResponse(token, user.getUserId(), user.getRole().name(), user.getEmail(), user.getName());
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found."));
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found.");
        }
        userRepository.deleteById(id);
    }

    @Override
    public User updateUser(Long id, String name, String email,
                           String role, String contactNumber) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found."));

        if (name          != null && !name.isBlank())  user.setName(name.trim());
        if (contactNumber != null)                      user.setContactNumber(contactNumber.trim());
        if (role          != null && !role.isBlank()) {
            try {
                user.setRole(User.Role.valueOf(role.toUpperCase()));
            } catch (IllegalArgumentException ignored) { }
        }
        // Only update email if changed and not already taken
        if (email != null && !email.isBlank() && !email.equalsIgnoreCase(user.getEmail())) {
            if (userRepository.existsByEmail(email)) {
                throw new RuntimeException("Email already in use.");
            }
            user.setEmail(email.trim().toLowerCase());
        }

        return userRepository.save(user);
    }
}