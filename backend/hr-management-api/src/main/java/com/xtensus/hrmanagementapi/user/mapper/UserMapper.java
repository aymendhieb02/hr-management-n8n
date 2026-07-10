package com.xtensus.hrmanagementapi.user.mapper;

import com.xtensus.hrmanagementapi.domain.entity.Department;
import com.xtensus.hrmanagementapi.domain.entity.Position;
import com.xtensus.hrmanagementapi.domain.entity.User;
import com.xtensus.hrmanagementapi.user.dto.UserCreateRequest;
import com.xtensus.hrmanagementapi.user.dto.UserResponse;
import com.xtensus.hrmanagementapi.user.dto.UserUpdateRequest;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toEntity(UserCreateRequest request) {
        User user = new User();
        user.setUsername(trimToNull(request.getUsername()));
        user.setEmail(normalizeEmail(request.getEmail()));
        user.setFirstName(trimToNull(request.getFirstName()));
        user.setLastName(trimToNull(request.getLastName()));
        user.setPhone(trimToNull(request.getPhone()));
        user.setHireDate(request.getHireDate());
        user.setRole(request.getRole());
        user.setStatus(request.getStatus());
        user.setEnabled(request.getEnabled());
        return user;
    }

    public UserResponse toResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setPhone(user.getPhone());
        response.setHireDate(user.getHireDate());
        response.setRole(user.getRole());
        response.setStatus(user.getStatus());
        response.setEnabled(user.getEnabled());
        response.setManager(toManagerSummary(user.getManager()));
        response.setDepartment(toDepartmentSummary(user.getDepartment()));
        response.setPosition(toPositionSummary(user.getPosition()));
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        return response;
    }

    public void updateEntity(UserUpdateRequest request, User user) {
        user.setUsername(trimToNull(request.getUsername()));
        user.setEmail(normalizeEmail(request.getEmail()));
        user.setFirstName(trimToNull(request.getFirstName()));
        user.setLastName(trimToNull(request.getLastName()));
        user.setPhone(trimToNull(request.getPhone()));
        user.setHireDate(request.getHireDate());
        user.setRole(request.getRole());
        user.setStatus(request.getStatus());
        user.setEnabled(request.getEnabled());
    }

    private UserResponse.ManagerSummary toManagerSummary(User manager) {
        if (manager == null) {
            return null;
        }

        UserResponse.ManagerSummary summary = new UserResponse.ManagerSummary();
        summary.setId(manager.getId());
        summary.setFirstName(manager.getFirstName());
        summary.setLastName(manager.getLastName());
        summary.setEmail(manager.getEmail());
        return summary;
    }

    private UserResponse.DepartmentSummary toDepartmentSummary(Department department) {
        if (department == null) {
            return null;
        }

        UserResponse.DepartmentSummary summary = new UserResponse.DepartmentSummary();
        summary.setId(department.getId());
        summary.setName(department.getName());
        return summary;
    }

    private UserResponse.PositionSummary toPositionSummary(Position position) {
        if (position == null) {
            return null;
        }

        UserResponse.PositionSummary summary = new UserResponse.PositionSummary();
        summary.setId(position.getId());
        summary.setTitle(position.getTitle());
        return summary;
    }

    private String normalizeEmail(String value) {
        String trimmed = trimToNull(value);
        return trimmed == null ? null : trimmed.toLowerCase();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
