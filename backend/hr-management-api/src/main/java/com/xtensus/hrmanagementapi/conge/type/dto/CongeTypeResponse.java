package com.xtensus.hrmanagementapi.conge.type.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CongeTypeResponse {

    private Long id;
    private String nom;
    private String description;
    private BigDecimal joursMaximum;
    private Boolean certificatObligatoire;
    private Boolean remunere;
    private Boolean actif;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
}
