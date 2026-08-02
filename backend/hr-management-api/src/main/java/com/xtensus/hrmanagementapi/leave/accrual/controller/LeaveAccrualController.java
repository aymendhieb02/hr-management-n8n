package com.xtensus.hrmanagementapi.leave.accrual.controller;

import com.xtensus.hrmanagementapi.leave.accrual.dto.LeaveAccrualResponse;
import com.xtensus.hrmanagementapi.leave.accrual.dto.LeaveAccrualRunSummary;
import com.xtensus.hrmanagementapi.leave.accrual.service.LeaveAccrualService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/leave-accruals", "/api/acquisitions-conges"})
public class LeaveAccrualController {

    private final LeaveAccrualService leaveAccrualService;

    public LeaveAccrualController(LeaveAccrualService leaveAccrualService) {
        this.leaveAccrualService = leaveAccrualService;
    }

    @PostMapping("/run")
    public ResponseEntity<LeaveAccrualRunSummary> run(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month
    ) {
        return ResponseEntity.ok(leaveAccrualService.run(year, month));
    }

    @GetMapping
    public ResponseEntity<List<LeaveAccrualResponse>> findAll() {
        return ResponseEntity.ok(leaveAccrualService.findAll());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<LeaveAccrualResponse>> findByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(leaveAccrualService.findByUser(userId));
    }

    @GetMapping("/year/{year}/month/{month}")
    public ResponseEntity<List<LeaveAccrualResponse>> findByYearAndMonth(
            @PathVariable Integer year,
            @PathVariable Integer month
    ) {
        return ResponseEntity.ok(leaveAccrualService.findByYearAndMonth(year, month));
    }
}

