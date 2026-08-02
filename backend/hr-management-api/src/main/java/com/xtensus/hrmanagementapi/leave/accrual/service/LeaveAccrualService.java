package com.xtensus.hrmanagementapi.leave.accrual.service;

import com.xtensus.hrmanagementapi.domain.entity.LeaveBalanceAccrual;
import com.xtensus.hrmanagementapi.domain.entity.LeaveType;
import com.xtensus.hrmanagementapi.domain.entity.User;
import com.xtensus.hrmanagementapi.domain.enums.RoleType;
import com.xtensus.hrmanagementapi.domain.enums.UserStatus;
import com.xtensus.hrmanagementapi.leave.accrual.dto.LeaveAccrualResponse;
import com.xtensus.hrmanagementapi.leave.accrual.dto.LeaveAccrualRunSummary;
import com.xtensus.hrmanagementapi.leave.accrual.exception.InvalidLeaveAccrualException;
import com.xtensus.hrmanagementapi.leave.accrual.exception.LeaveAccrualConfigurationException;
import com.xtensus.hrmanagementapi.leave.accrual.mapper.LeaveAccrualMapper;
import com.xtensus.hrmanagementapi.repository.LeaveBalanceAccrualRepository;
import com.xtensus.hrmanagementapi.repository.LeaveTypeRepository;
import com.xtensus.hrmanagementapi.repository.UserRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LeaveAccrualService {

    private static final Logger log = LoggerFactory.getLogger(LeaveAccrualService.class);
    private static final String ANNUAL_LEAVE_NAME = "Annual Leave";
    private static final List<RoleType> ELIGIBLE_ROLES = List.of(RoleType.EMPLOYEE, RoleType.MANAGER, RoleType.HR);

    private final UserRepository userRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final LeaveBalanceAccrualRepository leaveBalanceAccrualRepository;
    private final LeaveAccrualEmployeeProcessor employeeProcessor;
    private final LeaveAccrualProperties properties;
    private final LeaveAccrualMapper leaveAccrualMapper;

    public LeaveAccrualService(
            UserRepository userRepository,
            LeaveTypeRepository leaveTypeRepository,
            LeaveBalanceAccrualRepository leaveBalanceAccrualRepository,
            LeaveAccrualEmployeeProcessor employeeProcessor,
            LeaveAccrualProperties properties,
            LeaveAccrualMapper leaveAccrualMapper
    ) {
        this.userRepository = userRepository;
        this.leaveTypeRepository = leaveTypeRepository;
        this.leaveBalanceAccrualRepository = leaveBalanceAccrualRepository;
        this.employeeProcessor = employeeProcessor;
        this.properties = properties;
        this.leaveAccrualMapper = leaveAccrualMapper;
    }

    public LeaveAccrualRunSummary run(Integer requestedYear, Integer requestedMonth) {
        LocalDate now = LocalDate.now(ZoneId.of(properties.getZone()));
        Integer year = requestedYear == null ? now.getYear() : requestedYear;
        Integer month = requestedMonth == null ? now.getMonthValue() : requestedMonth;
        validatePeriod(year, month);

        BigDecimal monthlyDays = properties.getMonthlyDays().setScale(2, RoundingMode.HALF_UP);
        if (monthlyDays.compareTo(BigDecimal.ZERO) <= 0) {
            throw new LeaveAccrualConfigurationException("Monthly accrual days must be greater than zero");
        }

        log.info("Leave accrual run started year={} month={}", year, month);
        LeaveType annualLeave = leaveTypeRepository.findByNameIgnoreCase(ANNUAL_LEAVE_NAME)
                .orElseThrow(() -> new LeaveAccrualConfigurationException("Annual Leave type is not configured"));
        List<User> eligibleUsers = userRepository.findByStatusAndEnabledTrueAndRoleIn(
                UserStatus.ACTIVE,
                ELIGIBLE_ROLES
        );
        log.info("Leave accrual eligible employee count={} year={} month={}", eligibleUsers.size(), year, month);

        int creditedUsers = 0;
        int skippedUsers = 0;
        int failedUsers = 0;
        BigDecimal totalCredited = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        for (User user : eligibleUsers) {
            try {
                LeaveAccrualEmployeeResult result = employeeProcessor.process(
                        user,
                        annualLeave,
                        year,
                        month,
                        monthlyDays
                );
                if (result.credited()) {
                    creditedUsers++;
                    totalCredited = totalCredited.add(result.creditedDays()).setScale(2, RoundingMode.HALF_UP);
                    log.info("Leave accrual employee credited userId={} year={} month={} creditedDays={}",
                            user.getId(), year, month, result.creditedDays());
                } else if (result.skipped()) {
                    skippedUsers++;
                    log.info("Leave accrual employee skipped userId={} year={} month={} reason=already_processed",
                            user.getId(), year, month);
                }
            } catch (Exception exception) {
                failedUsers++;
                log.warn("Leave accrual employee failure userId={} year={} month={} error={}",
                        user.getId(), year, month, exception.getClass().getSimpleName());
            }
        }

        LeaveAccrualRunSummary summary = new LeaveAccrualRunSummary();
        summary.setYear(year);
        summary.setMonth(month);
        summary.setEligibleUsers(eligibleUsers.size());
        summary.setCreditedUsers(creditedUsers);
        summary.setSkippedUsers(skippedUsers);
        summary.setFailedUsers(failedUsers);
        summary.setTotalCreditedDays(totalCredited);
        summary.setExecutedAt(LocalDateTime.now(ZoneId.of(properties.getZone())));
        log.info("Leave accrual run completed year={} month={} eligibleUsers={} creditedUsers={} skippedUsers={} failedUsers={} totalCreditedDays={}",
                year, month, eligibleUsers.size(), creditedUsers, skippedUsers, failedUsers, totalCredited);
        return summary;
    }

    @Transactional(readOnly = true)
    public List<LeaveAccrualResponse> findAll() {
        return leaveBalanceAccrualRepository.findAll()
                .stream()
                .map(leaveAccrualMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LeaveAccrualResponse> findByUser(Long userId) {
        return leaveBalanceAccrualRepository.findByUserIdOrderByExecutedAtDesc(userId)
                .stream()
                .map(leaveAccrualMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LeaveAccrualResponse> findByYearAndMonth(Integer year, Integer month) {
        validatePeriod(year, month);
        return leaveBalanceAccrualRepository.findByAccrualYearAndAccrualMonthOrderByExecutedAtDesc(year, month)
                .stream()
                .map(leaveAccrualMapper::toResponse)
                .toList();
    }

    private void validatePeriod(Integer year, Integer month) {
        if (year == null || year < 2000 || year > 2100) {
            throw new InvalidLeaveAccrualException("Year must be between 2000 and 2100");
        }
        if (month == null || month < 1 || month > 12) {
            throw new InvalidLeaveAccrualException("Month must be between 1 and 12");
        }
    }
}
