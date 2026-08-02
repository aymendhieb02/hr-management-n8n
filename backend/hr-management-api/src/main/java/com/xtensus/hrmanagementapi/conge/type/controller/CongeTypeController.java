package com.xtensus.hrmanagementapi.conge.type.controller;

import com.xtensus.hrmanagementapi.conge.type.dto.CongeTypeRequest;
import com.xtensus.hrmanagementapi.conge.type.dto.CongeTypeResponse;
import com.xtensus.hrmanagementapi.conge.type.service.CongeTypeService;
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
@RequestMapping("/api/conge-types")
public class CongeTypeController {

    private final CongeTypeService congeTypeService;

    public CongeTypeController(CongeTypeService congeTypeService) {
        this.congeTypeService = congeTypeService;
    }

    @PostMapping
    public ResponseEntity<CongeTypeResponse> creer(@Valid @RequestBody CongeTypeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(congeTypeService.creer(request));
    }

    @GetMapping
    public ResponseEntity<List<CongeTypeResponse>> lister() {
        return ResponseEntity.ok(congeTypeService.lister());
    }

    @GetMapping("/actifs")
    public ResponseEntity<List<CongeTypeResponse>> listerActifs() {
        return ResponseEntity.ok(congeTypeService.listerActifs());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CongeTypeResponse> trouverParId(@PathVariable Long id) {
        return ResponseEntity.ok(congeTypeService.trouverParId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CongeTypeResponse> modifier(@PathVariable Long id, @Valid @RequestBody CongeTypeRequest request) {
        return ResponseEntity.ok(congeTypeService.modifier(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        congeTypeService.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}
