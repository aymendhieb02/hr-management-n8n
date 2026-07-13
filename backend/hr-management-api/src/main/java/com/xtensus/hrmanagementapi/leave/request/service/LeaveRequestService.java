package com.xtensus.hrmanagementapi.leave.request.service;

import com.xtensus.hrmanagementapi.domain.entity.LeaveRequest;
import com.xtensus.hrmanagementapi.domain.entity.LeaveBalance;
import com.xtensus.hrmanagementapi.domain.entity.LeaveType;
import com.xtensus.hrmanagementapi.domain.entity.User;
import com.xtensus.hrmanagementapi.domain.enums.LeaveStatus;
import com.xtensus.hrmanagementapi.domain.enums.RoleType;
import com.xtensus.hrmanagementapi.leave.balance.exception.InsufficientLeaveBalanceException;
import com.xtensus.hrmanagementapi.leave.balance.exception.LeaveBalanceNotFoundException;
import com.xtensus.hrmanagementapi.leave.request.dto.LeaveApprovalRequest;
import com.xtensus.hrmanagementapi.leave.request.dto.LeaveRequestCreateRequest;
import com.xtensus.hrmanagementapi.leave.request.dto.LeaveRequestResponse;
import com.xtensus.hrmanagementapi.leave.request.dto.LeaveRequestUpdateRequest;
import com.xtensus.hrmanagementapi.leave.request.dto.LeaveRejectionRequest;
import com.xtensus.hrmanagementapi.leave.request.exception.InvalidLeaveRequestException;
import com.xtensus.hrmanagementapi.leave.request.exception.LeaveDecisionNotAllowedException;
import com.xtensus.hrmanagementapi.leave.request.exception.LeaveRequestNotFoundException;
import com.xtensus.hrmanagementapi.leave.request.exception.UnauthorizedApproverException;
import com.xtensus.hrmanagementapi.leave.request.exception.UpdateNotAllowedException;
import com.xtensus.hrmanagementapi.leave.request.mapper.LeaveRequestMapper;
import com.xtensus.hrmanagementapi.leave.type.exception.LeaveTypeNotFoundException;
import com.xtensus.hrmanagementapi.notification.dto.NotificationCreateRequest;
import com.xtensus.hrmanagementapi.notification.service.NotificationService;
import com.xtensus.hrmanagementapi.repository.LeaveRequestRepository;
import com.xtensus.hrmanagementapi.repository.LeaveBalanceRepository;
import com.xtensus.hrmanagementapi.repository.LeaveTypeRepository;
import com.xtensus.hrmanagementapi.repository.UserRepository;
import com.xtensus.hrmanagementapi.user.exception.UserNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final UserRepository userRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final LeaveRequestMapper leaveRequestMapper;
    private final NotificationService notificationService;

    public LeaveRequestService(
            LeaveRequestRepository leaveRequestRepository,
            LeaveBalanceRepository leaveBalanceRepository,
            UserRepository userRepository,
            LeaveTypeRepository leaveTypeRepository,
            LeaveRequestMapper leaveRequestMapper,
            NotificationService notificationService
    ) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.leaveBalanceRepository = leaveBalanceRepository;
        this.userRepository = userRepository;
        this.leaveTypeRepository = leaveTypeRepository;
        this.leaveRequestMapper = leaveRequestMapper;
        this.notificationService = notificationService;
    }

    @Transactional
    public LeaveRequestResponse create(LeaveRequestCreateRequest request) {
        User requester = userRepository.findById(request.getRequesterId())
                .orElseThrow(() -> new UserNotFoundException(request.getRequesterId()));
        if (!Boolean.TRUE.equals(requester.getEnabled())) {
            throw new InvalidLeaveRequestException("Requester cannot create leave while disabled");
        }

        LeaveType leaveType = getLeaveType(request.getLeaveTypeId());
        validateDates(request.getStartDate(), request.getEndDate());

        LeaveRequest leaveRequest = leaveRequestMapper.toEntity(request);
        leaveRequest.setRequester(requester);
        leaveRequest.setApprover(requester.getManager());
        leaveRequest.setLeaveType(leaveType);
        leaveRequest.setRequestedDays(calculateRequestedDays(request.getStartDate(), request.getEndDate()));
        leaveRequest.setStatus(LeaveStatus.PENDING);
        leaveRequest.setSubmittedAt(LocalDateTime.now());
        leaveRequest.setDecisionAt(null);
        leaveRequest.setDecisionComment(null);
        leaveRequest.setCreatedAt(LocalDateTime.now());

        LeaveRequest saved = leaveRequestRepository.save(leaveRequest);
        notifyManagerOfNewLeaveRequest(saved);
        return leaveRequestMapper.toResponse(saved);
    }

    @Transactional
    public LeaveRequestResponse update(Long id, LeaveRequestUpdateRequest request) {
        LeaveRequest leaveRequest = getLeaveRequest(id);
        ensurePending(leaveRequest, "Only pending leave requests can be updated");
        LeaveType leaveType = getLeaveType(request.getLeaveTypeId());
        validateDates(request.getStartDate(), request.getEndDate());

        leaveRequestMapper.updateEntity(request, leaveRequest);
        leaveRequest.setLeaveType(leaveType);
        leaveRequest.setRequestedDays(calculateRequestedDays(request.getStartDate(), request.getEndDate()));
        leaveRequest.setUpdatedAt(LocalDateTime.now());
        leaveRequest.setDecisionAt(null);
        leaveRequest.setDecisionComment(null);

        return leaveRequestMapper.toResponse(leaveRequestRepository.save(leaveRequest));
    }

    @Transactional
    public void delete(Long id) {
        LeaveRequest leaveRequest = getLeaveRequest(id);
        ensurePending(leaveRequest, "Only pending leave requests can be deleted");
        leaveRequestRepository.delete(leaveRequest);
    }

    @Transactional
    public LeaveRequestResponse approve(Long requestId, LeaveApprovalRequest request) {
        LeaveRequest leaveRequest = getLeaveRequest(requestId);
        User approver = getApprover(request.getApproverId());
        validateDecisionAllowed(leaveRequest, approver);
        applyApprovedBalanceUsage(leaveRequest);

        leaveRequest.setStatus(LeaveStatus.APPROVED);
        leaveRequest.setDecisionComment(trimToNull(request.getComment()));
        leaveRequest.setDecisionAt(LocalDateTime.now());
        leaveRequest.setUpdatedAt(LocalDateTime.now());

        LeaveRequest saved = leaveRequestRepository.save(leaveRequest);
        notifyRequester(saved.getRequester(), "Leave Approved", "Your leave request has been approved.");
        return leaveRequestMapper.toResponse(saved);
    }

    @Transactional
    public LeaveRequestResponse reject(Long requestId, LeaveRejectionRequest request) {
        LeaveRequest leaveRequest = getLeaveRequest(requestId);
        User approver = getApprover(request.getApproverId());
        validateDecisionAllowed(leaveRequest, approver);
        String decisionComment = trimToNull(request.getComment());
        if (decisionComment == null) {
            throw new InvalidLeaveRequestException("Rejection comment is required");
        }

        leaveRequest.setStatus(LeaveStatus.REJECTED);
        leaveRequest.setDecisionComment(decisionComment);
        leaveRequest.setDecisionAt(LocalDateTime.now());
        leaveRequest.setUpdatedAt(LocalDateTime.now());

        LeaveRequest saved = leaveRequestRepository.save(leaveRequest);
        notifyRequester(saved.getRequester(), "Leave Rejected", "Your leave request has been rejected.");
        return leaveRequestMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public LeaveRequestResponse findById(Long id) {
        return leaveRequestMapper.toResponse(getLeaveRequest(id));
    }

    @Transactional(readOnly = true)
    public List<LeaveRequestResponse> findAll() {
        return leaveRequestRepository.findAll()
                .stream()
                .map(leaveRequestMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LeaveRequestResponse> findByRequester(Long requesterId) {
        return leaveRequestRepository.findByRequesterIdOrderBySubmittedAtDesc(requesterId)
                .stream()
                .map(leaveRequestMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LeaveRequestResponse> findByApprover(Long approverId) {
        return leaveRequestRepository.findByApproverIdOrderBySubmittedAtDesc(approverId)
                .stream()
                .map(leaveRequestMapper::toResponse)
                .toList();
    }

    private LeaveRequest getLeaveRequest(Long id) {
        return leaveRequestRepository.findById(id)
                .orElseThrow(() -> new LeaveRequestNotFoundException(id));
    }

    private LeaveType getLeaveType(Long leaveTypeId) {
        return leaveTypeRepository.findById(leaveTypeId)
                .orElseThrow(() -> new LeaveTypeNotFoundException(leaveTypeId));
    }

    private User getApprover(Long approverId) {
        return userRepository.findById(approverId)
                .orElseThrow(() -> new UserNotFoundException(approverId));
    }

    private void validateDates(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new InvalidLeaveRequestException("Start date cannot be after end date");
        }

        if (startDate.isBefore(LocalDate.now())) {
            throw new InvalidLeaveRequestException("Start date cannot be in the past");
        }
    }

    private BigDecimal calculateRequestedDays(LocalDate startDate, LocalDate endDate) {
        return BigDecimal.valueOf(ChronoUnit.DAYS.between(startDate, endDate) + 1);
    }

    private void ensurePending(LeaveRequest leaveRequest, String message) {
        if (leaveRequest.getStatus() != LeaveStatus.PENDING) {
            throw new UpdateNotAllowedException(message);
        }
    }

    private void validateDecisionAllowed(LeaveRequest leaveRequest, User approver) {
        if (leaveRequest.getStatus() != LeaveStatus.PENDING) {
            throw new LeaveDecisionNotAllowedException("Only pending leave requests can be approved or rejected");
        }

        if (leaveRequest.getApprover() == null) {
            throw new UnauthorizedApproverException("Leave request has no assigned approver");
        }

        if (!leaveRequest.getApprover().getId().equals(approver.getId())) {
            throw new UnauthorizedApproverException("Only the assigned approver can decide this leave request");
        }

        if (!Boolean.TRUE.equals(approver.getEnabled())) {
            throw new UnauthorizedApproverException("Approver is disabled");
        }

        if (!isDecisionRole(approver.getRole())) {
            throw new UnauthorizedApproverException("Approver role is not allowed to decide leave requests");
        }

        if (leaveRequest.getRequester().getId().equals(approver.getId())) {
            throw new UnauthorizedApproverException("Requester cannot decide their own leave request");
        }
    }

    private boolean isDecisionRole(RoleType role) {
        return role == RoleType.MANAGER || role == RoleType.HR || role == RoleType.ADMIN;
    }

    private void applyApprovedBalanceUsage(LeaveRequest leaveRequest) {
        Integer balanceYear = leaveRequest.getStartDate().getYear();
        LeaveBalance balance = leaveBalanceRepository.findByUserIdAndLeaveTypeIdAndYear(
                        leaveRequest.getRequester().getId(),
                        leaveRequest.getLeaveType().getId(),
                        balanceYear
                )
                .orElseThrow(() -> new LeaveBalanceNotFoundException(
                        leaveRequest.getRequester().getId(),
                        leaveRequest.getLeaveType().getId(),
                        balanceYear
                ));

        if (balance.getRemainingDays().compareTo(leaveRequest.getRequestedDays()) < 0) {
            throw new InsufficientLeaveBalanceException();
        }

        balance.setUsedDays(balance.getUsedDays().add(leaveRequest.getRequestedDays()));
        balance.setRemainingDays(balance.getRemainingDays().subtract(leaveRequest.getRequestedDays()));
        balance.setUpdatedAt(LocalDateTime.now());
        leaveBalanceRepository.save(balance);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void notifyManagerOfNewLeaveRequest(LeaveRequest leaveRequest) {
        if (leaveRequest.getApprover() == null) {
            return;
        }

        String employeeName = leaveRequest.getRequester().getFirstName() + " " + leaveRequest.getRequester().getLastName();
        notifyRequester(
                leaveRequest.getApprover(),
                "New Leave Request",
                employeeName + " submitted a leave request."
        );
    }

    private void notifyRequester(User recipient, String title, String message) {
        NotificationCreateRequest request = new NotificationCreateRequest();
        request.setRecipientId(recipient.getId());
        request.setTitle(title);
        request.setMessage(message);
        notificationService.create(request);
    }
}
