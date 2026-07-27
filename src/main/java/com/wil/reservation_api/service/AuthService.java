package com.wil.reservation_api.service;

import com.wil.reservation_api.dto.UserResponse;
import com.wil.reservation_api.entity.User;
import com.wil.reservation_api.entity.exception.EmailAlreadyInUseException;
import com.wil.reservation_api.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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
}
