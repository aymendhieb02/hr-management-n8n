package com.xtensus.hrmanagementapi.employe.controller;

import com.xtensus.hrmanagementapi.employe.dto.EmployeRequest;
import com.xtensus.hrmanagementapi.employe.dto.EmployeResponse;
import com.xtensus.hrmanagementapi.employe.dto.MotDePasseModificationRequest;
import com.xtensus.hrmanagementapi.employe.service.EmployeService;
import com.xtensus.hrmanagementapi.security.user.CustomUserDetails;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/employes")
public class EmployeController {

    private final EmployeService employeService;

    public EmployeController(EmployeService employeService) {
        this.employeService = employeService;
    }

    @PostMapping
    public ResponseEntity<EmployeResponse> create(@Valid @RequestBody EmployeRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        EmployeResponse response = isManager(principal)
                ? employeService.createForManager(request, principal.getId())
                : employeService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<EmployeResponse>> findAll(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(isManager(principal) ? employeService.findEquipe(principal.getId()) : employeService.findAll());
    }

    @GetMapping("/actifs")
    public ResponseEntity<List<EmployeResponse>> findActifs() {
        return ResponseEntity.ok(employeService.findActifs());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeResponse> findById(@PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal) {
        if (principal != null && (isManager(principal) || principal.getId().equals(id))) {
            return ResponseEntity.ok(employeService.findAccessible(id, principal.getId(), isManager(principal)));
        }
        return ResponseEntity.ok(employeService.findById(id));
    }

    @GetMapping("/{managerId}/equipe")
    public ResponseEntity<List<EmployeResponse>> findEquipe(@PathVariable Long managerId) {
        return ResponseEntity.ok(employeService.findEquipe(managerId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody EmployeRequest request,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        return ResponseEntity.ok(isManager(principal)
                ? employeService.updateForManager(id, request, principal.getId())
                : employeService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal) {
        if (isManager(principal)) employeService.deleteForManager(id, principal.getId());
        else employeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<EmployeResponse> me(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(employeService.findById(principal.getId()));
    }

    @PutMapping("/me/password")
    public ResponseEntity<Void> updateOwnPassword(@Valid @RequestBody MotDePasseModificationRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        employeService.updateOwnPassword(principal.getId(), request);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/active/{active}")
    public ResponseEntity<EmployeResponse> setActive(@PathVariable Long id, @PathVariable boolean active,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(isManager(principal)
                ? employeService.setActiveForManager(id, active, principal.getId())
                : employeService.setActive(id, active));
    }

    @PatchMapping("/{id}/password/default")
    public ResponseEntity<Void> resetPasswordToDefault(@PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal) {
        if (isManager(principal)) employeService.resetPasswordToDefaultForManager(id, principal.getId());
        else employeService.resetPasswordToDefault(id);
        return ResponseEntity.noContent().build();
    }

    private boolean isManager(CustomUserDetails principal) {
        return principal != null && principal.getRole() == com.xtensus.hrmanagementapi.domain.enums.RoleType.MANAGER;
    }
}
