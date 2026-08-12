package com.xtensus.hrmanagementapi.conge.demande.historique;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.xtensus.hrmanagementapi.domain.enums.RoleType;
import com.xtensus.hrmanagementapi.security.user.CustomUserDetails;

@RestController
@RequestMapping("/api/conge-demande-historiques")
public class CongeDemandeHistoriqueController {
    private final CongeDemandeHistoriqueService service;
    public CongeDemandeHistoriqueController(CongeDemandeHistoriqueService service) { this.service = service; }
    @GetMapping public List<CongeDemandeHistoriqueResponse> lister(@AuthenticationPrincipal CustomUserDetails user) { return user.getRole() == RoleType.EMPLOYEE ? service.listerParEmploye(user.getId()) : service.lister(); }
    @GetMapping("/page")
    public CongeDemandeHistoriquePageResponse rechercher(
            @RequestParam(required = false) String recherche,
            @RequestParam(required = false) Long employeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int taille, @AuthenticationPrincipal CustomUserDetails user) {
        Long filtreEmploye = user.getRole() == RoleType.EMPLOYEE ? user.getId() : employeId;
        return service.rechercher(recherche, filtreEmploye, page, Math.min(Math.max(taille, 1), 100));
    }
    @GetMapping("/employes")
    public List<CongeDemandeHistoriqueEmployeResponse> listerEmployes(@AuthenticationPrincipal CustomUserDetails user) { return user.getRole() == RoleType.EMPLOYEE ? List.of() : service.listerEmployes(); }
    @PutMapping("/{id}") public CongeDemandeHistoriqueResponse modifier(@PathVariable Long id, @Valid @RequestBody CongeDemandeHistoriqueRequest request) { return service.modifier(id, request); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> supprimer(@PathVariable Long id) { service.supprimer(id); return ResponseEntity.noContent().build(); }
}
