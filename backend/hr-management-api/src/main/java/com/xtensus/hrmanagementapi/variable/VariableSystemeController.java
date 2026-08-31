package com.xtensus.hrmanagementapi.variable;

import com.xtensus.hrmanagementapi.domain.entity.VariableSysteme;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.xtensus.hrmanagementapi.conge.solde.service.CongeSoldeAcquisitionPlanifiee;
import java.time.LocalDate;
import java.util.stream.IntStream;
import org.springframework.transaction.annotation.Transactional;

@RestController
@RequestMapping("/api/variables-systeme")
public class VariableSystemeController {
    private final VariableSystemeService service;
    private final CongeSoldeAcquisitionPlanifiee acquisition;
    public VariableSystemeController(VariableSystemeService service, CongeSoldeAcquisitionPlanifiee acquisition) { this.service = service; this.acquisition = acquisition; }
    @PostMapping("/solde-conge-par-mois/tester")
    @Transactional
    public TestAcquisition testerAcquisition() {
        LocalDate debut = LocalDate.now();
        LocalDate fin = debut.plusDays(15);
        int comptes = IntStream.rangeClosed(0, 15)
                .map(offset -> acquisition.executerPourDateTest(debut.plusDays(offset)))
                .sum();
        return new TestAcquisition(debut, fin, service.decimal("solde_conge_par_mois", "2.16"), comptes);
    }
    @PostMapping("/solde-conge-par-mois/retourner")
    @Transactional
    public RetourAcquisition retournerAcquisition() {
        int comptes = acquisition.annulerAcquisitionsTest("TEST_ACQUISITION:");
        return new RetourAcquisition(comptes);
    }
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
    public record TestAcquisition(LocalDate debut, LocalDate fin, java.math.BigDecimal montant, int comptesCredités) {}
    public record RetourAcquisition(int comptesAnnulés) {}
}
