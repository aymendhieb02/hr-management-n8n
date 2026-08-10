package com.xtensus.hrmanagementapi.jourferie.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JourFerieRequest {

    @NotBlank(message = "Le nom du jour férié est obligatoire")
    @Size(max = 150, message = "Le nom ne doit pas dépasser 150 caractères")
    private String nom;

    @NotNull(message = "La date est obligatoire")
    private LocalDate date;

    @Size(max = 255, message = "La description ne doit pas dépasser 255 caractères")
    private String description;

    private Boolean actif = true;
}
