package com.xtensus.hrmanagementapi.leave.balance.controller;

import com.xtensus.hrmanagementapi.leave.balance.dto.LeaveBalanceRequest;
import com.xtensus.hrmanagementapi.leave.balance.dto.LeaveBalanceResponse;
import com.xtensus.hrmanagementapi.leave.balance.service.LeaveBalanceService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/leave-balances", "/api/conge-soldes"})
public class LeaveBalanceController {

    private final LeaveBalanceService leaveBalanceService;

    public LeaveBalanceController(LeaveBalanceService leaveBalanceService) {
        this.leaveBalanceService = leaveBalanceService;
    }

    @PostMapping
    public ResponseEntity<LeaveBalanceResponse> create(@Valid @RequestBody LeaveBalanceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(leaveBalanceService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<LeaveBalanceResponse>> findAll() {
        return ResponseEntity.ok(leaveBalanceService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LeaveBalanceResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(leaveBalanceService.findById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<LeaveBalanceResponse>> findByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(leaveBalanceService.findByUser(userId));
    }

    @GetMapping("/user/{userId}/year/{year}")
    public ResponseEntity<List<LeaveBalanceResponse>> findByUserAndYear(
            @PathVariable Long userId,
            @PathVariable Integer year
    ) {
        return ResponseEntity.ok(leaveBalanceService.findByUserAndYear(userId, year));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LeaveBalanceResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody LeaveBalanceRequest request
    ) {
        return ResponseEntity.ok(leaveBalanceService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        leaveBalanceService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

