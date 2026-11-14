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
    void register_shouldThrowException_whenEmailAlreadyExists() {

        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setUsername("Tony");
        request.setPassword("password123");

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(new User()));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.register(request)
        );

        assertEquals("Email already exists", exception.getMessage());

        verify(userRepository, never()).save(any(User.class));
    }

    // Verify successful registration encrypts the password and saves the user
    @Test
    void register_shouldSaveUser_whenEmailAndUsernameDoNotExist() {

        RegisterRequest request = new RegisterRequest();
        request.setEmail("new@example.com");
        request.setUsername("NewPlayer");
        request.setPassword("password123");

        when(userRepository.findByEmail("new@example.com"))
                .thenReturn(Optional.empty());

        when(userRepository.existsByUsername("NewPlayer"))
                .thenReturn(false);

        when(passwordEncoder.encode("password123"))
                .thenReturn("hashedPassword");

        assertDoesNotThrow(() -> authService.register(request));

        verify(passwordEncoder).encode("password123");

        verify(userRepository).save(argThat(user ->
                user.getEmail().equals("new@example.com")
                        && user.getUsername().equals("NewPlayer")
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