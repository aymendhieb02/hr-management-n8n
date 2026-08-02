package com.xtensus.hrmanagementapi.conge.statut.controller;

import com.xtensus.hrmanagementapi.conge.statut.dto.CongeDemandeStatutRequest;
import com.xtensus.hrmanagementapi.conge.statut.dto.CongeDemandeStatutResponse;
import com.xtensus.hrmanagementapi.conge.statut.service.CongeDemandeStatutService;
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
@RequestMapping("/api/conge-demande-statuts")
public class CongeDemandeStatutController {

    private final CongeDemandeStatutService service;

    public CongeDemandeStatutController(CongeDemandeStatutService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<CongeDemandeStatutResponse> creer(@Valid @RequestBody CongeDemandeStatutRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.creer(request));
    }

    @GetMapping
    public ResponseEntity<List<CongeDemandeStatutResponse>> lister() {
        return ResponseEntity.ok(service.lister());
    }

    @GetMapping("/actifs")
    public ResponseEntity<List<CongeDemandeStatutResponse>> listerActifs() {
        return ResponseEntity.ok(service.listerActifs());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CongeDemandeStatutResponse> trouverParId(@PathVariable Long id) {
        return ResponseEntity.ok(service.trouverParId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CongeDemandeStatutResponse> modifier(@PathVariable Long id, @Valid @RequestBody CongeDemandeStatutRequest request) {
        return ResponseEntity.ok(service.modifier(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        service.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}
