package com.nba.nbastatsapi.service;

import com.nba.nbastatsapi.auth.LoginRequest;
import com.nba.nbastatsapi.auth.RegisterRequest;
import com.nba.nbastatsapi.entity.Role;
import com.nba.nbastatsapi.entity.User;
import com.nba.nbastatsapi.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    // Verify registration fails when the email already exists
    @Test
    void register_shouldReturnFalse_whenEmailAlreadyExists() {

        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(new User()));

        boolean result = authService.register(request);

        assertFalse(result);
        verify(userRepository, never()).save(any(User.class));
    }

    // Verify successful registration encrypts the password and saves the user
    @Test
    void register_shouldSaveUser_whenEmailDoesNotExist() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("new@example.com");
        request.setPassword("password123");

        when(userRepository.findByEmail("new@example.com"))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode("password123"))
                .thenReturn("hashedPassword");

        boolean result = authService.register(request);

        assertTrue(result);

        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(argThat(user ->
                user.getEmail().equals("new@example.com")
                        && user.getPassword().equals("hashedPassword")
                        && user.getRole() == Role.USER
        ));
    }
    // Verify login fails when the email does not exist
    @Test
    void login_shouldReturnNull_whenEmailDoesNotExist() {
        LoginRequest request = new LoginRequest();
        request.setEmail("unknown@example.com");
        request.setPassword("password123");

        when(userRepository.findByEmail("unknown@example.com"))
                .thenReturn(Optional.empty());

        String token = authService.login(request);

        assertNull(token);
        verifyNoInteractions(passwordEncoder, jwtService);
    }

}