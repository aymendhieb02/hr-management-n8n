package com.xtensus.hrmanagementapi.certificat.medical.dto;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CertificatMedicalResponse {
    private Long id;
    private Long congeDemandeId;
    private String nomFichier;
    private String typeMime;
    private Long tailleFichier;
    private LocalDateTime dateSoumission;
}
