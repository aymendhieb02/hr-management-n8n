package com.xtensus.hrmanagementapi.variable;

import com.xtensus.hrmanagementapi.domain.entity.VariableSysteme;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/variables-systeme")
public class VariableSystemeController {
    private final VariableSystemeService service;
    public VariableSystemeController(VariableSystemeService service) { this.service = service; }
    @GetMapping public List<VariableResponse> lister() { return service.lister().stream().map(this::response).toList(); }
    @GetMapping("/politique-conges") public PolitiqueConge politique() {
        boolean negatif=service.booleen("solde_negatif",true);
        return new PolitiqueConge(service.decimal("solde_conge_par_mois","2.16"), negatif,
                negatif?service.decimal("solde_negatif_max","-5"):java.math.BigDecimal.ZERO,
                service.entier("reinitialisation_solde",2));
    }
    @PutMapping("/{id}") public ResponseEntity<VariableResponse> modifier(@PathVariable Long id, @RequestBody Modification request) {
        return ResponseEntity.ok(response(service.modifier(id, request.valeur())));
    }
    private VariableResponse response(VariableSysteme v) {
        boolean secret = service.estSecret(v);
        return new VariableResponse(v.getId(), v.getNom(), v.getLibelle(), v.getDescription(), v.getType(),
                secret ? "" : v.getValeur(), secret && v.getValeur() != null && !v.getValeur().isBlank());
    }
    public record Modification(@NotBlank String valeur) {}
    public record VariableResponse(Long id, String nom, String libelle, String description, String type,
            String valeur, boolean secretConfigure) {}
    public record PolitiqueConge(java.math.BigDecimal acquisitionMensuelle, boolean soldeNegatifAutorise,
            java.math.BigDecimal soldeMinimum, int anneesConservation) {}
}
