package com.xtensus.hrmanagementapi.typecontrat.controller;

import com.xtensus.hrmanagementapi.typecontrat.dto.TypeContratRequest;
import com.xtensus.hrmanagementapi.typecontrat.dto.TypeContratResponse;
import com.xtensus.hrmanagementapi.typecontrat.service.TypeContratService;
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
@RequestMapping("/api/type-contrats")
public class TypeContratController {

    private final TypeContratService typeContratService;

    public TypeContratController(TypeContratService typeContratService) {
        this.typeContratService = typeContratService;
    }

    @PostMapping
    public ResponseEntity<TypeContratResponse> creer(@Valid @RequestBody TypeContratRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(typeContratService.creer(request));
    }

    @GetMapping
    public ResponseEntity<List<TypeContratResponse>> lister() {
        return ResponseEntity.ok(typeContratService.lister());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TypeContratResponse> trouverParId(@PathVariable Long id) {
        return ResponseEntity.ok(typeContratService.trouverParId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TypeContratResponse> modifier(@PathVariable Long id, @Valid @RequestBody TypeContratRequest request) {
        return ResponseEntity.ok(typeContratService.modifier(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        typeContratService.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}
