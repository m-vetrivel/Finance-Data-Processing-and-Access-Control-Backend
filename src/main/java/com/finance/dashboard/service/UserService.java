package com.finance.dashboard.service;

import com.finance.dashboard.dto.*;
import com.finance.dashboard.exception.AppException;
import com.finance.dashboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserResponse::from)
                .toList();
    }

    public UserResponse getUserById(Long id) {
        return userRepository.findById(id)
                .map(UserResponse::from)
                .orElseThrow(() -> new AppException(
                        "User not found with id: " + id,
                        HttpStatus.NOT_FOUND));
    }

    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(
                        "User not found with id: " + id,
                        HttpStatus.NOT_FOUND));

        user.setRole(request.role());
        user.setActive(request.active());

        return UserResponse.from(userRepository.save(user));
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id))
            throw new AppException("User not found with id: " + id,
                    HttpStatus.NOT_FOUND);
        userRepository.deleteById(id);
    }
}