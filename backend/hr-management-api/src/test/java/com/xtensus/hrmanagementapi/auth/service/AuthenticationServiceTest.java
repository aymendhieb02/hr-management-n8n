package com.xtensus.hrmanagementapi.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.xtensus.hrmanagementapi.auth.dto.LoginRequest;
import com.xtensus.hrmanagementapi.auth.dto.LoginResponse;
import com.xtensus.hrmanagementapi.auth.exception.AccountDisabledException;
import com.xtensus.hrmanagementapi.auth.exception.AccountInactiveException;
import com.xtensus.hrmanagementapi.auth.exception.InvalidCredentialsException;
import com.xtensus.hrmanagementapi.domain.entity.User;
import com.xtensus.hrmanagementapi.domain.enums.RoleType;
import com.xtensus.hrmanagementapi.domain.enums.UserStatus;
import com.xtensus.hrmanagementapi.repository.UserRepository;
import com.xtensus.hrmanagementapi.security.jwt.JwtService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    private PasswordEncoder passwordEncoder;

    private JwtService jwtService;

    private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        jwtService = new JwtService("replace_with_a_secure_secret_of_at_least_32_characters", 3600000);
        authenticationService = new AuthenticationService(userRepository, passwordEncoder, jwtService);
    }

    @Test
    void loginSucceedsWithUsername() {
        User user = user(RoleType.EMPLOYEE, true, UserStatus.ACTIVE);
        when(userRepository.findByUsernameIgnoreCase("jdoe")).thenReturn(Optional.of(user));

        LoginResponse response = authenticationService.login(request(" jdoe ", "StrongPass123"));

        assertNotNull(response.getAccessToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals("jdoe", response.getUser().getUsername());
    }

    @Test
    void loginSucceedsWithEmail() {
        User user = user(RoleType.EMPLOYEE, true, UserStatus.ACTIVE);
        when(userRepository.findByUsernameIgnoreCase("jdoe@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByEmailIgnoreCase("jdoe@example.com")).thenReturn(Optional.of(user));

        LoginResponse response = authenticationService.login(request("jdoe@example.com", "StrongPass123"));

        assertEquals("jdoe@example.com", response.getUser().getEmail());
    }

    @Test
    void usernameMatchingIsCaseInsensitive() {
        User user = user(RoleType.EMPLOYEE, true, UserStatus.ACTIVE);
        when(userRepository.findByUsernameIgnoreCase("JDOE")).thenReturn(Optional.of(user));

        LoginResponse response = authenticationService.login(request("JDOE", "StrongPass123"));

        assertEquals("jdoe", response.getUser().getUsername());
    }

    @Test
    void emailMatchingIsCaseInsensitive() {
        User user = user(RoleType.EMPLOYEE, true, UserStatus.ACTIVE);
        when(userRepository.findByUsernameIgnoreCase("JDOE@EXAMPLE.COM")).thenReturn(Optional.empty());
        when(userRepository.findByEmailIgnoreCase("JDOE@EXAMPLE.COM")).thenReturn(Optional.of(user));

        LoginResponse response = authenticationService.login(request("JDOE@EXAMPLE.COM", "StrongPass123"));

        assertEquals("jdoe@example.com", response.getUser().getEmail());
    }

    @Test
    void invalidPasswordReturnsUnauthorized() {
        when(userRepository.findByUsernameIgnoreCase("jdoe"))
                .thenReturn(Optional.of(user(RoleType.EMPLOYEE, true, UserStatus.ACTIVE)));

        assertThrows(InvalidCredentialsException.class, () -> authenticationService.login(request("jdoe", "wrong")));
    }

    @Test
    void missingUserReturnsSameGenericUnauthorized() {
        when(userRepository.findByUsernameIgnoreCase("missing")).thenReturn(Optional.empty());
        when(userRepository.findByEmailIgnoreCase("missing")).thenReturn(Optional.empty());

        InvalidCredentialsException exception = assertThrows(
                InvalidCredentialsException.class,
                () -> authenticationService.login(request("missing", "wrong"))
        );
        assertEquals("Invalid username/email or password", exception.getMessage());
    }

    @Test
    void disabledAccountReturnsForbidden() {
        when(userRepository.findByUsernameIgnoreCase("jdoe"))
                .thenReturn(Optional.of(user(RoleType.EMPLOYEE, false, UserStatus.ACTIVE)));

        assertThrows(AccountDisabledException.class, () -> authenticationService.login(request("jdoe", "StrongPass123")));
    }

    @Test
    void inactiveAccountReturnsForbidden() {
        when(userRepository.findByUsernameIgnoreCase("jdoe"))
                .thenReturn(Optional.of(user(RoleType.EMPLOYEE, true, UserStatus.INACTIVE)));

        assertThrows(AccountInactiveException.class, () -> authenticationService.login(request("jdoe", "StrongPass123")));
    }

    @Test
    void jwtContainsExpectedClaimsOnly() {
        User user = user(RoleType.HR, true, UserStatus.ACTIVE);
        String token = jwtService.generateToken(user);

        assertEquals(1L, jwtService.extractUserId(token));
        assertEquals("jdoe", jwtService.extractUsername(token));
        assertEquals("HR", jwtService.extractRole(token));
        assertFalse(token.contains("hash"));
        assertFalse(token.contains("jdoe@example.com"));
    }

    private LoginRequest request(String usernameOrEmail, String password) {
        LoginRequest request = new LoginRequest();
        request.setUsernameOrEmail(usernameOrEmail);
        request.setPassword(password);
        return request;
    }

    private User user(RoleType role, boolean enabled, UserStatus status) {
        User user = new User();
        user.setId(1L);
        user.setUsername("jdoe");
        user.setEmail("jdoe@example.com");
        user.setPasswordHash(passwordEncoder.encode("StrongPass123"));
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setRole(role);
        user.setEnabled(enabled);
        user.setStatus(status);
        return user;
    }
}
