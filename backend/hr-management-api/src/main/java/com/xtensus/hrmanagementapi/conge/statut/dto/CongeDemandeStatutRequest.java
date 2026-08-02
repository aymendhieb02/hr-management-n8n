package com.xtensus.hrmanagementapi.conge.statut.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CongeDemandeStatutRequest {

    @NotBlank(message = "Le libelle est obligatoire")
    @Size(max = 100, message = "Le libelle ne doit pas depasser 100 caracteres")
    private String libelle;

    @Size(max = 255, message = "La description ne doit pas depasser 255 caracteres")
    private String description;

    @NotNull(message = "Le statut actif est obligatoire")
    private Boolean actif;
}
