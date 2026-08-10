package com.xtensus.hrmanagementapi.jourferie.controller;

import com.xtensus.hrmanagementapi.jourferie.dto.JourFerieRequest;
import com.xtensus.hrmanagementapi.jourferie.dto.JourFerieResponse;
import com.xtensus.hrmanagementapi.jourferie.service.JourFerieService;
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
@RequestMapping("/api/jours-feries")
public class JourFerieController {

    private final JourFerieService service;

    public JourFerieController(JourFerieService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<JourFerieResponse> creer(@Valid @RequestBody JourFerieRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.creer(request));
    }

    @GetMapping
    public ResponseEntity<List<JourFerieResponse>> lister() {
        return ResponseEntity.ok(service.lister());
    }

    @GetMapping("/actifs")
    public ResponseEntity<List<JourFerieResponse>> listerActifs() {
        return ResponseEntity.ok(service.listerActifs());
    }

    @GetMapping("/{id}")
    public ResponseEntity<JourFerieResponse> trouverParId(@PathVariable Long id) {
        return ResponseEntity.ok(service.trouverParId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<JourFerieResponse> modifier(@PathVariable Long id, @Valid @RequestBody JourFerieRequest request) {
        return ResponseEntity.ok(service.modifier(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        service.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}
