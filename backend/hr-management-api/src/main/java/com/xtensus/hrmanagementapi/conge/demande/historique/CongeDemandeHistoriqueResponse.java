package com.xtensus.hrmanagementapi.conge.demande.historique;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.Builder;
import lombok.Getter;

@Getter @Builder
public class CongeDemandeHistoriqueResponse {
    private Long id;
    private Long demandeId;
    private LocalDateTime dateAction;
    private String action;
    private String commentaire;
    private String ancienStatut;
    private String nouveauStatut;
    private Long employeId;
    private String employeNomComplet;
    private String typeDemande;
    private String typeConge;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private LocalTime heureDebut;
    private LocalTime heureFin;
    private String statutActuel;
}
