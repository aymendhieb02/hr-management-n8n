package com.xtensus.hrmanagementapi.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "conge_demandes")
@Getter
@Setter
@NoArgsConstructor
public class CongeDemande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "conge_demande_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employe_id", nullable = false)
    private Employe employe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "decideur_id")
    private Employe decideur;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conge_type_id", nullable = false)
    private CongeType congeType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conge_demande_statut_id", nullable = false)
    private CongeDemandeStatut statut;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "raison_id")
    private Raison raison;

    @Column(name = "conge_demande_nature", nullable = false, length = 30)
    private String nature;

    @Column(name = "conge_demande_date_debut", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "conge_demande_heure_debut")
    private LocalTime heureDebut;

    @Column(name = "conge_demande_date_fin", nullable = false)
    private LocalDate dateFin;

    @Column(name = "conge_demande_heure_fin")
    private LocalTime heureFin;

    @Column(name = "conge_demande_date_soumission", nullable = false)
    private LocalDateTime dateSoumission;

    @Column(name = "conge_demande_nombre_jours", nullable = false, precision = 5, scale = 2)
    private BigDecimal nombreJours;

    @Column(name = "conge_demande_commentaire_employe", length = 1000)
    private String commentaireEmploye;

    @Column(name = "conge_demande_commentaire_decision", length = 1000)
    private String commentaireDecision;

    @Column(name = "conge_demande_date_decision")
    private LocalDateTime dateDecision;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;

    @Column(name = "date_modification")
    private LocalDateTime dateModification;
}
