package com.xtensus.hrmanagementapi.conge.demande.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ConsommationReelleRequest(
        @NotNull(message = "Le nombre de jours reellement consommes est obligatoire")
        @DecimalMin(value = "0.00", message = "La consommation reelle ne peut pas etre negative")
        BigDecimal nombreJoursConsommes,
        @NotBlank(message = "Le commentaire de regularisation est obligatoire")
        @Size(max = 1000, message = "Le commentaire ne peut pas depasser 1000 caracteres")
        String commentaire) {}
