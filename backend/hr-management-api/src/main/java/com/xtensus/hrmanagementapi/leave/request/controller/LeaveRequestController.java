package com.xtensus.hrmanagementapi.leave.request.controller;

import com.xtensus.hrmanagementapi.leave.request.dto.LeaveRequestCreateRequest;
import com.xtensus.hrmanagementapi.leave.request.dto.LeaveRequestResponse;
import com.xtensus.hrmanagementapi.leave.request.dto.LeaveRequestUpdateRequest;
import com.xtensus.hrmanagementapi.leave.request.dto.LeaveApprovalRequest;
import com.xtensus.hrmanagementapi.leave.request.dto.LeaveRejectionRequest;
import com.xtensus.hrmanagementapi.leave.request.service.LeaveRequestService;
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
@RequestMapping("/api/leave-requests")
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;

    public LeaveRequestController(LeaveRequestService leaveRequestService) {
        this.leaveRequestService = leaveRequestService;
    }

    @PostMapping
    public ResponseEntity<LeaveRequestResponse> create(@Valid @RequestBody LeaveRequestCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(leaveRequestService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<LeaveRequestResponse>> findAll() {
        return ResponseEntity.ok(leaveRequestService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LeaveRequestResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(leaveRequestService.findById(id));
    }

    @GetMapping("/requester/{id}")
    public ResponseEntity<List<LeaveRequestResponse>> findByRequester(@PathVariable Long id) {
        return ResponseEntity.ok(leaveRequestService.findByRequester(id));
    }

    @GetMapping("/approver/{id}")
    public ResponseEntity<List<LeaveRequestResponse>> findByApprover(@PathVariable Long id) {
        return ResponseEntity.ok(leaveRequestService.findByApprover(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LeaveRequestResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody LeaveRequestUpdateRequest request
    ) {
        return ResponseEntity.ok(leaveRequestService.update(id, request));
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<LeaveRequestResponse> approve(
            @PathVariable Long id,
            @Valid @RequestBody LeaveApprovalRequest request
    ) {
        return ResponseEntity.ok(leaveRequestService.approve(id, request));
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<LeaveRequestResponse> reject(
            @PathVariable Long id,
            @Valid @RequestBody LeaveRejectionRequest request
    ) {
        return ResponseEntity.ok(leaveRequestService.reject(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        leaveRequestService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
