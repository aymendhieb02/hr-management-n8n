package com.xtensus.hrmanagementapi.employe.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EmployeResponse {

    private Long id;
    private String username;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private String adresse;
    private LocalDate dateNaissance;
    private LocalDate dateEmbauche;
    private String sexe;
    private Boolean actif;
    private String role;
    private String statut;
    private PosteResume poste;
    private TypeContratResume typeContrat;
    private ManagerResume manager;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class PosteResume {
        private Long id;
        private String intitule;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class TypeContratResume {
        private Long id;
        private String libelle;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class ManagerResume {
        private Long id;
        private String nom;
        private String prenom;
        private String email;
    }
}


