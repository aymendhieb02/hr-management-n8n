package com.xtensus.hrmanagementapi.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "jours_feries")
@Getter
@Setter
@NoArgsConstructor
public class JourFerie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "jour_ferie_id")
    private Long id;

    @Column(name = "jour_ferie_nom", nullable = false, length = 150)
    private String nom;

    @Column(name = "jour_ferie_date", nullable = false, unique = true)
    private LocalDate date;

    @Column(name = "jour_ferie_description", length = 255)
    private String description;

    @Column(name = "jour_ferie_actif", nullable = false)
    private Boolean actif = true;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;

    @Column(name = "date_modification")
    private LocalDateTime dateModification;
}
