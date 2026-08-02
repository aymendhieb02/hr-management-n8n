package com.xtensus.hrmanagementapi.leave.accrual.mapper;

import com.xtensus.hrmanagementapi.domain.entity.LeaveBalanceAccrual;
import com.xtensus.hrmanagementapi.leave.accrual.dto.LeaveAccrualResponse;
import org.springframework.stereotype.Component;

@Component
public class LeaveAccrualMapper {

    public LeaveAccrualResponse toResponse(LeaveBalanceAccrual entity) {
        LeaveAccrualResponse response = new LeaveAccrualResponse();
        response.setId(entity.getId());
        response.setUserId(entity.getUser().getId());
        response.setUsername(entity.getUser().getUsername());
        response.setUserFullName(entity.getUser().getFirstName() + " " + entity.getUser().getLastName());
        response.setLeaveTypeId(entity.getLeaveType().getId());
        response.setLeaveTypeName(entity.getLeaveType().getName());
        response.setAccrualYear(entity.getAccrualYear());
        response.setAccrualMonth(entity.getAccrualMonth());
        response.setCreditedDays(entity.getCreditedDays());
        response.setBalanceBefore(entity.getBalanceBefore());
        response.setBalanceAfter(entity.getBalanceAfter());
        response.setExecutedAt(entity.getExecutedAt());
        response.setStatus(entity.getStatus());
        response.setErrorMessage(entity.getErrorMessage());
        return response;
    }
}
