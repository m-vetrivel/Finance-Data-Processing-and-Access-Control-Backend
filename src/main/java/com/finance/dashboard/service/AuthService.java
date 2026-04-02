package com.finance.dashboard.service;

import com.finance.dashboard.dto.*;
import com.finance.dashboard.entity.User;
import com.finance.dashboard.exception.AppException;
import com.finance.dashboard.repository.UserRepository;
import com.finance.dashboard.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository      userRepository;
    private final PasswordEncoder     passwordEncoder;
    private final JwtUtils            jwtUtils;
    private final AuthenticationManager authenticationManager;

    public AuthResponse login(AuthRequest request) {
        // throws BadCredentialsException if wrong — handled globally
        var auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username(), request.password()));

        User user = (User) auth.getPrincipal();
        String token = jwtUtils.generateToken(user);

        return new AuthResponse(token, user.getUsername(),
                user.getRole().name());
    }

    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username()))
            throw new AppException("Username already taken",
                    HttpStatus.CONFLICT);

        if (userRepository.existsByEmail(request.email()))
            throw new AppException("Email already registered",
                    HttpStatus.CONFLICT);

        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(request.role())
                .build();

        return UserResponse.from(userRepository.save(user));
    }
}