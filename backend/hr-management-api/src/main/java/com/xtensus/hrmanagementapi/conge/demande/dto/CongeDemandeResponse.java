package com.xtensus.hrmanagementapi.conge.demande.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CongeDemandeResponse {
    private Long id;
    private EmployeResume employe;
    private EmployeResume decideur;
    private TypeCongeResume congeType;
    private StatutResume statut;
    private String raison;
    private Long raisonId;
    private Boolean certificatMedicalRequis;
    private String nature;
    private LocalDate dateDebut;
    private LocalTime heureDebut;
    private LocalDate dateFin;
    private LocalTime heureFin;
    private LocalDateTime dateSoumission;
    private BigDecimal nombreJours;
    private Boolean samediCompte;
    private BigDecimal nombreJoursConsomme;
    private String commentaireEmploye;
    private String commentaireDecision;
    private LocalDateTime dateDecision;

    @Getter @Setter @NoArgsConstructor
    public static class EmployeResume {
        private Long id;
        private String nom;
        private String prenom;
        private String email;
        private String photoUrl;
    }

    @Getter @Setter @NoArgsConstructor
    public static class TypeCongeResume {
        private Long id;
        private String nom;
    }

    @Getter @Setter @NoArgsConstructor
    public static class StatutResume {
        private Long id;
        private String libelle;
    }
}
