package com.xtensus.hrmanagementapi.domain.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "conge_solde_historiques")
@Getter @Setter @NoArgsConstructor
public class CongeSoldeHistorique {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "conge_solde_historique_id")
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "conge_solde_id", nullable = false)
    private CongeSolde solde;
    @Column(name = "conge_solde_historique_solde_avant", nullable = false, precision = 5, scale = 2)
    private BigDecimal soldeAvant;
    @Column(name = "conge_solde_historique_solde_apres", nullable = false, precision = 5, scale = 2)
    private BigDecimal soldeApres;
    @Column(name = "conge_solde_historique_date_execution", nullable = false)
    private LocalDateTime dateExecution;
    @Column(name = "conge_solde_historique_statut_acquisition", nullable = false, length = 50)
    private String statutAcquisition;
    @Column(name = "conge_solde_historique_erreur", length = 500)
    private String erreur;
    @Column(name = "conge_solde_historique_type_transaction", nullable = false, length = 50)
    private String typeTransaction;
    @Column(name = "conge_solde_historique_montant", nullable = false, precision = 5, scale = 2)
    private BigDecimal montant;
    @Column(name = "conge_solde_historique_reference", nullable = false, unique = true, length = 100)
    private String reference;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "conge_demande_id")
    private CongeDemande demande;
}
