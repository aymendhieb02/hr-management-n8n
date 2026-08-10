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
public class CongeDemandeModificationRequest {
    @NotNull(message = "Le type de conge est obligatoire")
    private Long congeTypeId;
    @NotNull(message = "La nature de la demande est obligatoire")
    private String nature;
    private Long raisonId;
    @Size(max = 255, message = "Le motif ne doit pas depasser 255 caracteres")
    private String autreMotif;
    @NotNull(message = "La date de debut est obligatoire")
    private LocalDate dateDebut;
    private LocalTime heureDebut;
    @NotNull(message = "La date de fin est obligatoire")
    private LocalDate dateFin;
    private LocalTime heureFin;
    private java.math.BigDecimal nombreJours;
    @Size(max = 1000, message = "Le commentaire ne doit pas depasser 1000 caracteres")
    private String commentaireEmploye;
}
