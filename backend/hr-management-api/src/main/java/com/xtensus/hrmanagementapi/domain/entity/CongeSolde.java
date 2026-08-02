package com.xtensus.hrmanagementapi.domain.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "conge_soldes")
@Getter @Setter @NoArgsConstructor
public class CongeSolde {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "conge_solde_id")
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "employe_id", nullable = false)
    private Employe employe;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "conge_type_id", nullable = false)
    private CongeType congeType;
    @Column(name = "conge_solde_jours_utilises", nullable = false, precision = 5, scale = 2)
    private BigDecimal joursUtilises;
    @Column(name = "conge_solde_restants", nullable = false, precision = 5, scale = 2)
    private BigDecimal restants;
    @Column(name = "conge_solde_annee", nullable = false)
    private Integer annee;
    @Column(name = "conge_solde_droit_acquis", nullable = false, precision = 5, scale = 2)
    private BigDecimal droitAcquis;
    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;
    @Column(name = "date_modification")
    private LocalDateTime dateModification;
}
