package com.xtensus.hrmanagementapi.leave.type.mapper;

import com.xtensus.hrmanagementapi.domain.entity.LeaveType;
import com.xtensus.hrmanagementapi.leave.type.dto.LeaveTypeRequest;
import com.xtensus.hrmanagementapi.leave.type.dto.LeaveTypeResponse;
import org.springframework.stereotype.Component;

@Component
public class LeaveTypeMapper {

    public LeaveType toEntity(LeaveTypeRequest request) {
        LeaveType leaveType = new LeaveType();
        updateEntity(request, leaveType);
        return leaveType;
    }

    public LeaveTypeResponse toResponse(LeaveType entity) {
        LeaveTypeResponse response = new LeaveTypeResponse();
        response.setId(entity.getId());
        response.setName(entity.getName());
        response.setDescription(entity.getDescription());
        response.setMaxDays(entity.getMaxDays());
        response.setRequiresMedicalCertificate(entity.getRequiresMedicalCertificate());
        response.setActive(entity.getActive());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        return response;
    }

    public void updateEntity(LeaveTypeRequest request, LeaveType entity) {
        entity.setName(trimToNull(request.getName()));
        entity.setDescription(trimToNull(request.getDescription()));
        entity.setMaxDays(request.getMaxDays());
        entity.setRequiresMedicalCertificate(request.getRequiresMedicalCertificate());
        entity.setActive(request.getActive());
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
