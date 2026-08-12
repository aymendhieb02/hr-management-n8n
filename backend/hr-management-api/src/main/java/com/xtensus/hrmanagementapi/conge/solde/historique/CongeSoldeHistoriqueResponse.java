package com.xtensus.hrmanagementapi.conge.solde.historique;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class CongeSoldeHistoriqueResponse {
    private Long id;
    private Long soldeId;
    private Long employeId;
    private String employeNomComplet;
    private Long demandeId;
    private String typeTransaction;
    private BigDecimal montant;
    private BigDecimal soldeAvant;
    private BigDecimal soldeApres;
    private LocalDateTime dateExecution;
    private String statut;
    private String erreur;
}
