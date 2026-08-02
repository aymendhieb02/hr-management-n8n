package com.xtensus.hrmanagementapi.conge.solde.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import lombok.Getter; import lombok.NoArgsConstructor; import lombok.Setter;
@Getter @Setter @NoArgsConstructor
public class CongeSoldeRequest {
    @NotNull private Long employeId;
    @NotNull private Long congeTypeId;
    @NotNull @Min(2000) @Max(2100) private Integer annee;
    @NotNull @DecimalMin("0.00") private BigDecimal droitAcquis;
    @NotNull @DecimalMin("0.00") private BigDecimal joursUtilises;
    @NotNull @DecimalMin("0.00") private BigDecimal restants;
}
