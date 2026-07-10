package com.xtensus.hrmanagementapi.department.service;

import com.xtensus.hrmanagementapi.department.dto.DepartmentRequest;
import com.xtensus.hrmanagementapi.department.dto.DepartmentResponse;
import com.xtensus.hrmanagementapi.department.exception.DepartmentNotFoundException;
import com.xtensus.hrmanagementapi.department.exception.DuplicateDepartmentException;
import com.xtensus.hrmanagementapi.department.mapper.DepartmentMapper;
import com.xtensus.hrmanagementapi.domain.entity.Department;
import com.xtensus.hrmanagementapi.repository.DepartmentRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final DepartmentMapper departmentMapper;

    public DepartmentService(
            DepartmentRepository departmentRepository,
            DepartmentMapper departmentMapper
    ) {
        this.departmentRepository = departmentRepository;
        this.departmentMapper = departmentMapper;
    }

    @Transactional
    public DepartmentResponse create(DepartmentRequest request) {
        String name = normalizeName(request.getName());
        ensureNameIsUnique(name, null);

        Department department = departmentMapper.toEntity(request);
        department.setName(name);
        department.setCreatedAt(LocalDateTime.now());

        return departmentMapper.toResponse(departmentRepository.save(department));
    }

    @Transactional
    public DepartmentResponse update(Long id, DepartmentRequest request) {
        Department department = getDepartment(id);
        String name = normalizeName(request.getName());
        ensureNameIsUnique(name, id);

        departmentMapper.updateEntity(department, request);
        department.setName(name);
        department.setUpdatedAt(LocalDateTime.now());

        return departmentMapper.toResponse(departmentRepository.save(department));
    }

    @Transactional
    public void delete(Long id) {
        Department department = getDepartment(id);
        departmentRepository.delete(department);
    }

    @Transactional(readOnly = true)
    public DepartmentResponse findById(Long id) {
        return departmentMapper.toResponse(getDepartment(id));
    }

    @Transactional(readOnly = true)
    public List<DepartmentResponse> findAll() {
        return departmentRepository.findAll()
                .stream()
                .map(departmentMapper::toResponse)
                .toList();
    }

    private Department getDepartment(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new DepartmentNotFoundException(id));
    }

    private String normalizeName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Department name must not be blank");
        }

        return name.trim();
    }

    private void ensureNameIsUnique(String name, Long currentDepartmentId) {
        boolean duplicateExists = departmentRepository.findAll()
                .stream()
                .anyMatch(department ->
                        !department.getId().equals(currentDepartmentId)
                                && department.getName().equalsIgnoreCase(name)
                );

        if (duplicateExists) {
            throw new DuplicateDepartmentException(name);
        }
    }
}
