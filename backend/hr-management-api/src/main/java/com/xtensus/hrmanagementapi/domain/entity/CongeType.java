package com.xtensus.hrmanagementapi.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "conge_types")
@Getter
@Setter
@NoArgsConstructor
public class CongeType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "conge_type_id")
    private Long id;

    @Column(name = "conge_type_nom", nullable = false, unique = true, length = 100)
    private String nom;

    @Column(name = "conge_type_description", length = 255)
    private String description;

    @Column(name = "conge_type_jours_maximum", precision = 5, scale = 2)
    private BigDecimal joursMaximum;

    @Column(name = "conge_type_certificat_obligatoire", nullable = false)
    private Boolean certificatObligatoire;

    @Column(name = "conge_type_remunere", nullable = false)
    private Boolean remunere;

    @Column(name = "conge_type_actif", nullable = false)
    private Boolean actif;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;

    @Column(name = "date_modification")
    private LocalDateTime dateModification;
}
