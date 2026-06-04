package com.example.project1.Controller;

import com.example.project1.Repository.UsersRepository;
import com.example.project1.Service.UsersService;
import com.example.project1.model.Users;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import java.util.Optional;

@RestController
@RequestMapping("/user")
public class UsersController {
    @Autowired
    private UsersRepository usersRepository;
    @Autowired
    private UsersService userService;
    @GetMapping("/get")
    public ResponseEntity<?> getUserDetailsById(@RequestParam Integer id, Authentication authentication) {
        String loggedInEmail = authentication.getName();
        Optional<Users> user = usersRepository.findById(id);
        if (user.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        if (!user.get().getEmail().equals(loggedInEmail)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Cannot view other users");
        }
        Users safeUser = user.get();
        safeUser.setPassword(null);
        return ResponseEntity.ok(safeUser);
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
