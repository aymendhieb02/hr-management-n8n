package com.xtensus.hrmanagementapi.auth.dto;
import jakarta.validation.constraints.NotBlank;
public record MotDePasseOublieRequest(@NotBlank String identifiant) {}
