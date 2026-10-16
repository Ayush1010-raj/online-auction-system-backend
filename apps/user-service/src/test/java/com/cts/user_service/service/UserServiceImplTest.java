package com.cts.user_service.service;

import com.cts.user_service.dto.LoginRequest;
import com.cts.user_service.dto.RegisterRequest;
import com.cts.user_service.dto.AuthResponse;
import com.cts.user_service.jwt.JwtUtil;
import com.cts.user_service.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.cts.user_service.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
// @ExtendWith → tells JUnit to use Mockito to create mocks automatically
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserServiceImpl userService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User user;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setName("Ravi Kumar");
        registerRequest.setEmail("ravi@gmail.com");
        registerRequest.setPassword("ravi@123");
        registerRequest.setRole(User.Role.SELLER);
        registerRequest.setContactNumber("9876543210");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("ravi@gmail.com");
        loginRequest.setPassword("ravi@123");

        user = User.builder()
                .userId(1L)
                .name("Ravi Kumar")
                .email("ravi@gmail.com")
                .password("$2a$10$hashedpassword")
                .role(User.Role.SELLER)
                .contactNumber("9876543210")
                .build();
    }

    @Test
    void register_ShouldReturnResponseWithoutToken_WhenValidRequest() {
        when(userRepository.existsByEmail("ravi@gmail.com")).thenReturn(false);
        when(passwordEncoder.encode("ravi@123")).thenReturn("$2a$10$hashedpassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        AuthResponse response = userService.register(registerRequest);

        assertNotNull(response);
        assertNull(response.getToken());  // ← Token should be null after register
        assertEquals(1L, response.getUserId());
        assertEquals("SELLER", response.getRole());
        assertEquals("ravi@gmail.com", response.getEmail());

        verify(userRepository, times(1)).save(any(User.class));
        
        verify(jwtUtil, never()).generateToken(any(), any(), any());
    }

    @Test
    void register_ShouldThrowException_WhenEmailAlreadyExists() {
        when(userRepository.existsByEmail("ravi@gmail.com")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> userService.register(registerRequest));

        assertEquals("Email already registered.", ex.getMessage());

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_ShouldReturnToken_WhenValidCredentials() {
        when(userRepository.findByEmail("ravi@gmail.com")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken("ravi@gmail.com", "SELLER", 1L)).thenReturn("mock.jwt.token");

        AuthResponse response = userService.login(loginRequest);

        assertNotNull(response);
        assertEquals("mock.jwt.token", response.getToken());
        assertEquals(1L, response.getUserId());
        assertEquals("SELLER", response.getRole());
        assertEquals("ravi@gmail.com", response.getEmail());
    }

    @Test
    void login_ShouldThrowException_WhenUserNotFound() {
        when(userRepository.findByEmail("ravi@gmail.com")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> userService.login(loginRequest));

        assertEquals("User not found.", ex.getMessage());
    }

    @Test
    void login_ShouldThrowException_WhenPasswordIsWrong() {
        doThrow(new BadCredentialsException("Bad credentials"))
                .when(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        assertThrows(BadCredentialsException.class,
                () -> userService.login(loginRequest));
    }

    @Test
    void getUserById_ShouldReturnUser_WhenExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User result = userService.getUserById(1L);

        assertNotNull(result);
        assertEquals("Ravi Kumar", result.getName());
        assertEquals("ravi@gmail.com", result.getEmail());
    }

    @Test
    void getUserById_ShouldThrowException_WhenNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> userService.getUserById(99L));

        assertEquals("User not found.", ex.getMessage());
    }


    @Test
    void getAllUsers_ShouldReturnList() {
        when(userRepository.findAll()).thenReturn(List.of(user));

        List<User> users = userService.getAllUsers();

        assertEquals(1, users.size());
        assertEquals("Ravi Kumar", users.get(0).getName());
    }

    @Test
    void deleteUser_ShouldDelete_WhenUserExists() {
        when(userRepository.existsById(1L)).thenReturn(true);

        assertDoesNotThrow(() -> userService.deleteUser(1L));

        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteUser_ShouldThrowException_WhenNotFound() {
        when(userRepository.existsById(99L)).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> userService.deleteUser(99L));

        assertEquals("User not found.", ex.getMessage());
        verify(userRepository, never()).deleteById(any());
    }
}
