package com.xtensus.hrmanagementapi.employe.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EmployeRequest {

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 100, message = "Le nom ne doit pas depasser 100 caracteres")
    private String nom;

    @NotBlank(message = "Le prenom est obligatoire")
    @Size(max = 100, message = "Le prenom ne doit pas depasser 100 caracteres")
    private String prenom;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "L'email doit etre valide")
    @Size(max = 150, message = "L'email ne doit pas depasser 150 caracteres")
    private String email;

    @Size(max = 30, message = "Le telephone ne doit pas depasser 30 caracteres")
    private String telephone;

    @Size(max = 255, message = "L'adresse ne doit pas depasser 255 caracteres")
    private String adresse;

    private LocalDate dateNaissance;

    private LocalDate dateEmbauche;

    @Size(max = 30, message = "Le sexe ne doit pas depasser 30 caracteres")
    private String sexe;

    @NotNull(message = "Le statut actif est obligatoire")
    private Boolean actif;

    @Size(max = 30, message = "Le role ne doit pas depasser 30 caracteres")
    private String role;

    private Long posteId;

    private Long typeContratId;

    private Long managerId;
}


