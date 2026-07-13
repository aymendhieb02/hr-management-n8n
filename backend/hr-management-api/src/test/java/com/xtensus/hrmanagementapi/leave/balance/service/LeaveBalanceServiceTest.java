package com.xtensus.hrmanagementapi.leave.balance.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.xtensus.hrmanagementapi.domain.entity.LeaveBalance;
import com.xtensus.hrmanagementapi.domain.entity.LeaveType;
import com.xtensus.hrmanagementapi.domain.entity.User;
import com.xtensus.hrmanagementapi.domain.enums.RoleType;
import com.xtensus.hrmanagementapi.domain.enums.UserStatus;
import com.xtensus.hrmanagementapi.leave.balance.dto.LeaveBalanceRequest;
import com.xtensus.hrmanagementapi.leave.balance.dto.LeaveBalanceResponse;
import com.xtensus.hrmanagementapi.leave.balance.exception.DuplicateLeaveBalanceException;
import com.xtensus.hrmanagementapi.leave.balance.exception.InvalidLeaveBalanceException;
import com.xtensus.hrmanagementapi.leave.balance.mapper.LeaveBalanceMapper;
import com.xtensus.hrmanagementapi.leave.type.exception.LeaveTypeNotFoundException;
import com.xtensus.hrmanagementapi.repository.LeaveBalanceRepository;
import com.xtensus.hrmanagementapi.repository.LeaveTypeRepository;
import com.xtensus.hrmanagementapi.repository.UserRepository;
import com.xtensus.hrmanagementapi.user.exception.UserNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LeaveBalanceServiceTest {

    @Mock
    private LeaveBalanceRepository leaveBalanceRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private LeaveTypeRepository leaveTypeRepository;

    private LeaveBalanceService leaveBalanceService;

    @BeforeEach
    void setUp() {
        leaveBalanceService = new LeaveBalanceService(
                leaveBalanceRepository,
                userRepository,
                leaveTypeRepository,
                new LeaveBalanceMapper()
        );
    }

    @Test
    void createLeaveBalanceSuccessfully() {
        mockUserAndLeaveType();
        when(leaveBalanceRepository.findByUserIdAndLeaveTypeIdAndYear(1L, 3L, 2026))
                .thenReturn(Optional.empty());
        when(leaveBalanceRepository.save(any(LeaveBalance.class))).thenAnswer(invocation -> {
            LeaveBalance saved = invocation.getArgument(0);
            saved.setId(10L);
            return saved;
        });

        LeaveBalanceResponse response = leaveBalanceService.create(request(BigDecimal.valueOf(20)));

        assertEquals(10L, response.getId());
        assertEquals(1L, response.getUser().getId());
        assertEquals("Annual Leave", response.getLeaveType().getName());
    }

    @Test
    void usedDaysDefaultsToZero() {
        LeaveBalanceResponse response = createResponse(BigDecimal.valueOf(20));

        assertEquals(BigDecimal.ZERO, response.getUsedDays());
    }

    @Test
    void remainingDaysEqualsTotalDays() {
        LeaveBalanceResponse response = createResponse(BigDecimal.valueOf(20));

        assertEquals(BigDecimal.valueOf(20), response.getRemainingDays());
    }

    @Test
    void duplicateUserTypeYearReturnsConflict() {
        mockUserAndLeaveType();
        when(leaveBalanceRepository.findByUserIdAndLeaveTypeIdAndYear(1L, 3L, 2026))
                .thenReturn(Optional.of(balance(10L, BigDecimal.valueOf(20), BigDecimal.ZERO, BigDecimal.valueOf(20))));

        assertThrows(DuplicateLeaveBalanceException.class,
                () -> leaveBalanceService.create(request(BigDecimal.valueOf(20))));
    }

    @Test
    void missingUserReturnsNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> leaveBalanceService.create(request(BigDecimal.valueOf(20))));
    }

    @Test
    void missingLeaveTypeReturnsNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L)));
        when(leaveTypeRepository.findById(3L)).thenReturn(Optional.empty());

        assertThrows(LeaveTypeNotFoundException.class,
                () -> leaveBalanceService.create(request(BigDecimal.valueOf(20))));
    }

    @Test
    void negativeTotalDaysReturnsBadRequest() {
        mockUserAndLeaveType();

        assertThrows(InvalidLeaveBalanceException.class,
                () -> leaveBalanceService.create(request(BigDecimal.valueOf(-1))));
    }

    @Test
    void updateTotalDaysSuccessfully() {
        LeaveBalance existing = balance(1L, BigDecimal.valueOf(20), BigDecimal.valueOf(5), BigDecimal.valueOf(15));
        when(leaveBalanceRepository.findById(1L)).thenReturn(Optional.of(existing));
        mockUserAndLeaveType();
        when(leaveBalanceRepository.findByUserIdAndLeaveTypeIdAndYear(1L, 3L, 2026))
                .thenReturn(Optional.of(existing));
        when(leaveBalanceRepository.save(any(LeaveBalance.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LeaveBalanceResponse response = leaveBalanceService.update(1L, request(BigDecimal.valueOf(25)));

        assertEquals(BigDecimal.valueOf(25), response.getTotalDays());
        assertEquals(BigDecimal.valueOf(5), response.getUsedDays());
        assertEquals(BigDecimal.valueOf(20), response.getRemainingDays());
    }

    @Test
    void updateRejectsTotalDaysBelowUsedDays() {
        LeaveBalance existing = balance(1L, BigDecimal.valueOf(20), BigDecimal.valueOf(5), BigDecimal.valueOf(15));
        when(leaveBalanceRepository.findById(1L)).thenReturn(Optional.of(existing));
        mockUserAndLeaveType();
        when(leaveBalanceRepository.findByUserIdAndLeaveTypeIdAndYear(1L, 3L, 2026))
                .thenReturn(Optional.of(existing));

        assertThrows(InvalidLeaveBalanceException.class,
                () -> leaveBalanceService.update(1L, request(BigDecimal.valueOf(4))));
    }

    @Test
    void findBalancesByUser() {
        when(leaveBalanceRepository.findByUserId(1L))
                .thenReturn(List.of(balance(1L, BigDecimal.valueOf(20), BigDecimal.ZERO, BigDecimal.valueOf(20))));

        List<LeaveBalanceResponse> responses = leaveBalanceService.findByUser(1L);

        assertEquals(1, responses.size());
        assertEquals(1L, responses.get(0).getUser().getId());
    }

    @Test
    void findBalancesByUserAndYear() {
        when(leaveBalanceRepository.findByUserIdAndYear(1L, 2026))
                .thenReturn(List.of(balance(1L, BigDecimal.valueOf(20), BigDecimal.ZERO, BigDecimal.valueOf(20))));

        List<LeaveBalanceResponse> responses = leaveBalanceService.findByUserAndYear(1L, 2026);

        assertEquals(1, responses.size());
        assertEquals(2026, responses.get(0).getYear());
    }

    @Test
    void deleteBalanceSuccessfully() {
        LeaveBalance existing = balance(1L, BigDecimal.valueOf(20), BigDecimal.ZERO, BigDecimal.valueOf(20));
        when(leaveBalanceRepository.findById(1L)).thenReturn(Optional.of(existing));

        leaveBalanceService.delete(1L);

        verify(leaveBalanceRepository).delete(existing);
    }

    private LeaveBalanceResponse createResponse(BigDecimal totalDays) {
        mockUserAndLeaveType();
        when(leaveBalanceRepository.findByUserIdAndLeaveTypeIdAndYear(1L, 3L, 2026))
                .thenReturn(Optional.empty());
        when(leaveBalanceRepository.save(any(LeaveBalance.class))).thenAnswer(invocation -> invocation.getArgument(0));

        return leaveBalanceService.create(request(totalDays));
    }

    private LeaveBalanceRequest request(BigDecimal totalDays) {
        LeaveBalanceRequest request = new LeaveBalanceRequest();
        request.setUserId(1L);
        request.setLeaveTypeId(3L);
        request.setYear(2026);
        request.setTotalDays(totalDays);
        return request;
    }

    private void mockUserAndLeaveType() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L)));
        when(leaveTypeRepository.findById(3L)).thenReturn(Optional.of(leaveType(3L)));
    }

    private LeaveBalance balance(Long id, BigDecimal totalDays, BigDecimal usedDays, BigDecimal remainingDays) {
        LeaveBalance balance = new LeaveBalance();
        balance.setId(id);
        balance.setUser(user(1L));
        balance.setLeaveType(leaveType(3L));
        balance.setYear(2026);
        balance.setTotalDays(totalDays);
        balance.setUsedDays(usedDays);
        balance.setRemainingDays(remainingDays);
        balance.setCreatedAt(LocalDateTime.now());
        return balance;
    }

    private User user(Long id) {
        User user = new User();
        user.setId(id);
        user.setUsername("user" + id);
        user.setEmail("user" + id + "@example.com");
        user.setPasswordHash("hash");
        user.setFirstName("User");
        user.setLastName(String.valueOf(id));
        user.setRole(RoleType.EMPLOYEE);
        user.setStatus(UserStatus.ACTIVE);
        user.setEnabled(true);
        return user;
    }

    private LeaveType leaveType(Long id) {
        LeaveType leaveType = new LeaveType();
        leaveType.setId(id);
        leaveType.setName("Annual Leave");
        leaveType.setActive(true);
        return leaveType;
    }
}
