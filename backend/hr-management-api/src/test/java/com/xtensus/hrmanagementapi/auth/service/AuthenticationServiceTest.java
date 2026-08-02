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
import com.xtensus.hrmanagementapi.domain.entity.Employe;
import com.xtensus.hrmanagementapi.repository.EmployeRepository;
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
    @Mock private EmployeRepository employeRepository;
    private PasswordEncoder passwordEncoder;
    private JwtService jwtService;
    private AuthenticationService authenticationService;

    @BeforeEach void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        jwtService = new JwtService("replace_with_a_secure_secret_of_at_least_32_characters", 3600000);
        authenticationService = new AuthenticationService(employeRepository, passwordEncoder, jwtService);
    }

    @Test void loginSucceedsWithUsername() {
        Employe employe = employe("EMPLOYE", true, "ACTIF");
        when(employeRepository.findByUsernameIgnoreCase("jdoe")).thenReturn(Optional.of(employe));
        LoginResponse response = authenticationService.login(request(" jdoe ", "StrongPass123"));
        assertNotNull(response.getAccessToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals("jdoe", response.getUser().getUsername());
        assertEquals("EMPLOYEE", response.getUser().getRole().name());
    }

    @Test void loginSucceedsWithEmail() {
        Employe employe = employe("EMPLOYE", true, "ACTIF");
        when(employeRepository.findByUsernameIgnoreCase("jdoe@example.com")).thenReturn(Optional.empty());
        when(employeRepository.findByEmailIgnoreCase("jdoe@example.com")).thenReturn(Optional.of(employe));
        LoginResponse response = authenticationService.login(request("jdoe@example.com", "StrongPass123"));
        assertEquals("jdoe@example.com", response.getUser().getEmail());
    }

    @Test void invalidPasswordReturnsUnauthorized() {
        when(employeRepository.findByUsernameIgnoreCase("jdoe")).thenReturn(Optional.of(employe("EMPLOYE", true, "ACTIF")));
        assertThrows(InvalidCredentialsException.class, () -> authenticationService.login(request("jdoe", "wrong")));
    }

    @Test void missingUserReturnsSameGenericUnauthorized() {
        when(employeRepository.findByUsernameIgnoreCase("missing")).thenReturn(Optional.empty());
        when(employeRepository.findByEmailIgnoreCase("missing")).thenReturn(Optional.empty());
        InvalidCredentialsException exception = assertThrows(InvalidCredentialsException.class, () -> authenticationService.login(request("missing", "wrong")));
        assertEquals("Invalid username/email or password", exception.getMessage());
    }

    @Test void disabledAccountReturnsForbidden() {
        when(employeRepository.findByUsernameIgnoreCase("jdoe")).thenReturn(Optional.of(employe("EMPLOYE", false, "ACTIF")));
        assertThrows(AccountDisabledException.class, () -> authenticationService.login(request("jdoe", "StrongPass123")));
    }

    @Test void inactiveAccountReturnsForbidden() {
        when(employeRepository.findByUsernameIgnoreCase("jdoe")).thenReturn(Optional.of(employe("EMPLOYE", true, "INACTIF")));
        assertThrows(AccountInactiveException.class, () -> authenticationService.login(request("jdoe", "StrongPass123")));
    }

    @Test void jwtContainsExpectedClaimsOnly() {
        Employe employe = employe("RH", true, "ACTIF");
        String token = jwtService.generateToken(employe);
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

    private Employe employe(String role, boolean actif, String statut) {
        Employe employe = new Employe();
        employe.setId(1L);
        employe.setUsername("jdoe");
        employe.setEmail("jdoe@example.com");
        employe.setMotDePasseHash(passwordEncoder.encode("StrongPass123"));
        employe.setPrenom("John");
        employe.setNom("Doe");
        employe.setRole(role);
        employe.setActif(actif);
        employe.setStatut(statut);
        return employe;
    }
}
