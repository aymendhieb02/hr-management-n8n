package com.xtensus.hrmanagementapi.conge.demande.controller;

import com.xtensus.hrmanagementapi.conge.demande.dto.CongeDecisionRequest;
import com.xtensus.hrmanagementapi.conge.demande.dto.CongeDemandeCreationRequest;
import com.xtensus.hrmanagementapi.conge.demande.dto.CongeDemandeModificationRequest;
import com.xtensus.hrmanagementapi.conge.demande.dto.CongeDemandeResponse;
import com.xtensus.hrmanagementapi.conge.demande.dto.ConsommationReelleRequest;
import com.xtensus.hrmanagementapi.domain.enums.RoleType;
import com.xtensus.hrmanagementapi.conge.demande.service.CongeDemandeService;
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
@RequestMapping("/api/conge-demandes-v2")
public class CongeDemandeController {
    private final CongeDemandeService service;

    public CongeDemandeController(CongeDemandeService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<CongeDemandeResponse> creer(@Valid @RequestBody CongeDemandeCreationRequest request, @AuthenticationPrincipal CustomUserDetails principal) {
        if (principal == null || !principal.getId().equals(request.getEmployeId())) throw new com.xtensus.hrmanagementapi.conge.demande.exception.DecisionCongeNonAutoriseeException("Vous ne pouvez creer que vos propres brouillons");
        return ResponseEntity.status(HttpStatus.CREATED).body(service.creer(request));
    }

    @PostMapping("/{id}/soumettre")
    public ResponseEntity<CongeDemandeResponse> soumettre(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(service.soumettre(id, principal.getId()));
    }

    @GetMapping
    public ResponseEntity<List<CongeDemandeResponse>> lister() {
        return ResponseEntity.ok(service.lister());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CongeDemandeResponse> trouverParId(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(service.trouverParId(id, principal.getId()));
    }

    @GetMapping("/employe/{employeId}")
    public ResponseEntity<List<CongeDemandeResponse>> parEmploye(@PathVariable Long employeId, @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(service.parEmploye(employeId, principal.getId()));
    }

    @GetMapping("/decideur/{decideurId}")
    public ResponseEntity<List<CongeDemandeResponse>> parDecideur(@PathVariable Long decideurId, @AuthenticationPrincipal CustomUserDetails principal) {
        if(principal==null||!principal.getId().equals(decideurId)) throw new com.xtensus.hrmanagementapi.conge.demande.exception.DecisionCongeNonAutoriseeException("Consultation non autorisee");
        return ResponseEntity.ok(service.parDecideur(decideurId,principal.getRole()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CongeDemandeResponse> modifier(
            @PathVariable Long id,
            @Valid @RequestBody CongeDemandeModificationRequest request, @AuthenticationPrincipal CustomUserDetails principal
    ) {
        return ResponseEntity.ok(service.modifier(id, request, principal.getId()));
    }

    @PostMapping("/{id}/approuver")
    public ResponseEntity<CongeDemandeResponse> approuver(
            @PathVariable Long id,
            @Valid @RequestBody CongeDecisionRequest request
    ) {
        return ResponseEntity.ok(service.approuver(id, request));
    }

    @PostMapping("/{id}/refuser")
    public ResponseEntity<CongeDemandeResponse> refuser(
            @PathVariable Long id,
            @Valid @RequestBody CongeDecisionRequest request
    ) {
        return ResponseEntity.ok(service.refuser(id, request));
    }

    @PostMapping("/{id}/consommation-reelle")
    public ResponseEntity<CongeDemandeResponse> ajusterConsommation(@PathVariable Long id,
            @Valid @RequestBody ConsommationReelleRequest request, @AuthenticationPrincipal CustomUserDetails principal) {
        if (principal == null || (principal.getRole() != RoleType.DG && principal.getRole() != RoleType.DT)) {
            throw new com.xtensus.hrmanagementapi.conge.demande.exception.DecisionCongeNonAutoriseeException("Seuls DG et DT peuvent regulariser une consommation");
        }
        return ResponseEntity.ok(service.ajusterConsommation(id, request.nombreJoursConsommes(), request.commentaire(), principal.getId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails principal) {
        service.supprimer(id, principal.getId());
        return ResponseEntity.noContent().build();
    }
}
