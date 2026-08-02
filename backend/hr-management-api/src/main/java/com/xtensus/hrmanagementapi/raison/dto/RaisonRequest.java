package com.xtensus.hrmanagementapi.raison.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RaisonRequest {

    @NotBlank(message = "Le commentaire est obligatoire")
    @Size(max = 255, message = "Le commentaire ne doit pas depasser 255 caracteres")
    private String commentaire;
}
