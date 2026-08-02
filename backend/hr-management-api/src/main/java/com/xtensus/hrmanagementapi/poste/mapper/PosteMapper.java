package com.xtensus.hrmanagementapi.poste.mapper;

import com.xtensus.hrmanagementapi.domain.entity.Poste;
import com.xtensus.hrmanagementapi.poste.dto.PosteRequest;
import com.xtensus.hrmanagementapi.poste.dto.PosteResponse;
import org.springframework.stereotype.Component;

@Component
public class PosteMapper {

    public Poste toEntity(PosteRequest request) {
        Poste poste = new Poste();
        updateEntity(request, poste);
        return poste;
    }

    public PosteResponse toResponse(Poste poste) {
        PosteResponse response = new PosteResponse();
        response.setId(poste.getId());
        response.setIntitule(poste.getIntitule());
        response.setDescription(poste.getDescription());
        response.setNiveauPoste(poste.getNiveauPoste());
        response.setActif(poste.getActif());
        response.setDateCreation(poste.getDateCreation());
        response.setDateModification(poste.getDateModification());
        return response;
    }

    public void updateEntity(PosteRequest request, Poste poste) {
        poste.setIntitule(request.getIntitule() == null ? null : request.getIntitule().trim());
        poste.setDescription(request.getDescription() == null ? null : request.getDescription().trim());
        poste.setNiveauPoste(request.getNiveauPoste() == null ? null : request.getNiveauPoste().trim());
        poste.setActif(request.getActif() == null ? Boolean.TRUE : request.getActif());
    }
}
