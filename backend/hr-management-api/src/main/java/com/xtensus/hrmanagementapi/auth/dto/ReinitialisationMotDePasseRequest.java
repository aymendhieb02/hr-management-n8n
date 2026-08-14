package com.xtensus.hrmanagementapi.auth.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
public record ReinitialisationMotDePasseRequest(
        @NotBlank String identifiant,
        @Pattern(regexp="\\d{6}", message="Le code doit contenir 6 chiffres") String code,
        @Size(min=8,max=100,message="Le mot de passe doit contenir entre 8 et 100 caracteres") String nouveauMotDePasse) {}
