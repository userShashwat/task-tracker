package com.example.project1.model;

import com.example.project1.Token.Token;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(name = "unique_email", columnNames = "email")
})
public class Users implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Better for MySQL/PostgreSQL
    private Integer id;

    private String name;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private boolean verified;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Token> tokens;

    // --- UserDetails Implementation ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Return an empty list or roles (e.g., SimpleGrantedAuthority("USER"))
        return List.of();
    }

    @Override
    public String getPassword() {
        return password; // Fix: Must return the hashed password field
    }

    @Override
    public String getUsername() {
        return email; // Fix: Since email is your login identifier
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Set to true so login isn't blocked
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Set to true
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Set to true
    }

    @Override
    public boolean isEnabled() {
        return verified; // Only allows login if the user has confirmed their email
    }
}