package com.xtensus.hrmanagementapi.leave.balance.mapper;

import com.xtensus.hrmanagementapi.domain.entity.LeaveBalance;
import com.xtensus.hrmanagementapi.domain.entity.LeaveType;
import com.xtensus.hrmanagementapi.domain.entity.User;
import com.xtensus.hrmanagementapi.leave.balance.dto.LeaveBalanceRequest;
import com.xtensus.hrmanagementapi.leave.balance.dto.LeaveBalanceResponse;
import com.xtensus.hrmanagementapi.leave.balance.dto.LeaveBalanceTypeSummary;
import com.xtensus.hrmanagementapi.leave.balance.dto.LeaveBalanceUserSummary;
import org.springframework.stereotype.Component;

@Component
public class LeaveBalanceMapper {

    public LeaveBalance toEntity(LeaveBalanceRequest request) {
        LeaveBalance entity = new LeaveBalance();
        entity.setYear(request.getYear());
        entity.setTotalDays(request.getTotalDays());
        return entity;
    }

    public LeaveBalanceResponse toResponse(LeaveBalance entity) {
        LeaveBalanceResponse response = new LeaveBalanceResponse();
        response.setId(entity.getId());
        response.setUser(toUserSummary(entity.getUser()));
        response.setLeaveType(toLeaveTypeSummary(entity.getLeaveType()));
        response.setYear(entity.getYear());
        response.setTotalDays(entity.getTotalDays());
        response.setUsedDays(entity.getUsedDays());
        response.setRemainingDays(entity.getRemainingDays());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        return response;
    }

    public void updateEntity(LeaveBalanceRequest request, LeaveBalance entity) {
        entity.setYear(request.getYear());
        entity.setTotalDays(request.getTotalDays());
    }

    private LeaveBalanceUserSummary toUserSummary(User user) {
        LeaveBalanceUserSummary summary = new LeaveBalanceUserSummary();
        summary.setId(user.getId());
        summary.setFirstName(user.getFirstName());
        summary.setLastName(user.getLastName());
        summary.setEmail(user.getEmail());
        return summary;
    }

    private LeaveBalanceTypeSummary toLeaveTypeSummary(LeaveType leaveType) {
        LeaveBalanceTypeSummary summary = new LeaveBalanceTypeSummary();
        summary.setId(leaveType.getId());
        summary.setName(leaveType.getName());
        return summary;
    }
}
