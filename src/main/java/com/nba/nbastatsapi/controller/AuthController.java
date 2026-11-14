package com.nba.nbastatsapi.controller;

import com.nba.nbastatsapi.auth.AuthResponse;
import com.nba.nbastatsapi.auth.LoginRequest;
import com.nba.nbastatsapi.auth.RegisterRequest;
import com.nba.nbastatsapi.entity.Role;
import com.nba.nbastatsapi.entity.User;
import com.nba.nbastatsapi.repository.UserRepository;
import com.nba.nbastatsapi.service.AuthService;
import com.nba.nbastatsapi.service.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;


@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
        try {
            authService.register(request);

            return ResponseEntity.ok("User registered successfully");

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .status(409)
                    .body(e.getMessage());
        }
    }

    // Authenticate a user and return a JWT
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {

        String token = authService.login(request);

        if (token == null) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(new AuthResponse(token));
    }


}