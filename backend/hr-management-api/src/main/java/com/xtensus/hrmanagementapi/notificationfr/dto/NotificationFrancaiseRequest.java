package com.xtensus.hrmanagementapi.notificationfr.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class NotificationFrancaiseRequest {
    @NotNull
    private Long employeId;
    @NotNull
    private Long typeId;
    @NotBlank
    @Size(max = 200)
    private String titre;
    @NotBlank
    private String contenu;
    @Size(max = 50)
    private String priorite;
}
