package com.xtensus.hrmanagementapi.auth.service;

import com.xtensus.hrmanagementapi.auth.dto.AuthenticatedUserResponse;
import com.xtensus.hrmanagementapi.auth.dto.LoginRequest;
import com.xtensus.hrmanagementapi.auth.dto.LoginResponse;
import com.xtensus.hrmanagementapi.auth.exception.AccountDisabledException;
import com.xtensus.hrmanagementapi.auth.exception.AccountInactiveException;
import com.xtensus.hrmanagementapi.auth.exception.InvalidCredentialsException;
import com.xtensus.hrmanagementapi.domain.entity.Employe;
import com.xtensus.hrmanagementapi.domain.enums.RoleType;
import com.xtensus.hrmanagementapi.repository.EmployeRepository;
import com.xtensus.hrmanagementapi.security.jwt.JwtService;
import com.xtensus.hrmanagementapi.security.user.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthenticationService {
    private final EmployeRepository employeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthenticationService(EmployeRepository employeRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.employeRepository = employeRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        String usernameOrEmail = request.getUsernameOrEmail().trim();
        Employe employe = employeRepository.findByUsernameIgnoreCase(usernameOrEmail)
                .or(() -> employeRepository.findByEmailIgnoreCase(usernameOrEmail))
                .orElseThrow(InvalidCredentialsException::new);
        validateAccount(employe);
        if (!passwordEncoder.matches(request.getPassword(), employe.getMotDePasseHash())) { throw new InvalidCredentialsException(); }
        LoginResponse response = new LoginResponse();
        response.setAccessToken(jwtService.generateToken(employe));
        response.setTokenType("Bearer");
        response.setExpiresIn(jwtService.getExpirationMs());
        response.setUser(toAuthenticatedUser(employe));
        return response;
    }

    @Transactional(readOnly = true)
    public AuthenticatedUserResponse me() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails principal)) { throw new InvalidCredentialsException(); }
        Employe employe = employeRepository.findByUsernameIgnoreCase(principal.getUsername()).orElseThrow(InvalidCredentialsException::new);
        return toAuthenticatedUser(employe);
    }

    private void validateAccount(Employe employe) {
        if (!Boolean.TRUE.equals(employe.getActif())) { throw new AccountDisabledException(); }
        if (!"ACTIF".equalsIgnoreCase(employe.getStatut())) { throw new AccountInactiveException(); }
    }

    private AuthenticatedUserResponse toAuthenticatedUser(Employe employe) {
        AuthenticatedUserResponse response = new AuthenticatedUserResponse();
        response.setId(employe.getId());
        response.setUsername(employe.getUsername());
        response.setEmail(employe.getEmail());
        response.setFirstName(employe.getPrenom());
        response.setLastName(employe.getNom());
        response.setRole(RoleType.fromDatabaseRole(employe.getRole()));
        return response;
    }
}
