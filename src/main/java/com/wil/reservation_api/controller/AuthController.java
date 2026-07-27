package com.wil.reservation_api.controller;

import com.wil.reservation_api.dto.auth.LoginRequest;
import com.wil.reservation_api.dto.auth.LoginResponse;
import com.wil.reservation_api.dto.auth.RegisterRequest;
import com.wil.reservation_api.dto.auth.UserResponse;
import com.wil.reservation_api.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequest registerRequest){
        return authService.register(registerRequest.password(), registerRequest.email());
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public LoginResponse login(@Valid @RequestBody LoginRequest loginRequest){
        return authService.login(loginRequest.password(), loginRequest.email());
    }

}
