package com.xtensus.hrmanagementapi.leave.accrual.service;

import com.xtensus.hrmanagementapi.domain.entity.LeaveBalance;
import com.xtensus.hrmanagementapi.domain.entity.LeaveBalanceAccrual;
import com.xtensus.hrmanagementapi.domain.entity.LeaveType;
import com.xtensus.hrmanagementapi.domain.entity.User;
import com.xtensus.hrmanagementapi.domain.enums.LeaveAccrualStatus;
import com.xtensus.hrmanagementapi.repository.LeaveBalanceAccrualRepository;
import com.xtensus.hrmanagementapi.repository.LeaveBalanceRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LeaveAccrualEmployeeProcessor {

    private final LeaveBalanceRepository leaveBalanceRepository;
    private final LeaveBalanceAccrualRepository leaveBalanceAccrualRepository;

    public LeaveAccrualEmployeeProcessor(
            LeaveBalanceRepository leaveBalanceRepository,
            LeaveBalanceAccrualRepository leaveBalanceAccrualRepository
    ) {
        this.leaveBalanceRepository = leaveBalanceRepository;
        this.leaveBalanceAccrualRepository = leaveBalanceAccrualRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public LeaveAccrualEmployeeResult process(
            User user,
            LeaveType annualLeave,
            Integer year,
            Integer month,
            BigDecimal monthlyDays
    ) {
        if (leaveBalanceAccrualRepository.existsByUserIdAndLeaveTypeIdAndAccrualYearAndAccrualMonth(
                user.getId(),
                annualLeave.getId(),
                year,
                month
        )) {
            return LeaveAccrualEmployeeResult.skippedResult();
        }

        LocalDateTime now = LocalDateTime.now();
        BigDecimal credit = monthlyDays.setScale(2, RoundingMode.HALF_UP);
        LeaveBalance balance = leaveBalanceRepository.findByUserIdAndLeaveTypeIdAndYear(
                user.getId(),
                annualLeave.getId(),
                year
        ).orElseGet(() -> newBalance(user, annualLeave, year, now));

        BigDecimal before = balance.getRemainingDays().setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalAfter = balance.getTotalDays().add(credit).setScale(2, RoundingMode.HALF_UP);
        BigDecimal remainingAfter = balance.getRemainingDays().add(credit).setScale(2, RoundingMode.HALF_UP);

        balance.setTotalDays(totalAfter);
        balance.setRemainingDays(remainingAfter);
        balance.setUpdatedAt(now);
        leaveBalanceRepository.save(balance);

        LeaveBalanceAccrual accrual = new LeaveBalanceAccrual();
        accrual.setUser(user);
        accrual.setLeaveType(annualLeave);
        accrual.setAccrualYear(year);
        accrual.setAccrualMonth(month);
        accrual.setCreditedDays(credit);
        accrual.setBalanceBefore(before);
        accrual.setBalanceAfter(remainingAfter);
        accrual.setExecutedAt(now);
        accrual.setStatus(LeaveAccrualStatus.SUCCESS);
        leaveBalanceAccrualRepository.save(accrual);

        return LeaveAccrualEmployeeResult.credited(credit);
    }

    private LeaveBalance newBalance(User user, LeaveType annualLeave, Integer year, LocalDateTime now) {
        LeaveBalance balance = new LeaveBalance();
        balance.setUser(user);
        balance.setLeaveType(annualLeave);
        balance.setYear(year);
        balance.setTotalDays(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        balance.setUsedDays(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        balance.setRemainingDays(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        balance.setCreatedAt(now);
        return balance;
    }
}
