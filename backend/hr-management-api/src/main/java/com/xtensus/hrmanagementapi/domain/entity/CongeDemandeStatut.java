package com.xtensus.hrmanagementapi.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "conge_demande_statuts")
@Getter
@Setter
@NoArgsConstructor
public class CongeDemandeStatut {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "conge_demande_statut_id")
    private Long id;

    @Column(name = "conge_demande_statut_libelle", nullable = false, unique = true, length = 100)
    private String libelle;

    @Column(name = "conge_demande_statut_description", length = 255)
    private String description;

    @Column(name = "conge_demande_statut_actif", nullable = false)
    private Boolean actif;
}
