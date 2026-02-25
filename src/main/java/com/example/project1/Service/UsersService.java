package com.example.project1.Service;

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
    private static final String USER_NOT_FOUND_MSG = "user with email %s not found";
    public UserDetails loadUserByUserName(String email)throws UsernameNotFoundException{
        return  usersRepository.findByEmail(email).orElseThrow(()->new UsernameNotFoundException(String.format(USER_NOT_FOUND_MSG,email)));
    }
    public List<Users> getAllUser(){
        return usersRepository.findAll();
    }
    public Optional<Users> findUserId(int id){
        return usersRepository.findById(id);
    }
    public void deleteUserByEmail(String email) {
        Optional<Users> user = usersRepository.findByEmail(email);
        if (user.isPresent()) {
            usersRepository.delete(user.get());
        } else {
            throw new IllegalArgumentException("User not found");
        }
    }

}
