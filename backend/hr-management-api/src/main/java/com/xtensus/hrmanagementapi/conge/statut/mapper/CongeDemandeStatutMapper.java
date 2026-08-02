package com.xtensus.hrmanagementapi.conge.statut.mapper;

import com.xtensus.hrmanagementapi.conge.statut.dto.CongeDemandeStatutRequest;
import com.xtensus.hrmanagementapi.conge.statut.dto.CongeDemandeStatutResponse;
import com.xtensus.hrmanagementapi.domain.entity.CongeDemandeStatut;
import org.springframework.stereotype.Component;

@Component
public class CongeDemandeStatutMapper {

    public CongeDemandeStatut toEntity(CongeDemandeStatutRequest request) {
        CongeDemandeStatut statut = new CongeDemandeStatut();
        updateEntity(request, statut);
        return statut;
    }

    public CongeDemandeStatutResponse toResponse(CongeDemandeStatut statut) {
        CongeDemandeStatutResponse response = new CongeDemandeStatutResponse();
        response.setId(statut.getId());
        response.setLibelle(statut.getLibelle());
        response.setDescription(statut.getDescription());
        response.setActif(statut.getActif());
        return response;
    }

    public void updateEntity(CongeDemandeStatutRequest request, CongeDemandeStatut statut) {
        statut.setLibelle(trim(request.getLibelle()));
        statut.setDescription(trim(request.getDescription()));
        statut.setActif(request.getActif());
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}
