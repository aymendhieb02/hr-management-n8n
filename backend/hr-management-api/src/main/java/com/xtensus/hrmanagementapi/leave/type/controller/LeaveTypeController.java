package com.xtensus.hrmanagementapi.leave.type.controller;

import com.xtensus.hrmanagementapi.leave.type.dto.LeaveTypeRequest;
import com.xtensus.hrmanagementapi.leave.type.dto.LeaveTypeResponse;
import com.xtensus.hrmanagementapi.leave.type.service.LeaveTypeService;
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
@RequestMapping("/api/leave-types")
public class LeaveTypeController {

    private final LeaveTypeService leaveTypeService;

    public LeaveTypeController(LeaveTypeService leaveTypeService) {
        this.leaveTypeService = leaveTypeService;
    }

    @PostMapping
    public ResponseEntity<LeaveTypeResponse> create(@Valid @RequestBody LeaveTypeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(leaveTypeService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<LeaveTypeResponse>> findAll() {
        return ResponseEntity.ok(leaveTypeService.findAll());
    }

    @GetMapping("/active")
    public ResponseEntity<List<LeaveTypeResponse>> findActive() {
        return ResponseEntity.ok(leaveTypeService.findActive());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LeaveTypeResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(leaveTypeService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LeaveTypeResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody LeaveTypeRequest request
    ) {
        return ResponseEntity.ok(leaveTypeService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        leaveTypeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}


