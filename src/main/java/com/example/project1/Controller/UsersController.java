package com.example.project1.Controller;

import com.example.project1.Repository.UsersRepository;
import com.example.project1.Service.UsersService;
import com.example.project1.model.Users;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;
import java.util.Optional;

@RestController
@RequestMapping("/user")
public class UsersController {
    @Autowired
    private UsersRepository usersRepository;
    @Autowired
    private UsersService userService;
    public ResponseEntity<Users> getUserDetailsById(@RequestParam Integer id){
        Optional<Users> user=usersRepository.findById(id);
        return ResponseEntity.ok(user.get());
    }
    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteLoggedInUser(Authentication authentication) {
        if (authentication != null) {
            String userEmail = authentication.getName(); // Assuming the email is the username
            try {
                userService.deleteUserByEmail(userEmail);
                return ResponseEntity.ok("User deleted successfully");
            } catch (IllegalArgumentException e) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while deleting the user.");
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication required.");
        }
    }


}
