package com.xtensus.hrmanagementapi.leave.accrual.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.xtensus.hrmanagementapi.domain.entity.LeaveType;
import com.xtensus.hrmanagementapi.domain.entity.User;
import com.xtensus.hrmanagementapi.domain.enums.RoleType;
import com.xtensus.hrmanagementapi.domain.enums.UserStatus;
import com.xtensus.hrmanagementapi.leave.accrual.exception.InvalidLeaveAccrualException;
import com.xtensus.hrmanagementapi.leave.accrual.exception.LeaveAccrualConfigurationException;
import com.xtensus.hrmanagementapi.leave.accrual.mapper.LeaveAccrualMapper;
import com.xtensus.hrmanagementapi.repository.LeaveBalanceAccrualRepository;
import com.xtensus.hrmanagementapi.repository.LeaveTypeRepository;
import com.xtensus.hrmanagementapi.repository.UserRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LeaveAccrualServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private LeaveTypeRepository leaveTypeRepository;

    @Mock
    private LeaveBalanceAccrualRepository accrualRepository;

    @Mock
    private LeaveAccrualEmployeeProcessor processor;

    private LeaveAccrualService service;
    private LeaveAccrualProperties properties;

    @BeforeEach
    void setUp() {
        properties = new LeaveAccrualProperties();
        service = new LeaveAccrualService(
                userRepository,
                leaveTypeRepository,
                accrualRepository,
                processor,
                properties,
                new LeaveAccrualMapper()
        );
    }

    @Test
    void employeeManagerAndHrAreEligibleAndCredited() {
        mockAnnualLeave();
        List<User> users = List.of(user(1L, RoleType.EMPLOYEE, true, UserStatus.ACTIVE),
                user(2L, RoleType.MANAGER, true, UserStatus.ACTIVE),
                user(3L, RoleType.HR, true, UserStatus.ACTIVE));
        when(userRepository.findByStatusAndEnabledTrueAndRoleIn(eq(UserStatus.ACTIVE), eq(List.of(RoleType.EMPLOYEE, RoleType.MANAGER, RoleType.HR))))
                .thenReturn(users);
        when(processor.process(any(User.class), any(LeaveType.class), eq(2026), eq(7), eq(new BigDecimal("2.16"))))
                .thenReturn(LeaveAccrualEmployeeResult.credited(new BigDecimal("2.16")));

        var summary = service.run(2026, 7);

        assertEquals(3, summary.getEligibleUsers());
        assertEquals(3, summary.getCreditedUsers());
        assertEquals(new BigDecimal("6.48"), summary.getTotalCreditedDays());
    }

    @Test
    void disabledInactiveAndAdminUsersAreExcludedByRepositoryCriteria() {
        mockAnnualLeave();
        when(userRepository.findByStatusAndEnabledTrueAndRoleIn(eq(UserStatus.ACTIVE), eq(List.of(RoleType.EMPLOYEE, RoleType.MANAGER, RoleType.HR))))
                .thenReturn(List.of());

        var summary = service.run(2026, 7);

        assertEquals(0, summary.getEligibleUsers());
        verify(processor, never()).process(any(), any(), any(), any(), any());
    }

    @Test
    void secondExecutionSummaryCountsSkippedUsers() {
        mockAnnualLeave();
        when(userRepository.findByStatusAndEnabledTrueAndRoleIn(eq(UserStatus.ACTIVE), any()))
                .thenReturn(List.of(user(1L, RoleType.EMPLOYEE, true, UserStatus.ACTIVE)));
        when(processor.process(any(User.class), any(LeaveType.class), eq(2026), eq(7), any()))
                .thenReturn(LeaveAccrualEmployeeResult.skippedResult());

        var summary = service.run(2026, 7);

        assertEquals(0, summary.getCreditedUsers());
        assertEquals(1, summary.getSkippedUsers());
        assertEquals(new BigDecimal("0.00"), summary.getTotalCreditedDays());
    }

    @Test
    void anotherMonthAndYearArePassedToProcessor() {
        mockAnnualLeave();
        User user = user(1L, RoleType.EMPLOYEE, true, UserStatus.ACTIVE);
        when(userRepository.findByStatusAndEnabledTrueAndRoleIn(eq(UserStatus.ACTIVE), any()))
                .thenReturn(List.of(user));
        when(processor.process(eq(user), any(LeaveType.class), eq(2027), eq(1), any()))
                .thenReturn(LeaveAccrualEmployeeResult.credited(new BigDecimal("2.16")));

        service.run(2027, 1);

        verify(processor).process(eq(user), any(LeaveType.class), eq(2027), eq(1), eq(new BigDecimal("2.16")));
    }

    @Test
    void missingAnnualLeaveTypeFailsSafely() {
        when(leaveTypeRepository.findByNameIgnoreCase("Annual Leave")).thenReturn(Optional.empty());

        assertThrows(LeaveAccrualConfigurationException.class, () -> service.run(2026, 7));
    }

    @Test
    void oneEmployeeFailureDoesNotStopOthers() {
        mockAnnualLeave();
        User first = user(1L, RoleType.EMPLOYEE, true, UserStatus.ACTIVE);
        User second = user(2L, RoleType.MANAGER, true, UserStatus.ACTIVE);
        when(userRepository.findByStatusAndEnabledTrueAndRoleIn(eq(UserStatus.ACTIVE), any()))
                .thenReturn(List.of(first, second));
        when(processor.process(eq(first), any(LeaveType.class), eq(2026), eq(7), any()))
                .thenThrow(new RuntimeException("safe test failure"));
        when(processor.process(eq(second), any(LeaveType.class), eq(2026), eq(7), any()))
                .thenReturn(LeaveAccrualEmployeeResult.credited(new BigDecimal("2.16")));

        var summary = service.run(2026, 7);

        assertEquals(1, summary.getFailedUsers());
        assertEquals(1, summary.getCreditedUsers());
    }

    @Test
    void invalidMonthIsRejected() {
        assertThrows(InvalidLeaveAccrualException.class, () -> service.run(2026, 13));
    }

    @Test
    void bigDecimalPrecisionIsPreservedFromConfiguration() {
        properties.setMonthlyDays(new BigDecimal("2.1600"));
        mockAnnualLeave();
        when(userRepository.findByStatusAndEnabledTrueAndRoleIn(eq(UserStatus.ACTIVE), any()))
                .thenReturn(List.of(user(1L, RoleType.EMPLOYEE, true, UserStatus.ACTIVE)));
        when(processor.process(any(User.class), any(LeaveType.class), eq(2026), eq(7), eq(new BigDecimal("2.16"))))
                .thenReturn(LeaveAccrualEmployeeResult.credited(new BigDecimal("2.16")));

        var summary = service.run(2026, 7);

        assertEquals(new BigDecimal("2.16"), summary.getTotalCreditedDays());
    }

    private void mockAnnualLeave() {
        when(leaveTypeRepository.findByNameIgnoreCase("Annual Leave")).thenReturn(Optional.of(annualLeave()));
    }

    private User user(Long id, RoleType role, boolean enabled, UserStatus status) {
        User user = new User();
        user.setId(id);
        user.setUsername("user" + id);
        user.setEmail("user" + id + "@example.com");
        user.setPasswordHash("hash");
        user.setFirstName("User");
        user.setLastName(String.valueOf(id));
        user.setRole(role);
        user.setStatus(status);
        user.setEnabled(enabled);
        return user;
    }

    private LeaveType annualLeave() {
        LeaveType leaveType = new LeaveType();
        leaveType.setId(3L);
        leaveType.setName("Annual Leave");
        return leaveType;
    }
}
