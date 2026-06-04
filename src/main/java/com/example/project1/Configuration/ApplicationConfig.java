package com.example.project1.Configuration;

import com.example.project1.Repository.UsersRepository;
import com.example.project1.Service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {

    private final UsersRepository userRepository;
    private final CustomUserDetailsService service;
    @Bean
    public UserDetailsService userDetailsService() {
        return service;
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        // FIXED: DaoAuthenticationProvider has no-arg constructor, then set UserDetailsService
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
/*
Step 0: Application starts up (Spring Boot)
        ↓
Step 1: User/client sends HTTP POST request to /auth/login
        ↓
Step 2: Network layer receives request (port 8080)
        ↓
Step 3: Spring Boot's embedded Tomcat/Undertow receives TCP connection
        ↓
Step 4: Request passes through Spring's filter chain
        - CharacterEncodingFilter
        - HiddenHttpMethodFilter
        - SecurityContextPersistenceFilter? (No, stateless)
        - JwtAuthenticationFilter? (No, /auth/** is public)
        ↓
Step 5: Spring's DispatcherServlet finds handler for /auth/login
        ↓
Step 6: Spring maps to AuthenticationController.login() method
        ↓
Step 7: Spring converts JSON to LoginRequest object (@RequestBody)
        ↓
Step 8: 🔥 YOUR CODE STARTS — AuthenticationController.login() executes
        ↓
Step 9: Controller calls authenticationService.login(request)
        ↓
Step 10: Service calls authenticationManager.authenticate()  ← YOU ASKED ABOUT THIS
        ↓
Step 11: Spring Security's internal flow begins
 */
