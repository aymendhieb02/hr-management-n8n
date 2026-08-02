package com.xtensus.hrmanagementapi.employe.controller;

import com.xtensus.hrmanagementapi.employe.dto.EmployeRequest;
import com.xtensus.hrmanagementapi.employe.dto.EmployeResponse;
import com.xtensus.hrmanagementapi.employe.service.EmployeService;
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
@RequestMapping("/api/employes")
public class EmployeController {

    private final EmployeService employeService;

    public EmployeController(EmployeService employeService) {
        this.employeService = employeService;
    }

    @PostMapping
    public ResponseEntity<EmployeResponse> create(@Valid @RequestBody EmployeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<EmployeResponse>> findAll() {
        return ResponseEntity.ok(employeService.findAll());
    }

    @GetMapping("/actifs")
    public ResponseEntity<List<EmployeResponse>> findActifs() {
        return ResponseEntity.ok(employeService.findActifs());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(employeService.findById(id));
    }

    @GetMapping("/{managerId}/equipe")
    public ResponseEntity<List<EmployeResponse>> findEquipe(@PathVariable Long managerId) {
        return ResponseEntity.ok(employeService.findEquipe(managerId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody EmployeRequest request
    ) {
        return ResponseEntity.ok(employeService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        employeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
