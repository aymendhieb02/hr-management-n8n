package com.xtensus.hrmanagementapi.conge.demande.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CongeDecisionRequest {
    @NotNull(message = "Le decideur est obligatoire")
    private Long decideurId;
    @Size(max = 1000, message = "Le commentaire ne doit pas depasser 1000 caracteres")
    private String commentaire;
}
