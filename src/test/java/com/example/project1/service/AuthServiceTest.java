package com.example.project1.service;

import com.example.project1.Payload.Request.LoginRequest;
import com.example.project1.Payload.Request.RegisterRequest;
import com.example.project1.Payload.Response.AuthenticationResponse;
import com.example.project1.Repository.TokenRepository;
import com.example.project1.Repository.UsersRepository;
import com.example.project1.Service.AuthenticationService;
import com.example.project1.Service.JwtService;
import com.example.project1.Token.Token;
import com.example.project1.email.EmailSender;
import com.example.project1.email.EmailValidator;
import com.example.project1.model.Users;
import org.assertj.core.api.Java6BDDSoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;




@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
    @Mock
    private UsersRepository userRepository;

    @Mock
    private TokenRepository tokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private EmailValidator emailValidator;

    @Mock
    private EmailSender emailSender;
    @InjectMocks
    private AuthenticationService authenticationService;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private Users user;
    private Token token;
    @BeforeEach
    void setUp() {
        // Setup test data
        registerRequest = new RegisterRequest();
        registerRequest.setName("John Doe");
        registerRequest.setEmail("john@example.com");
        registerRequest.setPassword("password123");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("john@example.com");
        loginRequest.setPassword("password123");

        user = Users.builder()
                .id(1)
                .name("John Doe")
                .email("john@example.com")
                .password("encodedPassword")
                .verified(false)
                .build();
        token = Token.builder()
                .id(1)
                .token("jwt-token-123")
                .user(user)
                .expired(false)
                .revoked(false)
                .expiredAt(LocalDateTime.now().plusDays(1))
                .build();
    }
    @Test
    void register_Success_ReturnsSuccessMessage(){
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(emailValidator.test("john@example.com")).thenReturn(true);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(Users.class))).thenReturn(user);
        when(jwtService.generateToken(any(Users.class))).thenReturn("jwt-token-123");
        String result = authenticationService.register(registerRequest);
        assertEquals("Registration successful! Please check your email to verify your account.", result);
        verify(userRepository, times(1)).save(any(Users.class));
        verify(emailSender, times(1)).send(anyString(), anyString());
    }
    @Test
    void register_WhenEmailExists_ThrowsException(){
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);
        RuntimeException exception=assertThrows(RuntimeException.class,()->{authenticationService.register(registerRequest);
        });
        verify(userRepository, never()).save(any(Users.class));
    }

    @Test
    void register_InvalidEmailFormat_ThrowsException() {
        when(userRepository.existsByEmail("invalid-email")).thenReturn(false);
        when(emailValidator.test("invalid-email")).thenReturn(false);

        registerRequest.setEmail("invalid-email");

        // Act & Assert
        assertThatThrownBy(() -> authenticationService.register(registerRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Email format is invalid");

        verify(userRepository, never()).save(any(Users.class));
    }
    @Test
    void login_Success_ReturnsAuthenticationResponse() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("new-jwt-token");
        when(tokenRepository.findAllValidTokenByUser(user.getId())).thenReturn(Arrays.asList(token));
        when(tokenRepository.saveAll(anyList())).thenReturn(Arrays.asList(token));
        when(tokenRepository.save(any(Token.class))).thenReturn(token);
        AuthenticationResponse response = authenticationService.login(loginRequest);
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("new-jwt-token");
        // Verify
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByEmail("john@example.com");
        verify(jwtService).generateToken(user);
        verify(tokenRepository).findAllValidTokenByUser(user.getId());
        verify(tokenRepository).saveAll(anyList());
    }
    @Test
    void login_WhenUserNotFound_ThrowsException(){
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> {
            authenticationService.login(loginRequest);
        });
        verify(jwtService, never()).generateToken(any());
        verify(tokenRepository, never()).saveAll(anyList());
        verify(tokenRepository, never()).save(any(Token.class));
    }
    @Test
    void confirmToken_Success_VerifiesUser() {
        // Arrange - create a valid token
        Token validToken = Token.builder()
                .token("valid-token")
                .user(user)
                .confirmedAt(null)
                .expiredAt(java.time.LocalDateTime.now().plusDays(1))
                .build();
        when(tokenRepository.findByToken("valid-token")).thenReturn(Optional.of(validToken));
        when(tokenRepository.updateConfirmedAt(eq("valid-token"), any(java.time.LocalDateTime.class))).thenReturn(1);
        when(userRepository.verifyUser(user.getEmail())).thenReturn(1);
        String result = authenticationService.confirmToken("valid-token");
        assertEquals("Verification successful! You can now log in.", result);

    }
    @Test
    void confirmToken_WhenTokenNotFound_ThrowsException() {
        when(tokenRepository.findByToken("invalid-token")).thenReturn(Optional.empty());
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            authenticationService.confirmToken("invalid-token");
        });
        assertEquals("Token not found", exception.getMessage());
    }
    @Test
    void confirmToken_WhenTokenExpired_ThrowsException() {
        // Arrange - create expired token
        Token expiredToken = Token.builder()
                .token("expired-token")
                .user(user)
                .confirmedAt(null)
                .expiredAt(java.time.LocalDateTime.now().minusDays(1)) // Yesterday = expired
                .build();

        when(tokenRepository.findByToken("expired-token")).thenReturn(Optional.of(expiredToken));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            authenticationService.confirmToken("expired-token");
        });

        assertEquals("Confirmation link has expired", exception.getMessage());
    }

}
