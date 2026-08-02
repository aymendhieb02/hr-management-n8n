package com.xtensus.hrmanagementapi.poste.dto;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PosteResponse {

    private Long id;
    private String intitule;
    private String description;
    private String niveauPoste;
    private Boolean actif;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
}
