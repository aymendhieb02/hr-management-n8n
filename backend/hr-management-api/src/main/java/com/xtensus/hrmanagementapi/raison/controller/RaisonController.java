package com.xtensus.hrmanagementapi.raison.controller;

import com.xtensus.hrmanagementapi.raison.dto.RaisonRequest;
import com.xtensus.hrmanagementapi.raison.dto.RaisonResponse;
import com.xtensus.hrmanagementapi.raison.service.RaisonService;
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
@RequestMapping("/api/raisons")
public class RaisonController {

    private final RaisonService service;

    public RaisonController(RaisonService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<RaisonResponse> creer(@Valid @RequestBody RaisonRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.creer(request));
    }

    @GetMapping
    public ResponseEntity<List<RaisonResponse>> lister() {
        return ResponseEntity.ok(service.lister());
    }

    @GetMapping("/disponibles")
    public ResponseEntity<List<RaisonResponse>> listerDisponibles() {
        return ResponseEntity.ok(service.listerDisponibles());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RaisonResponse> trouverParId(@PathVariable Long id) {
        return ResponseEntity.ok(service.trouverParId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RaisonResponse> modifier(@PathVariable Long id, @Valid @RequestBody RaisonRequest request) {
        return ResponseEntity.ok(service.modifier(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        service.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}
