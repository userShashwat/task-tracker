package com.example.project1.Controller;

import com.example.project1.Payload.Request.LoginRequest;
import com.example.project1.Payload.Request.RegisterRequest;
import com.example.project1.Payload.Response.AuthenticationResponse;
import com.example.project1.Service.AuthenticationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private AuthenticationController authenticationController;

    private ObjectMapper objectMapper = new ObjectMapper();
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authenticationController).build();

        registerRequest = new RegisterRequest();
        registerRequest.setName("John Doe");
        registerRequest.setEmail("john@example.com");
        registerRequest.setPassword("password123");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("john@example.com");
        loginRequest.setPassword("password123");
    }

    // ==================== REGISTER TESTS ====================

    @Test
    void register_Success_ReturnsOk() throws Exception {
        // Arrange
        when(authenticationService.register(any(RegisterRequest.class)))
                .thenReturn("Registration successful! Please check your email.");

        // Act & Assert
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("Registration successful! Please check your email."));
    }

    @Test
    void register_WhenEmailExists_ReturnsBadRequest() throws Exception {
        // Arrange - Negative: Email already exists
        when(authenticationService.register(any(RegisterRequest.class)))
                .thenThrow(new RuntimeException("User already exists with email: john@example.com"));

        // Act & Assert
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("User already exists with email: john@example.com"));
    }

    // ==================== LOGIN TESTS ====================

    @Test
    void login_Success_ReturnsToken() throws Exception {
        // Arrange
        AuthenticationResponse response = AuthenticationResponse.builder()
                .token("jwt-token-123")
                .message("Logged in successfully")
                .build();

        when(authenticationService.login(any(LoginRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token-123"))
                .andExpect(jsonPath("$.message").value("Logged in successfully"));
    }

    @Test
    void login_InvalidCredentials_ReturnsUnauthorized() throws Exception {
        // Arrange - Negative: Invalid credentials
        when(authenticationService.login(any(LoginRequest.class)))
                .thenThrow(new RuntimeException("Invalid email or password"));

        // Act & Assert
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid email or password"));
    }


    @Test
    void confirmEmail_Success_ReturnsMessage() throws Exception {
        // Arrange
        when(authenticationService.confirmToken("valid-token"))
                .thenReturn("Verification successful! You can now log in.");

        // Act & Assert
        mockMvc.perform(get("/auth/confirm")
                        .param("token", "valid-token"))
                .andExpect(status().isOk())
                .andExpect(content().string("Verification successful! You can now log in."));
    }

    @Test
    void confirmEmail_TokenNotFound_ReturnsBadRequest() throws Exception {
        // Arrange - Negative: Invalid token
        when(authenticationService.confirmToken("invalid-token"))
                .thenThrow(new IllegalStateException("Token not found"));

        // Act & Assert
        mockMvc.perform(get("/auth/confirm")
                        .param("token", "invalid-token"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Token not found"));
    }
}