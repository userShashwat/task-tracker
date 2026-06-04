package com.example.project1.Service;

import com.example.project1.Repository.TokenRepository;
import com.example.project1.Repository.UsersRepository;
import com.example.project1.model.Users;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class UsersService {
    private final UsersRepository usersRepository;
    private final TokenRepository tokenRepository;
    public void deleteUserByEmail(String email) {
        Optional<Users> user = usersRepository.findByEmail(email);
        if (user.isPresent()) {
            tokenRepository.deleteTokenByUserId(user.get().getId());
            usersRepository.delete(user.get());
        } else {
            throw new IllegalArgumentException("User not found");
        }
    }

}
