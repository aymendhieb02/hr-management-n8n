package com.xtensus.hrmanagementapi.user.controller;

import com.xtensus.hrmanagementapi.domain.enums.RoleType;
import com.xtensus.hrmanagementapi.user.dto.PasswordUpdateRequest;
import com.xtensus.hrmanagementapi.user.dto.UserCreateRequest;
import com.xtensus.hrmanagementapi.user.dto.UserResponse;
import com.xtensus.hrmanagementapi.user.dto.UserUpdateRequest;
import com.xtensus.hrmanagementapi.user.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> findAll() {
        return ResponseEntity.ok(userService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    @GetMapping("/role/{role}")
    public ResponseEntity<List<UserResponse>> findByRole(@PathVariable RoleType role) {
        return ResponseEntity.ok(userService.findByRole(role));
    }

    @GetMapping("/department/{departmentId}")
    public ResponseEntity<List<UserResponse>> findByDepartment(@PathVariable Long departmentId) {
        return ResponseEntity.ok(userService.findByDepartment(departmentId));
    }

    @GetMapping("/{managerId}/team")
    public ResponseEntity<List<UserResponse>> findTeamMembers(@PathVariable Long managerId) {
        return ResponseEntity.ok(userService.findTeamMembers(managerId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request
    ) {
        return ResponseEntity.ok(userService.update(id, request));
    }

    @PatchMapping("/{id}/password")
    public ResponseEntity<UserResponse> updatePassword(
            @PathVariable Long id,
            @Valid @RequestBody PasswordUpdateRequest request
    ) {
        return ResponseEntity.ok(userService.updatePassword(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

