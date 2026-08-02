package com.xtensus.hrmanagementapi.conge.statut.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CongeDemandeStatutResponse {

    private Long id;
    private String libelle;
    private String description;
    private Boolean actif;
}
