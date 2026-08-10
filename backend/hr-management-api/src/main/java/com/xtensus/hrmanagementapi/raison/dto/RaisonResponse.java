package com.xtensus.hrmanagementapi.raison.dto;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RaisonResponse {

    private Long id;
    private String commentaire;
    private Boolean disponible;
    private LocalDateTime dateCreation;
}
