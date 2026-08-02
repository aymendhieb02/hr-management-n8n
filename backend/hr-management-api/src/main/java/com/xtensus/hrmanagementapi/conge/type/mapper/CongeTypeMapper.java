package com.xtensus.hrmanagementapi.conge.type.mapper;

import com.xtensus.hrmanagementapi.conge.type.dto.CongeTypeRequest;
import com.xtensus.hrmanagementapi.conge.type.dto.CongeTypeResponse;
import com.xtensus.hrmanagementapi.domain.entity.CongeType;
import java.math.RoundingMode;
import org.springframework.stereotype.Component;

@Component
public class CongeTypeMapper {

    public CongeType toEntity(CongeTypeRequest request) {
        CongeType congeType = new CongeType();
        updateEntity(request, congeType);
        return congeType;
    }

    public CongeTypeResponse toResponse(CongeType congeType) {
        CongeTypeResponse response = new CongeTypeResponse();
        response.setId(congeType.getId());
        response.setNom(congeType.getNom());
        response.setDescription(congeType.getDescription());
        response.setJoursMaximum(congeType.getJoursMaximum());
        response.setCertificatObligatoire(congeType.getCertificatObligatoire());
        response.setRemunere(congeType.getRemunere());
        response.setActif(congeType.getActif());
        response.setDateCreation(congeType.getDateCreation());
        response.setDateModification(congeType.getDateModification());
        return response;
    }

    public void updateEntity(CongeTypeRequest request, CongeType congeType) {
        congeType.setNom(request.getNom() == null ? null : request.getNom().trim());
        congeType.setDescription(request.getDescription() == null ? null : request.getDescription().trim());
        congeType.setJoursMaximum(request.getJoursMaximum() == null ? null : request.getJoursMaximum().setScale(2, RoundingMode.HALF_UP));
        congeType.setCertificatObligatoire(request.getCertificatObligatoire());
        congeType.setRemunere(request.getRemunere());
        congeType.setActif(request.getActif());
    }
}
