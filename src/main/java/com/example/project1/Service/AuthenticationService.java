
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

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

    @Transactional
    public String register(RegisterRequest request) {
        // 1. Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("User already exists with email: " + request.getEmail());
        }

        // 2. Validate email format
        if (!emailValidator.test(request.getEmail())) {
            throw new IllegalStateException("Email format is invalid");
        }

        // 3. Build user with hashed password (verified = FALSE)
        var user = Users.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .verified(false)
                .build();

        var savedUser = userRepository.save(user);

        // 4. FIXED: Generate RANDOM UUID for confirmation (NOT JWT)
        String confirmationToken = UUID.randomUUID().toString();

        // 5. FIXED: Save as CONFIRMATION token type
        saveConfirmationToken(savedUser, confirmationToken);

        // 6. Send email with UUID token (NOT JWT)
        String activationLink = "http://localhost:8080/auth/confirm?token=" + confirmationToken;
        emailSender.send(request.getEmail(), buildEmail(request.getName(), activationLink));

        return "Registration successful! Please check your email to verify your account.";
    }

    public AuthenticationResponse login(LoginRequest request) {
        // 1. Authenticate via Spring Security
        Authentication authenticate=authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        // 2. Find the user
        Users user = (Users) authenticate.getPrincipal();
        // 3. Check if email is verified
        if (!user.isVerified()) {
            throw new RuntimeException("Account not verified. Please check your email to verify your account.");
        }

        // 4. Revoke all existing BEARER tokens only
        revokeAllUserBearerTokens(user);

        // 5. Generate NEW JWT for authentication
        var jwtToken = jwtService.generateToken(user);

        // 6. Save as BEARER type (authentication token)
        saveBearerToken(user, jwtToken);

        // 7. Return response
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .message("Logged in successfully")
                .build();
    }

    @Transactional
    public String confirmToken(String token) {
        // 1. Find token in database
        Token confirmationToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalStateException("Token not found"));

        // 2. FIXED: Verify this is a CONFIRMATION token
        if (confirmationToken.getTokenType() != TokenType.CONFIRMATION) {
            throw new IllegalStateException("Invalid token type — this is not a confirmation token");
        }

        // 3. Check if already confirmed
        if (confirmationToken.getConfirmedAt() != null) {
            throw new IllegalStateException("Email already confirmed");
        }

        // 4. Check if expired
        if (confirmationToken.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Confirmation link has expired. Please register again.");
        }

        // 5. Update token as confirmed
        confirmationToken.setConfirmedAt(LocalDateTime.now());
        tokenRepository.save(confirmationToken);

        // 6. Mark user as verified
        userRepository.verifyUser(confirmationToken.getUser().getEmail());

        return "Verification successful! You can now log in.";
    }

    // FIXED: Save CONFIRMATION token (UUID, not JWT)
    private void saveConfirmationToken(Users user, String token) {
        var confirmationToken = Token.builder()
                .user(user)
                .token(token)  // This is UUID, not JWT
                .tokenType(TokenType.CONFIRMATION)  // Important!
                .expired(false)
                .revoked(false)
                .confirmedAt(null)  // Not confirmed yet
                .expiredAt(LocalDateTime.now().plusDays(1))  // 24 hours to confirm
                .build();
        tokenRepository.save(confirmationToken);
    }

    // FIXED: Save BEARER token (JWT for authentication)
    private void saveBearerToken(Users user, String jwtToken) {
        var bearerToken = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(TokenType.BEARER)  // Different from CONFIRMATION
                .expired(false)
                .revoked(false)
                .confirmedAt(null)  // Not applicable for bearer tokens
                .expiredAt(LocalDateTime.now().plusDays(1))
                .build();
        tokenRepository.save(bearerToken);
    }

    // FIXED: Only revoke BEARER tokens, leave CONFIRMATION tokens alone
    private void revokeAllUserBearerTokens(Users user) {
        var validBearerTokens = tokenRepository.findAllValidBearerTokensByUser(user.getId());
        if (validBearerTokens.isEmpty()) return;

        validBearerTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(validBearerTokens);
    }

    private String buildEmail(String name, String link) {
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
