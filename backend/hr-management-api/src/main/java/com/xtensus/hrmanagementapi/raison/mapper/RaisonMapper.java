package com.xtensus.hrmanagementapi.raison.mapper;

import com.xtensus.hrmanagementapi.domain.entity.Raison;
import com.xtensus.hrmanagementapi.raison.dto.RaisonRequest;
import com.xtensus.hrmanagementapi.raison.dto.RaisonResponse;
import org.springframework.stereotype.Component;

@Component
public class RaisonMapper {

    public Raison toEntity(RaisonRequest request) {
        Raison raison = new Raison();
        updateEntity(request, raison);
        return raison;
    }

    public RaisonResponse toResponse(Raison raison) {
        RaisonResponse response = new RaisonResponse();
        response.setId(raison.getId());
        response.setCommentaire(raison.getCommentaire());
        response.setDateCreation(raison.getDateCreation());
        return response;
    }

    public void updateEntity(RaisonRequest request, Raison raison) {
        raison.setCommentaire(request.getCommentaire() == null ? null : request.getCommentaire().trim());
    }
}
