package com.xtensus.hrmanagementapi.conge.solde.historique;

import com.xtensus.hrmanagementapi.security.user.CustomUserDetails;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/conge-solde-historiques")
public class CongeSoldeHistoriqueController {
    private final CongeSoldeHistoriqueService service;
    public CongeSoldeHistoriqueController(CongeSoldeHistoriqueService service) { this.service = service; }
    @GetMapping("/me") public ResponseEntity<List<CongeSoldeHistoriqueResponse>> me(@AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(service.parEmploye(user.getId()));
    }
    @GetMapping public ResponseEntity<List<CongeSoldeHistoriqueResponse>> tous() { return ResponseEntity.ok(service.tous()); }
}
