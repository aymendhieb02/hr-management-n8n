package com.xtensus.hrmanagementapi.position.mapper;

import com.xtensus.hrmanagementapi.domain.entity.Position;
import com.xtensus.hrmanagementapi.position.dto.PositionRequest;
import com.xtensus.hrmanagementapi.position.dto.PositionResponse;
import org.springframework.stereotype.Component;

@Component
public class PositionMapper {

    public Position toEntity(PositionRequest request) {
        Position position = new Position();
        updateEntity(request, position);
        return position;
    }

    public PositionResponse toResponse(Position entity) {
        PositionResponse response = new PositionResponse();
        response.setId(entity.getId());
        response.setTitle(entity.getTitle());
        response.setDescription(entity.getDescription());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        return response;
    }

    public void updateEntity(PositionRequest request, Position entity) {
        entity.setTitle(trimToNull(request.getTitle()));
        entity.setDescription(trimToNull(request.getDescription()));
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
