package com.finance.dashboard.dto;

import com.finance.dashboard.enums.Role;
import jakarta.validation.constraints.*;

public record RegisterRequest(
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50, message = "Username must be 3-50 characters")
        String username,

        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        String password,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        @NotNull(message = "Role is required")
        Role role
) {}