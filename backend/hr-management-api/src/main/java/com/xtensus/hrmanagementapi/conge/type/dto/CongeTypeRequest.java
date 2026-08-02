package com.xtensus.hrmanagementapi.conge.type.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CongeTypeRequest {

    @NotBlank(message = "Le nom du type de conge est obligatoire")
    @Size(max = 100, message = "Le nom ne doit pas depasser 100 caracteres")
    private String nom;

    @Size(max = 255, message = "La description ne doit pas depasser 255 caracteres")
    private String description;

    @DecimalMin(value = "0.01", message = "Le nombre de jours maximum doit etre superieur a zero")
    private BigDecimal joursMaximum;

    @NotNull(message = "L'indicateur certificat obligatoire est obligatoire")
    private Boolean certificatObligatoire;

    @NotNull(message = "L'indicateur remunere est obligatoire")
    private Boolean remunere;

    @NotNull(message = "L'indicateur actif est obligatoire")
    private Boolean actif;
}
