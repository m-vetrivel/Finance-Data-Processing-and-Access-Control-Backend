package com.finance.dashboard.dto;

import com.finance.dashboard.enums.Role;
import jakarta.validation.constraints.NotNull;

public record UpdateUserRequest(
        @NotNull(message = "Role is required")
        Role role,

        @NotNull(message = "Active status is required")
        Boolean active
) {}