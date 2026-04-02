package com.finance.dashboard.service;

import com.finance.dashboard.dto.*;
import com.finance.dashboard.entity.User;
import com.finance.dashboard.enums.Role;
import com.finance.dashboard.exception.AppException;
import com.finance.dashboard.repository.UserRepository;
import com.finance.dashboard.security.JwtUtils;
import com.finance.dashboard.util.TestDataFactory;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;


import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository        userRepository;
    @Mock PasswordEncoder       passwordEncoder;
    @Mock JwtUtils              jwtUtils;
    @Mock AuthenticationManager authenticationManager;

    @InjectMocks AuthService authService;

    // ── Login ─────────────────────────────────────────────

    @Test
    @DisplayName("login: valid credentials return token")
    void login_validCredentials_returnsToken() {
        User user = TestDataFactory.makeUser("admin", Role.ADMIN);
        var authToken = new UsernamePasswordAuthenticationToken(
                user, null, user.getAuthorities());

        when(authenticationManager.authenticate(any()))
                .thenReturn(authToken);
        when(jwtUtils.generateToken(user)).thenReturn("mock-jwt");

        AuthResponse response = authService.login(
                new AuthRequest("admin", "admin123"));

        assertThat(response.token()).isEqualTo("mock-jwt");
        assertThat(response.username()).isEqualTo("admin");
        assertThat(response.role()).isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("login: bad credentials propagate exception")
    void login_badCredentials_throwsException() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("bad"));

        assertThatThrownBy(() ->
                authService.login(new AuthRequest("admin", "wrong")))
                .isInstanceOf(BadCredentialsException.class);
    }

    // ── Register ──────────────────────────────────────────

    @Test
    @DisplayName("register: new user is saved and returned")
    void register_newUser_isSaved() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(passwordEncoder.encode("pass123")).thenReturn("hashed");

        User saved = TestDataFactory.makeUser("newuser", Role.VIEWER);
        when(userRepository.save(any())).thenReturn(saved);

        RegisterRequest req = new RegisterRequest(
                "newuser", "pass123", "new@test.com", Role.VIEWER);

        UserResponse response = authService.register(req);

        assertThat(response.username()).isEqualTo("newuser");
        assertThat(response.role()).isEqualTo(Role.VIEWER);
        verify(userRepository).save(any());
    }

    @Test
    @DisplayName("register: duplicate username throws CONFLICT")
    void register_duplicateUsername_throwsConflict() {
        when(userRepository.existsByUsername("admin")).thenReturn(true);

        assertThatThrownBy(() ->
                authService.register(new RegisterRequest(
                        "admin", "pass123", "a@test.com", Role.ADMIN)))
                .isInstanceOf(AppException.class)
                .satisfies(ex ->
                    assertThat(((AppException) ex).getStatus())
                            .isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    @DisplayName("register: duplicate email throws CONFLICT")
    void register_duplicateEmail_throwsConflict() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("dupe@test.com")).thenReturn(true);

        assertThatThrownBy(() ->
                authService.register(new RegisterRequest(
                        "newuser", "pass123", "dupe@test.com", Role.VIEWER)))
                .isInstanceOf(AppException.class)
                .satisfies(ex ->
                    assertThat(((AppException) ex).getStatus())
                            .isEqualTo(HttpStatus.CONFLICT));
    }
}