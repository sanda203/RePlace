package com.wil.reservation_api.service;

import com.wil.reservation_api.dto.auth.LoginResponse;
import com.wil.reservation_api.dto.auth.UserResponse;
import com.wil.reservation_api.entity.User;
import com.wil.reservation_api.entity.exception.EmailAlreadyInUseException;
import com.wil.reservation_api.repository.UserRepository;
import com.wil.reservation_api.security.JwtService;
import com.wil.reservation_api.security.UserPrincipal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    @Test
    void givenNewEmail_whenRegister_thenPasswordIsEncodedAndUserSaved() {
        String email = "new@test.com";
        String rawPassword = "raw-password";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(rawPassword)).thenReturn("ENCODED");

        UserResponse response = authService.register(rawPassword, email);

        ArgumentCaptor<User> savedUser = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(savedUser.capture());
        assertEquals("ENCODED", savedUser.getValue().getPassword());
        assertEquals(email, savedUser.getValue().getEmail());
        assertEquals(email, response.email());
    }

    @Test
    void givenExistingEmail_whenRegister_thenThrowsEmailAlreadyInUse() {
        String email = "taken@test.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(new User(email, "ENCODED")));

        assertThrows(EmailAlreadyInUseException.class, () -> authService.register("raw-password", email));

        verify(userRepository, never()).save(any());
    }

    @Test
    void givenValidCredentials_whenLogin_thenReturnsGeneratedToken() {
        UUID userId = UUID.randomUUID();
        User user = new User("user@test.com", "ENCODED");
        ReflectionTestUtils.setField(user, "id", userId);
        Authentication authentication = org.mockito.Mockito.mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(new UserPrincipal(user));
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtService.generateToken(userId)).thenReturn("TOKEN");

        LoginResponse response = authService.login("raw-password", "user@test.com");

        assertEquals("TOKEN", response.token());
    }
}
