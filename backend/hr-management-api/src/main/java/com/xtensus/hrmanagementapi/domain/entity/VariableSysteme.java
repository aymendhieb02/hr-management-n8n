package com.xtensus.hrmanagementapi.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "variables")
@Getter @Setter @NoArgsConstructor
public class VariableSysteme {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "variable_id")
    private Long id;
    @Column(name = "variable_nom", nullable = false, unique = true, length = 100)
    private String nom;
    @Column(name = "variable_libelle", nullable = false, length = 150)
    private String libelle;
    @Column(name = "variable_description", length = 500)
    private String description;
    @Column(name = "variable_type", nullable = false, length = 30)
    private String type;
    @Column(name = "variable_valeur", nullable = false, length = 100)
    private String valeur;
}
