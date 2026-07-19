package com.wil.reservation_api.service;

import com.wil.reservation_api.entity.Event;
import com.wil.reservation_api.entity.User;
import com.wil.reservation_api.entity.exception.EntityNotFoundException;
import com.wil.reservation_api.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getByIdOrThrow(UUID id) {
        return userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("User not found: " + id));
    }


}
