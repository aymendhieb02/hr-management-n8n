package com.xtensus.hrmanagementapi.typecontrat.mapper;

import com.xtensus.hrmanagementapi.domain.entity.TypeContrat;
import com.xtensus.hrmanagementapi.typecontrat.dto.TypeContratRequest;
import com.xtensus.hrmanagementapi.typecontrat.dto.TypeContratResponse;
import org.springframework.stereotype.Component;

@Component
public class TypeContratMapper {

    public TypeContrat toEntity(TypeContratRequest request) {
        TypeContrat typeContrat = new TypeContrat();
        updateEntity(request, typeContrat);
        return typeContrat;
    }

    public TypeContratResponse toResponse(TypeContrat typeContrat) {
        TypeContratResponse response = new TypeContratResponse();
        response.setId(typeContrat.getId());
        response.setLibelle(typeContrat.getLibelle());
        response.setDescription(typeContrat.getDescription());
        response.setActif(typeContrat.getActif());
        response.setDateCreation(typeContrat.getDateCreation());
        response.setDateModification(typeContrat.getDateModification());
        return response;
    }

    public void updateEntity(TypeContratRequest request, TypeContrat typeContrat) {
        typeContrat.setLibelle(request.getLibelle() == null ? null : request.getLibelle().trim());
        typeContrat.setDescription(request.getDescription() == null ? null : request.getDescription().trim());
        typeContrat.setActif(request.getActif() == null ? Boolean.TRUE : request.getActif());
    }
}
