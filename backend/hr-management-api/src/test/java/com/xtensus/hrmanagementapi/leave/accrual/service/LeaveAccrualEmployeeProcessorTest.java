package com.xtensus.hrmanagementapi.leave.accrual.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.xtensus.hrmanagementapi.domain.entity.LeaveBalance;
import com.xtensus.hrmanagementapi.domain.entity.LeaveBalanceAccrual;
import com.xtensus.hrmanagementapi.domain.entity.LeaveType;
import com.xtensus.hrmanagementapi.domain.entity.User;
import com.xtensus.hrmanagementapi.domain.enums.RoleType;
import com.xtensus.hrmanagementapi.domain.enums.UserStatus;
import com.xtensus.hrmanagementapi.repository.LeaveBalanceAccrualRepository;
import com.xtensus.hrmanagementapi.repository.LeaveBalanceRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LeaveAccrualEmployeeProcessorTest {

    @Mock
    private LeaveBalanceRepository leaveBalanceRepository;

    @Mock
    private LeaveBalanceAccrualRepository leaveBalanceAccrualRepository;

    private LeaveAccrualEmployeeProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new LeaveAccrualEmployeeProcessor(leaveBalanceRepository, leaveBalanceAccrualRepository);
    }

    @Test
    void missingAnnualBalanceIsCreatedAndCredited() {
        when(leaveBalanceAccrualRepository.existsByUserIdAndLeaveTypeIdAndAccrualYearAndAccrualMonth(1L, 3L, 2026, 7))
                .thenReturn(false);
        when(leaveBalanceRepository.findByUserIdAndLeaveTypeIdAndYear(1L, 3L, 2026))
                .thenReturn(Optional.empty());

        LeaveAccrualEmployeeResult result = processor.process(user(1L, RoleType.EMPLOYEE), annualLeave(), 2026, 7, new BigDecimal("2.16"));

        ArgumentCaptor<LeaveBalance> balanceCaptor = ArgumentCaptor.forClass(LeaveBalance.class);
        verify(leaveBalanceRepository).save(balanceCaptor.capture());
        LeaveBalance saved = balanceCaptor.getValue();
        assertTrue(result.credited());
        assertEquals(new BigDecimal("2.16"), result.creditedDays());
        assertEquals(new BigDecimal("2.16"), saved.getTotalDays());
        assertEquals(new BigDecimal("0.00"), saved.getUsedDays());
        assertEquals(new BigDecimal("2.16"), saved.getRemainingDays());
    }

    @Test
    void existingBalanceIsIncrementedAndUsedDaysRemainUnchanged() {
        LeaveBalance balance = balance(new BigDecimal("10.00"), new BigDecimal("3.00"), new BigDecimal("7.00"));
        when(leaveBalanceAccrualRepository.existsByUserIdAndLeaveTypeIdAndAccrualYearAndAccrualMonth(1L, 3L, 2026, 7))
                .thenReturn(false);
        when(leaveBalanceRepository.findByUserIdAndLeaveTypeIdAndYear(1L, 3L, 2026))
                .thenReturn(Optional.of(balance));

        processor.process(user(1L, RoleType.EMPLOYEE), annualLeave(), 2026, 7, new BigDecimal("2.16"));

        assertEquals(new BigDecimal("12.16"), balance.getTotalDays());
        assertEquals(new BigDecimal("3.00"), balance.getUsedDays());
        assertEquals(new BigDecimal("9.16"), balance.getRemainingDays());
    }

    @Test
    void secondExecutionInSameMonthSkipsCredit() {
        when(leaveBalanceAccrualRepository.existsByUserIdAndLeaveTypeIdAndAccrualYearAndAccrualMonth(1L, 3L, 2026, 7))
                .thenReturn(true);

        LeaveAccrualEmployeeResult result = processor.process(user(1L, RoleType.EMPLOYEE), annualLeave(), 2026, 7, new BigDecimal("2.16"));

        assertTrue(result.skipped());
        verify(leaveBalanceRepository, never()).save(any());
        verify(leaveBalanceAccrualRepository, never()).save(any());
    }

    @Test
    void auditRecordIsCreatedWithBalanceBeforeAndAfter() {
        LeaveBalance balance = balance(new BigDecimal("10.00"), BigDecimal.ZERO.setScale(2), new BigDecimal("10.00"));
        when(leaveBalanceAccrualRepository.existsByUserIdAndLeaveTypeIdAndAccrualYearAndAccrualMonth(1L, 3L, 2026, 7))
                .thenReturn(false);
        when(leaveBalanceRepository.findByUserIdAndLeaveTypeIdAndYear(1L, 3L, 2026))
                .thenReturn(Optional.of(balance));

        processor.process(user(1L, RoleType.EMPLOYEE), annualLeave(), 2026, 7, new BigDecimal("2.16"));

        ArgumentCaptor<LeaveBalanceAccrual> accrualCaptor = ArgumentCaptor.forClass(LeaveBalanceAccrual.class);
        verify(leaveBalanceAccrualRepository).save(accrualCaptor.capture());
        LeaveBalanceAccrual accrual = accrualCaptor.getValue();
        assertEquals(new BigDecimal("2.16"), accrual.getCreditedDays());
        assertEquals(new BigDecimal("10.00"), accrual.getBalanceBefore());
        assertEquals(new BigDecimal("12.16"), accrual.getBalanceAfter());
    }

    private LeaveBalance balance(BigDecimal totalDays, BigDecimal usedDays, BigDecimal remainingDays) {
        LeaveBalance balance = new LeaveBalance();
        balance.setId(1L);
        balance.setUser(user(1L, RoleType.EMPLOYEE));
        balance.setLeaveType(annualLeave());
        balance.setYear(2026);
        balance.setTotalDays(totalDays);
        balance.setUsedDays(usedDays);
        balance.setRemainingDays(remainingDays);
        balance.setCreatedAt(LocalDateTime.now());
        return balance;
    }

    private User user(Long id, RoleType role) {
        User user = new User();
        user.setId(id);
        user.setUsername("user" + id);
        user.setEmail("user" + id + "@example.com");
        user.setPasswordHash("hash");
        user.setFirstName("User");
        user.setLastName(String.valueOf(id));
        user.setRole(role);
        user.setStatus(UserStatus.ACTIVE);
        user.setEnabled(true);
        return user;
    }

    private LeaveType annualLeave() {
        LeaveType leaveType = new LeaveType();
        leaveType.setId(3L);
        leaveType.setName("Annual Leave");
        return leaveType;
    }
}
