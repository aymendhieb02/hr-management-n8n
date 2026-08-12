package com.xtensus.hrmanagementapi.conge.solde.controller;

import com.xtensus.hrmanagementapi.conge.solde.dto.CongeSoldeRequest;
import com.xtensus.hrmanagementapi.conge.solde.dto.CongeSoldeResponse;
import com.xtensus.hrmanagementapi.conge.solde.service.CongeSoldeService;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.xtensus.hrmanagementapi.security.user.CustomUserDetails;

@RestController
@RequestMapping("/api/conge-soldes-v2")
public class CongeSoldeController {
    private final CongeSoldeService service;

    public CongeSoldeController(CongeSoldeService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<CongeSoldeResponse> creer(@Valid @RequestBody CongeSoldeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.creer(request));
    }

    @GetMapping
    public ResponseEntity<List<CongeSoldeResponse>> lister() {
        return ResponseEntity.ok(service.lister());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CongeSoldeResponse> trouverParId(@PathVariable Long id) {
        return ResponseEntity.ok(service.trouverParId(id));
    }

    @GetMapping("/employe/{employeId}")
    public ResponseEntity<List<CongeSoldeResponse>> parEmploye(@PathVariable Long employeId) {
        return ResponseEntity.ok(service.parEmploye(employeId));
    }

    @GetMapping("/me")
    public ResponseEntity<List<CongeSoldeResponse>> monSolde(@AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(service.monSolde(user.getId()));
    }

    @GetMapping("/annee/{annee}")
    public ResponseEntity<List<CongeSoldeResponse>> parAnnee(@PathVariable Integer annee) {
        return ResponseEntity.ok(service.parAnnee(annee));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CongeSoldeResponse> modifier(@PathVariable Long id, @Valid @RequestBody CongeSoldeRequest request) {
        return ResponseEntity.ok(service.modifier(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        service.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}
