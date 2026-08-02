package com.xtensus.hrmanagementapi.notificationfr.dto;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class NotificationFrancaiseResponse {
    private Long id;
    private Long employeId;
    private String employeNom;
    private String typeLibelle;
    private String titre;
    private String contenu;
    private Boolean lu;
    private LocalDateTime dateCreation;
    private LocalDateTime dateLecture;
    private String priorite;
}
