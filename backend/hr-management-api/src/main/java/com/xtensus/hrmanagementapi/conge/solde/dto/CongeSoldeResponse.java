package com.xtensus.hrmanagementapi.conge.solde.dto;

import java.math.BigDecimal; import java.time.LocalDateTime;
import lombok.Getter; import lombok.NoArgsConstructor; import lombok.Setter;
@Getter @Setter @NoArgsConstructor
public class CongeSoldeResponse {
    private Long id; private Long employeId; private String employeNom; private String employePrenom;
    private Long congeTypeId; private String congeTypeNom; private Integer annee;
    private BigDecimal droitAcquis; private BigDecimal joursUtilises; private BigDecimal restants;
    private LocalDateTime dateCreation; private LocalDateTime dateModification;
}
