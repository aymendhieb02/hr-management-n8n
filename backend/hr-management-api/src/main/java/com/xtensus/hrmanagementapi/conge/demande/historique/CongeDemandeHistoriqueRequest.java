package com.xtensus.hrmanagementapi.conge.demande.historique;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CongeDemandeHistoriqueRequest {
    @NotBlank @Size(max = 50) private String action;
    @Size(max = 1000) private String commentaire;
    @Size(max = 50) private String ancienStatut;
    @Size(max = 50) private String nouveauStatut;
}
