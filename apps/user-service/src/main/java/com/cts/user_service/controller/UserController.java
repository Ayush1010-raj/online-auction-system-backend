package com.cts.user_service.controller;

import com.cts.user_service.model.User;
import com.cts.user_service.service.UserService;
import com.cts.user_service.service.TokenBlacklistService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final TokenBlacklistService tokenBlacklistService;

    /**
     * Public name lookup — returns only { name } for any authenticated user.
     * Used by review pages to display reviewer/reviewee names.
     */
    @GetMapping("/name/{id}")
    public ResponseEntity<?> getUserName(@PathVariable Long id) {
        try {
            User user = userService.getUserById(id);
            return ResponseEntity.ok(java.util.Map.of("name", user.getName()));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(java.util.Map.of("name", "User #" + id));
        }
    }

    // Get user by ID — own profile or ADMIN
    @GetMapping("/{id}")
    public ResponseEntity<?> getUser(
            @PathVariable Long id,
            @RequestHeader("X-User-Id")   String currentUserId,
            @RequestHeader("X-User-Role") String currentUserRole) {

        User requestedUser = userService.getUserById(id);

        if (currentUserRole.equals("ADMIN") ||
                Long.parseLong(currentUserId) == requestedUser.getUserId()) {
            return ResponseEntity.ok(requestedUser);
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("Access Denied: You can only view your own profile.");
    }

    // Logout — blacklist the token
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body("No token provided.");
        }

        String token = authHeader.substring(7);
        tokenBlacklistService.blacklist(token);
        return ResponseEntity.ok("Logged out successfully.");
    }

    // Admin only — get all users
    @GetMapping("/admin/all")
    public ResponseEntity<?> getAllUsers(
            @RequestHeader("X-User-Role") String role) {

        if (!role.equals("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access Denied: Admins only.");
        }
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // Admin only — update user
    @PutMapping("/admin/{id}")
    public ResponseEntity<?> updateUser(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @RequestHeader("X-User-Role") String role) {

        if (!role.equals("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access Denied: Admins only.");
        }
        try {
            User updated = userService.updateUser(
                    id,
                    body.get("name"),
                    body.get("email"),
                    body.get("role"),
                    body.get("contactNumber")
            );
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Admin only — delete user
    @DeleteMapping("/admin/{id}")
    public ResponseEntity<?> deleteUser(
            @PathVariable Long id,
            @RequestHeader("X-User-Role") String role) {

        if (!role.equals("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access Denied: Admins only.");
        }
        userService.deleteUser(id);
        return ResponseEntity.ok("User deleted successfully.");
    }
}