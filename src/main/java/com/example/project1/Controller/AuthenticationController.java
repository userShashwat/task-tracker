package com.example.project1.Controller;

import com.example.project1.Payload.Request.LoginRequest;
import com.example.project1.Payload.Request.RegisterRequest;
import com.example.project1.Payload.Response.AuthenticationResponse;
import com.example.project1.Service.AuthenticationService;
import com.example.project1.Repository.UsersRepository; // Add this import
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    @Autowired
    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) { //takes in an HTTP request body (typically in JSON format) and maps it to a RegisterRequest object using @RequestBody
        return ResponseEntity.ok(authenticationService.register(request));
    }
    //    @PostMapping("/registerAdmin")
//    public ResponseEntity<String> registerAdmin(@RequestBody RegisterRequest request) {
//        return ResponseEntity.ok(authenticationService.registerAdmin(request));
//    }
    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authenticationService.login(request));
    }

    @GetMapping(path = "/confirm")
    public String confirm(@RequestParam("token") String token) {
        return authenticationService.confirmToken(token);
    }

}