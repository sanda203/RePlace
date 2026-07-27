package com.wil.reservation_api.service;

import com.wil.reservation_api.dto.LoginResponse;
import com.wil.reservation_api.dto.UserResponse;
import com.wil.reservation_api.entity.User;
import com.wil.reservation_api.entity.exception.EmailAlreadyInUseException;
import com.wil.reservation_api.entity.exception.EntityNotFoundException;
import com.wil.reservation_api.repository.UserRepository;
import com.wil.reservation_api.security.JwtService;
import com.wil.reservation_api.security.UserPrincipal;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;


    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public UserResponse register(String password, String email){
        if (userRepository.findByEmail(email).isPresent()) {
            throw new EmailAlreadyInUseException("This email is already in use: " + email);
        }
        String encodedPassword = passwordEncoder.encode(password);
        User user = new User(email, encodedPassword);
        userRepository.save(user);
        return new UserResponse(user.getId(), user.getEmail());
    }

    public LoginResponse login(String password, String email){
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        String token = jwtService.generateToken(principal.getId());
        return new LoginResponse(token);
    }
}
