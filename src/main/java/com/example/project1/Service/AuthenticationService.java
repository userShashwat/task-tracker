package com.example.project1.Service;


import com.example.project1.Payload.Request.LoginRequest;
import com.example.project1.Payload.Request.RegisterRequest;
import com.example.project1.Payload.Response.AuthenticationResponse;
import com.example.project1.Repository.TokenRepository;
import com.example.project1.Repository.UsersRepository;
import com.example.project1.Token.Token;
import com.example.project1.Token.TokenType;
import com.example.project1.email.EmailSender;
import com.example.project1.email.EmailValidator;
import com.example.project1.model.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UsersRepository userRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailValidator emailValidator;
    private final EmailSender emailSender;

    // Modernized Register: Validates, Saves, and triggers Verification email
    @Transactional
    public String register(RegisterRequest request) {
        // 1. Single check for existence to save DB resources
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("User already exists with email: " + request.getEmail());
        }

        // 2. Validate email format using your custom Predicate
        if (!emailValidator.test(request.getEmail())) {
            throw new IllegalStateException("Email format is invalid");
        }

        // 3. Build user with hashed password
        var user = Users.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .verified(false)
                .build();

        var savedUser = userRepository.save(user);

        // 4. Token Generation for email confirmation
        var jwtToken = jwtService.generateToken(user);
        saveUserToken(savedUser, jwtToken);

        // 5. Send Email (Note: URL should ideally come from @Value or Config)
        String activationLink = "http://localhost:8080/auth/confirm?token=" + jwtToken;
        emailSender.send(request.getEmail(), buildEmail(request.getName(), activationLink));

        return "Registration successful! Please check your email to verify your account.";
    }

    public AuthenticationResponse login(LoginRequest request) {
        // 1. Authenticate via Spring Security
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // COMMENT THIS OUT TEMPORARILY FOR TESTING
    /*
    if (!user.isVerified()) {
        throw new RuntimeException("Account not verified. Please check your email.");
    }
    */

        revokeAllUserTokens(user);
        var jwtToken = jwtService.generateToken(user);
        saveUserToken(user, jwtToken);

        return AuthenticationResponse.builder()
                .token(jwtToken)
                .message("Logged in successfully")
                .build();
    }
    @Transactional
    public String confirmToken(String token) {
        Token confirmationToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalStateException("Token not found"));

        // Guard: Check if already confirmed
        if (confirmationToken.getConfirmedAt() != null) {
            throw new IllegalStateException("Email is already confirmed");
        }

        // Guard: Check expiration
        if (confirmationToken.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Confirmation link has expired");
        }

        // Update database state
        tokenRepository.updateConfirmedAt(token, LocalDateTime.now());
        userRepository.verifyUser(confirmationToken.getUser().getEmail());

        return "Verification successful! You can now log in.";
    }

    private void saveUserToken(Users user, String jwtToken) {
        var token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .expiredAt(LocalDateTime.now().plusDays(1)) // Reduced to 1 day for security
                .build();
        tokenRepository.save(token);
    }

    private void revokeAllUserTokens(Users user) {
        var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());
        if (validUserTokens.isEmpty()) return;

        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(validUserTokens);
    }

    private String buildEmail(String name, String link) {
        // Modernized template using simple inline styles
        return "<div style=\"font-family:Arial,sans-serif; padding:20px; border:1px solid #eee; border-radius:10px; max-width:600px;\">" +
                "<h2>Confirm your Email</h2>" +
                "<p>Hi " + name + ",</p>" +
                "<p>Welcome to TaskTracker! Please click the button below to activate your account:</p>" +
                "<div style=\"text-align: center; margin: 30px 0;\">" +
                "  <a href=\"" + link + "\" style=\"background-color: #1D70B8; color: white; padding: 15px 25px; text-decoration: none; border-radius: 5px; font-weight: bold;\">Activate Now</a>" +
                "</div>" +
                "<p>This link will expire in 24 hours.</p>" +
                "<p>Cheers,<br>The TaskTracker Team</p>" +
                "</div>";
    }
}