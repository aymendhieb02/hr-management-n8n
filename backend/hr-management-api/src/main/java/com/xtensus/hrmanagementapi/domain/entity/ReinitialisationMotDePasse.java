package com.xtensus.hrmanagementapi.domain.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "reinitialisations_mot_de_passe")
@Getter @Setter @NoArgsConstructor
public class ReinitialisationMotDePasse {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reinitialisation_id") private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employe_id", nullable = false) private Employe employe;
    @Column(name = "code_hash", nullable = false, length = 255) private String codeHash;
    @Column(name = "date_expiration", nullable = false) private LocalDateTime dateExpiration;
    @Column(name = "date_creation", nullable = false) private LocalDateTime dateCreation;
    @Column(name = "utilise", nullable = false) private Boolean utilise;
    @Column(name = "nombre_tentatives", nullable = false) private Integer nombreTentatives;
}
