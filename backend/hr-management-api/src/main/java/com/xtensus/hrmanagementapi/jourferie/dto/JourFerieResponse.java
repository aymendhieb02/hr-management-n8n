package com.xtensus.hrmanagementapi.jourferie.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class JourFerieResponse {
    private Long id;
    private String nom;
    private LocalDate date;
    private String description;
    private Boolean actif;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
}
