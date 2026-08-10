package com.xtensus.hrmanagementapi.conge.demande.historique;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/conge-demande-historiques")
public class CongeDemandeHistoriqueController {
    private final CongeDemandeHistoriqueService service;
    public CongeDemandeHistoriqueController(CongeDemandeHistoriqueService service) { this.service = service; }
    @GetMapping public List<CongeDemandeHistoriqueResponse> lister() { return service.lister(); }
    @PutMapping("/{id}") public CongeDemandeHistoriqueResponse modifier(@PathVariable Long id, @Valid @RequestBody CongeDemandeHistoriqueRequest request) { return service.modifier(id, request); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> supprimer(@PathVariable Long id) { service.supprimer(id); return ResponseEntity.noContent().build(); }
}
