package com.xtensus.hrmanagementapi.conge.demande.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CongeDemandeCreationRequest {
    @NotNull(message = "L'employe est obligatoire")
    private Long employeId;
    @NotNull(message = "Le type de conge est obligatoire")
    private Long congeTypeId;
    @NotNull(message = "La date de debut est obligatoire")
    private LocalDate dateDebut;
    private LocalTime heureDebut;
    @NotNull(message = "La date de fin est obligatoire")
    private LocalDate dateFin;
    private LocalTime heureFin;
    @Size(max = 1000, message = "Le commentaire ne doit pas depasser 1000 caracteres")
    private String commentaireEmploye;
}
