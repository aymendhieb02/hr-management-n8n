package com.xtensus.hrmanagementapi.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "postes")
@Getter
@Setter
@NoArgsConstructor
public class Poste {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "poste_id")
    private Long id;

    @Column(name = "poste_intitule", nullable = false, unique = true, length = 100)
    private String intitule;

    @Column(name = "poste_description", length = 255)
    private String description;

    @Column(name = "poste_niveau_poste", length = 100)
    private String niveauPoste;

    @Column(name = "poste_actif", nullable = false)
    private Boolean actif;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;

    @Column(name = "date_modification")
    private LocalDateTime dateModification;
}
