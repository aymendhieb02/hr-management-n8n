package com.xtensus.hrmanagementapi.poste.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PosteRequest {

    @NotBlank(message = "L'intitule du poste est obligatoire")
    @Size(max = 100, message = "L'intitule du poste ne doit pas depasser 100 caracteres")
    private String intitule;

    @Size(max = 255, message = "La description ne doit pas depasser 255 caracteres")
    private String description;

    @Size(max = 100, message = "Le niveau du poste ne doit pas depasser 100 caracteres")
    private String niveauPoste;

    private Boolean actif;
}
