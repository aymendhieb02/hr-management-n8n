package com.xtensus.hrmanagementapi.poste.controller;

import com.xtensus.hrmanagementapi.poste.dto.PosteRequest;
import com.xtensus.hrmanagementapi.poste.dto.PosteResponse;
import com.xtensus.hrmanagementapi.poste.service.PosteService;
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
@RequestMapping("/api/postes")
public class PosteController {

    private final PosteService posteService;

    public PosteController(PosteService posteService) {
        this.posteService = posteService;
    }

    @PostMapping
    public ResponseEntity<PosteResponse> creer(@Valid @RequestBody PosteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(posteService.creer(request));
    }

    @GetMapping
    public ResponseEntity<List<PosteResponse>> lister() {
        return ResponseEntity.ok(posteService.lister());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PosteResponse> trouverParId(@PathVariable Long id) {
        return ResponseEntity.ok(posteService.trouverParId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PosteResponse> modifier(@PathVariable Long id, @Valid @RequestBody PosteRequest request) {
        return ResponseEntity.ok(posteService.modifier(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        posteService.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}
