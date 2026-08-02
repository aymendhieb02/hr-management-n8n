package com.xtensus.hrmanagementapi.typecontrat.dto;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TypeContratResponse {

    private Long id;
    private String libelle;
    private String description;
    private Boolean actif;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
}
