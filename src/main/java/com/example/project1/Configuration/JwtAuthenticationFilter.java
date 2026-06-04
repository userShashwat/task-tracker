package com.example.project1.Configuration;

import com.example.project1.Repository.TokenRepository;
import com.example.project1.Service.JwtService;
import com.example.project1.Token.TokenType;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final TokenRepository tokenRepository;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // 1. Check if Authorization header exists and starts with "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Extract JWT token from header
        jwt = authHeader.substring(7);
        userEmail = jwtService.extractUsername(jwt);

        // 3. If email exists and no authentication is set yet
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // 4. Load user details from database
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

            // 5. CRITICAL FIX: Check token in database AND verify it's a BEARER token
            var tokenRecord = tokenRepository.findByToken(jwt);

            // 6. REJECT if token is CONFIRMATION type (email verification tokens cannot authenticate)
            if (tokenRecord.isPresent() && tokenRecord.get().getTokenType() == TokenType.CONFIRMATION) {
                // This is a confirmation token — reject authentication
                filterChain.doFilter(request, response);
                return;
            }

            // 7. Check if token is valid (signature + not expired + not revoked)
            var isTokenValid = tokenRecord
                    .map(t -> !t.isExpired() && !t.isRevoked())
                    .orElse(false);

            // 8. If JWT signature is valid AND token exists in DB and is not revoked/expired
            if (jwtService.isTokenValid(jwt, userDetails) && isTokenValid) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // 9. Continue the filter chain
        filterChain.doFilter(request, response);
    }
}