package com.finance.dashboard.dto;

public record AuthResponse(
        String token,
        String username,
        String role
) {}