package com.xtensus.hrmanagementapi.domain.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "conge_demande_historiques")
@Getter @Setter @NoArgsConstructor
public class CongeDemandeHistorique {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "conge_demande_historique_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conge_demande_id", nullable = false)
    private CongeDemande demande;

    @Column(name = "conge_demande_historique_date_action", nullable = false)
    private LocalDateTime dateAction;

    @Column(name = "conge_demande_historique_commentaire", length = 1000)
    private String commentaire;

    @Column(name = "conge_demande_historique_action", nullable = false, length = 50)
    private String action;

    @Column(name = "conge_demande_historique_ancien_statut", length = 50)
    private String ancienStatut;

    @Column(name = "conge_demande_historique_nouveau_statut", length = 50)
    private String nouveauStatut;
}
