package com.xtensus.hrmanagementapi.leave.type.service;

import com.xtensus.hrmanagementapi.domain.entity.LeaveType;
import com.xtensus.hrmanagementapi.leave.type.dto.LeaveTypeRequest;
import com.xtensus.hrmanagementapi.leave.type.dto.LeaveTypeResponse;
import com.xtensus.hrmanagementapi.leave.type.exception.DuplicateLeaveTypeException;
import com.xtensus.hrmanagementapi.leave.type.exception.LeaveTypeNotFoundException;
import com.xtensus.hrmanagementapi.leave.type.mapper.LeaveTypeMapper;
import com.xtensus.hrmanagementapi.repository.LeaveTypeRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LeaveTypeService {

    private final LeaveTypeRepository leaveTypeRepository;
    private final LeaveTypeMapper leaveTypeMapper;

    public LeaveTypeService(
            LeaveTypeRepository leaveTypeRepository,
            LeaveTypeMapper leaveTypeMapper
    ) {
        this.leaveTypeRepository = leaveTypeRepository;
        this.leaveTypeMapper = leaveTypeMapper;
    }

    @Transactional
    public LeaveTypeResponse create(LeaveTypeRequest request) {
        String name = normalizeName(request.getName());
        validateMaxDays(request.getMaxDays());

        if (leaveTypeRepository.existsByNameIgnoreCase(name)) {
            throw new DuplicateLeaveTypeException(name);
        }

        LeaveType leaveType = leaveTypeMapper.toEntity(request);
        leaveType.setName(name);
        leaveType.setCreatedAt(LocalDateTime.now());

        return leaveTypeMapper.toResponse(leaveTypeRepository.save(leaveType));
    }

    @Transactional
    public LeaveTypeResponse update(Long id, LeaveTypeRequest request) {
        LeaveType leaveType = getLeaveType(id);
        String name = normalizeName(request.getName());
        validateMaxDays(request.getMaxDays());

        if (leaveTypeRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new DuplicateLeaveTypeException(name);
        }

        leaveTypeMapper.updateEntity(request, leaveType);
        leaveType.setName(name);
        leaveType.setUpdatedAt(LocalDateTime.now());

        return leaveTypeMapper.toResponse(leaveTypeRepository.save(leaveType));
    }

    @Transactional
    public void delete(Long id) {
        LeaveType leaveType = getLeaveType(id);
        leaveTypeRepository.delete(leaveType);
    }

    @Transactional(readOnly = true)
    public LeaveTypeResponse findById(Long id) {
        return leaveTypeMapper.toResponse(getLeaveType(id));
    }

    @Transactional(readOnly = true)
    public List<LeaveTypeResponse> findAll() {
        return leaveTypeRepository.findAll()
                .stream()
                .map(leaveTypeMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LeaveTypeResponse> findActive() {
        return leaveTypeRepository.findByActiveTrue()
                .stream()
                .map(leaveTypeMapper::toResponse)
                .toList();
    }

    private LeaveType getLeaveType(Long id) {
        return leaveTypeRepository.findById(id)
                .orElseThrow(() -> new LeaveTypeNotFoundException(id));
    }

    private String normalizeName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Leave type name must not be blank");
        }

        return name.trim();
    }

    private void validateMaxDays(Integer maxDays) {
        if (maxDays != null && maxDays <= 0) {
            throw new IllegalArgumentException("Leave type maxDays must be greater than zero");
        }
    }
}
