package com.xtensus.hrmanagementapi.department.mapper;

import com.xtensus.hrmanagementapi.department.dto.DepartmentRequest;
import com.xtensus.hrmanagementapi.department.dto.DepartmentResponse;
import com.xtensus.hrmanagementapi.domain.entity.Department;
import org.springframework.stereotype.Component;

@Component
public class DepartmentMapper {

    public Department toEntity(DepartmentRequest request) {
        Department department = new Department();
        updateEntity(department, request);
        return department;
    }

    public DepartmentResponse toResponse(Department department) {
        DepartmentResponse response = new DepartmentResponse();
        response.setId(department.getId());
        response.setName(department.getName());
        response.setDescription(department.getDescription());
        response.setCreatedAt(department.getCreatedAt());
        response.setUpdatedAt(department.getUpdatedAt());
        return response;
    }

    public void updateEntity(Department department, DepartmentRequest request) {
        department.setName(trimToNull(request.getName()));
        department.setDescription(trimToNull(request.getDescription()));
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
