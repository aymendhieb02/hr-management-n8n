package com.xtensus.hrmanagementapi.leave.request.mapper;

import com.xtensus.hrmanagementapi.domain.entity.LeaveRequest;
import com.xtensus.hrmanagementapi.domain.entity.LeaveType;
import com.xtensus.hrmanagementapi.domain.entity.User;
import com.xtensus.hrmanagementapi.leave.request.dto.ApproverSummary;
import com.xtensus.hrmanagementapi.leave.request.dto.LeaveRequestCreateRequest;
import com.xtensus.hrmanagementapi.leave.request.dto.LeaveRequestResponse;
import com.xtensus.hrmanagementapi.leave.request.dto.LeaveRequestUpdateRequest;
import com.xtensus.hrmanagementapi.leave.request.dto.LeaveTypeSummary;
import com.xtensus.hrmanagementapi.leave.request.dto.RequesterSummary;
import org.springframework.stereotype.Component;

@Component
public class LeaveRequestMapper {

    public LeaveRequest toEntity(LeaveRequestCreateRequest request) {
        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setStartDate(request.getStartDate());
        leaveRequest.setEndDate(request.getEndDate());
        leaveRequest.setReason(trimToNull(request.getReason()));
        return leaveRequest;
    }

    public LeaveRequestResponse toResponse(LeaveRequest entity) {
        LeaveRequestResponse response = new LeaveRequestResponse();
        response.setId(entity.getId());
        response.setRequester(toRequesterSummary(entity.getRequester()));
        response.setApprover(toApproverSummary(entity.getApprover()));
        response.setLeaveType(toLeaveTypeSummary(entity.getLeaveType()));
        response.setStartDate(entity.getStartDate());
        response.setEndDate(entity.getEndDate());
        response.setRequestedDays(entity.getRequestedDays());
        response.setReason(entity.getReason());
        response.setStatus(entity.getStatus());
        response.setSubmittedAt(entity.getSubmittedAt());
        response.setDecisionAt(entity.getDecisionAt());
        response.setDecisionComment(entity.getDecisionComment());
        return response;
    }

    public void updateEntity(LeaveRequestUpdateRequest request, LeaveRequest entity) {
        entity.setStartDate(request.getStartDate());
        entity.setEndDate(request.getEndDate());
        entity.setReason(trimToNull(request.getReason()));
    }

    private RequesterSummary toRequesterSummary(User requester) {
        if (requester == null) {
            return null;
        }

        RequesterSummary summary = new RequesterSummary();
        summary.setId(requester.getId());
        summary.setFirstName(requester.getFirstName());
        summary.setLastName(requester.getLastName());
        summary.setEmail(requester.getEmail());
        return summary;
    }

    private ApproverSummary toApproverSummary(User approver) {
        if (approver == null) {
            return null;
        }

        ApproverSummary summary = new ApproverSummary();
        summary.setId(approver.getId());
        summary.setFirstName(approver.getFirstName());
        summary.setLastName(approver.getLastName());
        summary.setEmail(approver.getEmail());
        return summary;
    }

    private LeaveTypeSummary toLeaveTypeSummary(LeaveType leaveType) {
        if (leaveType == null) {
            return null;
        }

        LeaveTypeSummary summary = new LeaveTypeSummary();
        summary.setId(leaveType.getId());
        summary.setName(leaveType.getName());
        return summary;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
