package com.xtensus.hrmanagementapi.typecontrat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TypeContratRequest {

    @NotBlank(message = "Le libelle du type de contrat est obligatoire")
    @Size(max = 100, message = "Le libelle ne doit pas depasser 100 caracteres")
    private String libelle;

    @Size(max = 255, message = "La description ne doit pas depasser 255 caracteres")
    private String description;

    private Boolean actif;
}
