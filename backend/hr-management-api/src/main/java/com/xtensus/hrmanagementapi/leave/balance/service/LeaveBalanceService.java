package com.xtensus.hrmanagementapi.leave.balance.service;

import com.xtensus.hrmanagementapi.domain.entity.LeaveBalance;
import com.xtensus.hrmanagementapi.domain.entity.LeaveType;
import com.xtensus.hrmanagementapi.domain.entity.User;
import com.xtensus.hrmanagementapi.leave.balance.dto.LeaveBalanceRequest;
import com.xtensus.hrmanagementapi.leave.balance.dto.LeaveBalanceResponse;
import com.xtensus.hrmanagementapi.leave.balance.exception.DuplicateLeaveBalanceException;
import com.xtensus.hrmanagementapi.leave.balance.exception.InvalidLeaveBalanceException;
import com.xtensus.hrmanagementapi.leave.balance.exception.LeaveBalanceNotFoundException;
import com.xtensus.hrmanagementapi.leave.balance.mapper.LeaveBalanceMapper;
import com.xtensus.hrmanagementapi.leave.type.exception.LeaveTypeNotFoundException;
import com.xtensus.hrmanagementapi.repository.LeaveBalanceRepository;
import com.xtensus.hrmanagementapi.repository.LeaveTypeRepository;
import com.xtensus.hrmanagementapi.repository.UserRepository;
import com.xtensus.hrmanagementapi.user.exception.UserNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LeaveBalanceService {

    private final LeaveBalanceRepository leaveBalanceRepository;
    private final UserRepository userRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final LeaveBalanceMapper leaveBalanceMapper;

    public LeaveBalanceService(
            LeaveBalanceRepository leaveBalanceRepository,
            UserRepository userRepository,
            LeaveTypeRepository leaveTypeRepository,
            LeaveBalanceMapper leaveBalanceMapper
    ) {
        this.leaveBalanceRepository = leaveBalanceRepository;
        this.userRepository = userRepository;
        this.leaveTypeRepository = leaveTypeRepository;
        this.leaveBalanceMapper = leaveBalanceMapper;
    }

    @Transactional
    public LeaveBalanceResponse create(LeaveBalanceRequest request) {
        User user = getUser(request.getUserId());
        LeaveType leaveType = getLeaveType(request.getLeaveTypeId());
        validateTotalDays(request.getTotalDays());
        ensureUnique(request.getUserId(), request.getLeaveTypeId(), request.getYear(), null);

        LeaveBalance balance = leaveBalanceMapper.toEntity(request);
        balance.setUser(user);
        balance.setLeaveType(leaveType);
        balance.setUsedDays(BigDecimal.ZERO);
        balance.setRemainingDays(request.getTotalDays());
        balance.setCreatedAt(LocalDateTime.now());

        return leaveBalanceMapper.toResponse(leaveBalanceRepository.save(balance));
    }

    @Transactional
    public LeaveBalanceResponse update(Long id, LeaveBalanceRequest request) {
        LeaveBalance balance = getBalance(id);
        User user = getUser(request.getUserId());
        LeaveType leaveType = getLeaveType(request.getLeaveTypeId());
        validateTotalDays(request.getTotalDays());
        ensureUnique(request.getUserId(), request.getLeaveTypeId(), request.getYear(), id);

        if (request.getTotalDays().compareTo(balance.getUsedDays()) < 0) {
            throw new InvalidLeaveBalanceException("Total days cannot be less than used days");
        }

        leaveBalanceMapper.updateEntity(request, balance);
        balance.setUser(user);
        balance.setLeaveType(leaveType);
        balance.setRemainingDays(request.getTotalDays().subtract(balance.getUsedDays()));
        validateNonNegative(balance.getUsedDays(), "Used days cannot be negative");
        validateNonNegative(balance.getRemainingDays(), "Remaining days cannot be negative");
        balance.setUpdatedAt(LocalDateTime.now());

        return leaveBalanceMapper.toResponse(leaveBalanceRepository.save(balance));
    }

    @Transactional(readOnly = true)
    public LeaveBalanceResponse findById(Long id) {
        return leaveBalanceMapper.toResponse(getBalance(id));
    }

    @Transactional(readOnly = true)
    public List<LeaveBalanceResponse> findAll() {
        return leaveBalanceRepository.findAll()
                .stream()
                .map(leaveBalanceMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LeaveBalanceResponse> findByUser(Long userId) {
        return leaveBalanceRepository.findByUserId(userId)
                .stream()
                .map(leaveBalanceMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LeaveBalanceResponse> findByUserAndYear(Long userId, Integer year) {
        return leaveBalanceRepository.findByUserIdAndYear(userId, year)
                .stream()
                .map(leaveBalanceMapper::toResponse)
                .toList();
    }

    @Transactional
    public void delete(Long id) {
        LeaveBalance balance = getBalance(id);
        leaveBalanceRepository.delete(balance);
    }

    private LeaveBalance getBalance(Long id) {
        return leaveBalanceRepository.findById(id)
                .orElseThrow(() -> new LeaveBalanceNotFoundException(id));
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    private LeaveType getLeaveType(Long leaveTypeId) {
        return leaveTypeRepository.findById(leaveTypeId)
                .orElseThrow(() -> new LeaveTypeNotFoundException(leaveTypeId));
    }

    private void ensureUnique(Long userId, Long leaveTypeId, Integer year, Long currentId) {
        leaveBalanceRepository.findByUserIdAndLeaveTypeIdAndYear(userId, leaveTypeId, year)
                .filter(existing -> currentId == null || !existing.getId().equals(currentId))
                .ifPresent(existing -> {
                    throw new DuplicateLeaveBalanceException(userId, leaveTypeId, year);
                });
    }

    private void validateTotalDays(BigDecimal totalDays) {
        validateNonNegative(totalDays, "Total days cannot be negative");
    }

    private void validateNonNegative(BigDecimal value, String message) {
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidLeaveBalanceException(message);
        }
    }
}
