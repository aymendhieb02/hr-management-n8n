package com.xtensus.hrmanagementapi.conge.demande.historique;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CongeDemandeHistoriquePageResponse {
    private List<CongeDemandeHistoriqueResponse> contenu;
    private long totalElements;
    private int totalPages;
    private int page;
    private int taille;
}
