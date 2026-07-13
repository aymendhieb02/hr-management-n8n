package com.xtensus.hrmanagementapi.leave.request.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.xtensus.hrmanagementapi.domain.entity.LeaveBalance;
import com.xtensus.hrmanagementapi.domain.entity.LeaveRequest;
import com.xtensus.hrmanagementapi.domain.entity.LeaveType;
import com.xtensus.hrmanagementapi.domain.entity.User;
import com.xtensus.hrmanagementapi.domain.enums.LeaveStatus;
import com.xtensus.hrmanagementapi.domain.enums.RoleType;
import com.xtensus.hrmanagementapi.domain.enums.UserStatus;
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
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LeaveRequestServiceTest {

    @Mock
    private LeaveRequestRepository leaveRequestRepository;

    @Mock
    private LeaveBalanceRepository leaveBalanceRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private LeaveTypeRepository leaveTypeRepository;

    @Mock
    private NotificationService notificationService;

    private LeaveRequestService leaveRequestService;

    @BeforeEach
    void setUp() {
        leaveRequestService = new LeaveRequestService(
                leaveRequestRepository,
                leaveBalanceRepository,
                userRepository,
                leaveTypeRepository,
                new LeaveRequestMapper(),
                notificationService
        );
    }

    @Test
    void createLeaveRequestSuccessfully() {
        User manager = user(2L, true);
        User requester = user(1L, true);
        requester.setManager(manager);
        LeaveRequest leaveRequest = decisionRequest(LeaveStatus.PENDING);
        when(userRepository.findById(1L)).thenReturn(Optional.of(requester));
        when(leaveTypeRepository.findById(3L)).thenReturn(Optional.of(leaveType(3L)));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenAnswer(invocation -> {
            LeaveRequest saved = invocation.getArgument(0);
            saved.setId(10L);
            return saved;
        });

        LeaveRequestResponse response = leaveRequestService.create(createRequest());

        assertEquals(10L, response.getId());
        assertEquals("Annual Leave", response.getLeaveType().getName());
        assertEquals(LeaveStatus.PENDING, response.getStatus());
        assertEquals(2L, response.getRequestedDays().longValue());
        assertNull(response.getDecisionAt());
        assertNull(response.getDecisionComment());
    }

    @Test
    void requesterNotFoundReturnsNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> leaveRequestService.create(createRequest()));
    }

    @Test
    void leaveTypeNotFoundReturnsNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L, true)));
        when(leaveTypeRepository.findById(3L)).thenReturn(Optional.empty());

        assertThrows(LeaveTypeNotFoundException.class, () -> leaveRequestService.create(createRequest()));
    }

    @Test
    void disabledRequesterReturnsBadRequest() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L, false)));

        assertThrows(InvalidLeaveRequestException.class, () -> leaveRequestService.create(createRequest()));
    }

    @Test
    void invalidDatesReturnBadRequest() {
        LeaveRequestCreateRequest request = createRequest();
        request.setStartDate(LocalDate.now().plusDays(5));
        request.setEndDate(LocalDate.now().plusDays(2));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L, true)));
        when(leaveTypeRepository.findById(3L)).thenReturn(Optional.of(leaveType(3L)));

        assertThrows(InvalidLeaveRequestException.class, () -> leaveRequestService.create(request));
    }

    @Test
    void pastDateReturnsBadRequest() {
        LeaveRequestCreateRequest request = createRequest();
        request.setStartDate(LocalDate.now().minusDays(1));
        request.setEndDate(LocalDate.now().plusDays(1));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L, true)));
        when(leaveTypeRepository.findById(3L)).thenReturn(Optional.of(leaveType(3L)));

        assertThrows(InvalidLeaveRequestException.class, () -> leaveRequestService.create(request));
    }

    @Test
    void requestedDaysAreCalculatedAutomatically() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L, true)));
        when(leaveTypeRepository.findById(3L)).thenReturn(Optional.of(leaveType(3L)));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        leaveRequestService.create(createRequest());

        ArgumentCaptor<LeaveRequest> captor = ArgumentCaptor.forClass(LeaveRequest.class);
        verify(leaveRequestRepository).save(captor.capture());
        assertEquals(BigDecimal.valueOf(2), captor.getValue().getRequestedDays());
    }

    @Test
    void statusDefaultsToPending() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L, true)));
        when(leaveTypeRepository.findById(3L)).thenReturn(Optional.of(leaveType(3L)));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        leaveRequestService.create(createRequest());

        ArgumentCaptor<LeaveRequest> captor = ArgumentCaptor.forClass(LeaveRequest.class);
        verify(leaveRequestRepository).save(captor.capture());
        assertEquals(LeaveStatus.PENDING, captor.getValue().getStatus());
    }

    @Test
    void approverAssignedAutomatically() {
        User manager = user(2L, true);
        User requester = user(1L, true);
        requester.setManager(manager);
        when(userRepository.findById(1L)).thenReturn(Optional.of(requester));
        when(leaveTypeRepository.findById(3L)).thenReturn(Optional.of(leaveType(3L)));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        leaveRequestService.create(createRequest());

        ArgumentCaptor<LeaveRequest> captor = ArgumentCaptor.forClass(LeaveRequest.class);
        verify(leaveRequestRepository).save(captor.capture());
        assertSame(manager, captor.getValue().getApprover());
    }

    @Test
    void updatePendingRequestSuccessfully() {
        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(leaveRequest(1L, LeaveStatus.PENDING)));
        when(leaveTypeRepository.findById(3L)).thenReturn(Optional.of(leaveType(3L)));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LeaveRequestResponse response = leaveRequestService.update(1L, updateRequest());

        assertEquals("Updated reason", response.getReason());
        assertEquals(2L, response.getRequestedDays().longValue());
    }

    @Test
    void rejectUpdateAfterApproval() {
        when(leaveRequestRepository.findById(1L))
                .thenReturn(Optional.of(leaveRequest(1L, LeaveStatus.APPROVED)));

        assertThrows(UpdateNotAllowedException.class, () -> leaveRequestService.update(1L, updateRequest()));
    }

    @Test
    void deletePendingRequestSuccessfully() {
        LeaveRequest existing = leaveRequest(1L, LeaveStatus.PENDING);
        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(existing));

        leaveRequestService.delete(1L);

        verify(leaveRequestRepository).delete(existing);
    }

    @Test
    void getRequesterRequestsSuccessfully() {
        when(leaveRequestRepository.findByRequesterIdOrderBySubmittedAtDesc(1L))
                .thenReturn(List.of(leaveRequest(1L, LeaveStatus.PENDING)));

        List<LeaveRequestResponse> responses = leaveRequestService.findByRequester(1L);

        assertEquals(1, responses.size());
        assertEquals(1L, responses.get(0).getRequester().getId());
    }

    @Test
    void getApproverRequestsSuccessfully() {
        LeaveRequest leaveRequest = leaveRequest(1L, LeaveStatus.PENDING);
        leaveRequest.setApprover(user(2L, true));
        when(leaveRequestRepository.findByApproverIdOrderBySubmittedAtDesc(2L))
                .thenReturn(List.of(leaveRequest));

        List<LeaveRequestResponse> responses = leaveRequestService.findByApprover(2L);

        assertEquals(1, responses.size());
        assertEquals(LeaveStatus.PENDING, responses.get(0).getStatus());
    }

    @Test
    void missingLeaveRequestReturnsNotFound() {
        when(leaveRequestRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(LeaveRequestNotFoundException.class, () -> leaveRequestService.findById(99L));
    }

    @Test
    void assignedManagerApprovesPendingRequestSuccessfully() {
        LeaveRequest leaveRequest = decisionRequest(LeaveStatus.PENDING);
        User approver = user(2L, true, RoleType.MANAGER);
        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(leaveRequest));
        when(userRepository.findById(2L)).thenReturn(Optional.of(approver));
        when(leaveBalanceRepository.findByUserIdAndLeaveTypeIdAndYear(1L, 3L, leaveRequest.getStartDate().getYear()))
                .thenReturn(Optional.of(balance(BigDecimal.valueOf(10), BigDecimal.ZERO, BigDecimal.valueOf(10))));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LeaveRequestResponse response = leaveRequestService.approve(1L, approvalRequest(" Approved "));

        assertEquals(LeaveStatus.APPROVED, response.getStatus());
        assertEquals("Approved", response.getDecisionComment());
        assertNotNull(response.getDecisionAt());
        assertEquals(2L, response.getApprover().getId());
    }

    @Test
    void approvalSetsStatusApproved() {
        LeaveRequestResponse response = approveDecisionRequest("Looks good");

        assertEquals(LeaveStatus.APPROVED, response.getStatus());
    }

    @Test
    void approvalSetsDecisionAt() {
        LeaveRequestResponse response = approveDecisionRequest("Looks good");

        assertNotNull(response.getDecisionAt());
    }

    @Test
    void approvalStoresOptionalComment() {
        LeaveRequestResponse response = approveDecisionRequest(" Approved ");

        assertEquals("Approved", response.getDecisionComment());
    }

    @Test
    void approvalAllowsNullComment() {
        LeaveRequestResponse response = approveDecisionRequest(null);

        assertNull(response.getDecisionComment());
    }

    @Test
    void assignedManagerRejectsPendingRequestSuccessfully() {
        LeaveRequestResponse response = rejectDecisionRequest(" Not enough coverage ");

        assertEquals(LeaveStatus.REJECTED, response.getStatus());
        assertEquals("Not enough coverage", response.getDecisionComment());
        assertNotNull(response.getDecisionAt());
    }

    @Test
    void rejectionSetsStatusRejected() {
        LeaveRequestResponse response = rejectDecisionRequest("Insufficient reason");

        assertEquals(LeaveStatus.REJECTED, response.getStatus());
    }

    @Test
    void rejectionRequiresNonBlankComment() {
        LeaveRequest leaveRequest = decisionRequest(LeaveStatus.PENDING);
        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(leaveRequest));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user(2L, true, RoleType.MANAGER)));

        assertThrows(InvalidLeaveRequestException.class, () -> leaveRequestService.reject(1L, rejectionRequest(" ")));
    }

    @Test
    void rejectionStoresDecisionComment() {
        LeaveRequestResponse response = rejectDecisionRequest(" Rejected because of team coverage ");

        assertEquals("Rejected because of team coverage", response.getDecisionComment());
    }

    @Test
    void approvalMissingLeaveRequestReturnsNotFound() {
        when(leaveRequestRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(LeaveRequestNotFoundException.class, () -> leaveRequestService.approve(99L, approvalRequest(null)));
    }

    @Test
    void missingApproverReturnsNotFound() {
        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(decisionRequest(LeaveStatus.PENDING)));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> leaveRequestService.approve(1L, approvalRequest(null)));
    }

    @Test
    void requestWithoutAssignedApproverReturnsForbidden() {
        LeaveRequest leaveRequest = decisionRequest(LeaveStatus.PENDING);
        leaveRequest.setApprover(null);
        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(leaveRequest));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user(2L, true, RoleType.MANAGER)));

        assertThrows(UnauthorizedApproverException.class, () -> leaveRequestService.approve(1L, approvalRequest(null)));
    }

    @Test
    void wrongApproverReturnsForbidden() {
        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(decisionRequest(LeaveStatus.PENDING)));
        when(userRepository.findById(99L)).thenReturn(Optional.of(user(99L, true, RoleType.MANAGER)));

        assertThrows(UnauthorizedApproverException.class, () -> leaveRequestService.approve(1L, approvalRequest(99L, null)));
    }

    @Test
    void disabledApproverReturnsForbidden() {
        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(decisionRequest(LeaveStatus.PENDING)));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user(2L, false, RoleType.MANAGER)));

        assertThrows(UnauthorizedApproverException.class, () -> leaveRequestService.approve(1L, approvalRequest(null)));
    }

    @Test
    void employeeRoleCannotApprove() {
        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(decisionRequest(LeaveStatus.PENDING)));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user(2L, true, RoleType.EMPLOYEE)));

        assertThrows(UnauthorizedApproverException.class, () -> leaveRequestService.approve(1L, approvalRequest(null)));
    }

    @Test
    void requesterCannotApproveOwnRequest() {
        LeaveRequest leaveRequest = decisionRequest(LeaveStatus.PENDING);
        User requester = user(1L, true, RoleType.MANAGER);
        leaveRequest.setRequester(requester);
        leaveRequest.setApprover(requester);
        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(leaveRequest));
        when(userRepository.findById(1L)).thenReturn(Optional.of(requester));

        assertThrows(UnauthorizedApproverException.class, () -> leaveRequestService.approve(1L, approvalRequest(1L, null)));
    }

    @Test
    void alreadyApprovedRequestReturnsConflict() {
        assertDecisionConflict(LeaveStatus.APPROVED);
    }

    @Test
    void alreadyRejectedRequestReturnsConflict() {
        assertDecisionConflict(LeaveStatus.REJECTED);
    }

    @Test
    void cancelledRequestReturnsConflict() {
        assertDecisionConflict(LeaveStatus.CANCELLED);
    }

    @Test
    void approvalUpdatesLeaveBalanceUsage() {
        LeaveBalance balance = balance(BigDecimal.valueOf(10), BigDecimal.valueOf(2), BigDecimal.valueOf(8));
        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(decisionRequest(LeaveStatus.PENDING)));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user(2L, true, RoleType.MANAGER)));
        when(leaveBalanceRepository.findByUserIdAndLeaveTypeIdAndYear(1L, 3L, LocalDate.now().plusDays(1).getYear()))
                .thenReturn(Optional.of(balance));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        leaveRequestService.approve(1L, approvalRequest(null));

        assertEquals(BigDecimal.valueOf(4), balance.getUsedDays());
        assertEquals(BigDecimal.valueOf(6), balance.getRemainingDays());
        verify(leaveBalanceRepository).save(balance);
    }

    @Test
    void approvalFailsWhenBalanceIsMissing() {
        LeaveRequest leaveRequest = decisionRequest(LeaveStatus.PENDING);
        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(leaveRequest));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user(2L, true, RoleType.MANAGER)));
        when(leaveBalanceRepository.findByUserIdAndLeaveTypeIdAndYear(1L, 3L, leaveRequest.getStartDate().getYear()))
                .thenReturn(Optional.empty());

        assertThrows(LeaveBalanceNotFoundException.class, () -> leaveRequestService.approve(1L, approvalRequest(null)));
        assertEquals(LeaveStatus.PENDING, leaveRequest.getStatus());
        verify(leaveRequestRepository, never()).save(any(LeaveRequest.class));
    }

    @Test
    void approvalFailsWhenRemainingBalanceIsInsufficient() {
        LeaveRequest leaveRequest = decisionRequest(LeaveStatus.PENDING);
        LeaveBalance balance = balance(BigDecimal.valueOf(10), BigDecimal.valueOf(9), BigDecimal.ONE);
        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(leaveRequest));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user(2L, true, RoleType.MANAGER)));
        when(leaveBalanceRepository.findByUserIdAndLeaveTypeIdAndYear(1L, 3L, leaveRequest.getStartDate().getYear()))
                .thenReturn(Optional.of(balance));

        assertThrows(InsufficientLeaveBalanceException.class, () -> leaveRequestService.approve(1L, approvalRequest(null)));
        assertEquals(LeaveStatus.PENDING, leaveRequest.getStatus());
        assertEquals(BigDecimal.valueOf(9), balance.getUsedDays());
        assertEquals(BigDecimal.ONE, balance.getRemainingDays());
    }

    @Test
    void rejectionDoesNotChangeBalance() {
        rejectDecisionRequest("No");

        verify(leaveBalanceRepository, never()).save(any(LeaveBalance.class));
    }

    @Test
    void requestCreationDoesNotChangeBalance() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L, true)));
        when(leaveTypeRepository.findById(3L)).thenReturn(Optional.of(leaveType(3L)));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        leaveRequestService.create(createRequest());

        verify(leaveBalanceRepository, never()).save(any(LeaveBalance.class));
    }

    @Test
    void requestUpdateDoesNotChangeBalance() {
        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(leaveRequest(1L, LeaveStatus.PENDING)));
        when(leaveTypeRepository.findById(3L)).thenReturn(Optional.of(leaveType(3L)));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        leaveRequestService.update(1L, updateRequest());

        verify(leaveBalanceRepository, never()).save(any(LeaveBalance.class));
    }

    @Test
    void approvalCreatesRequesterNotification() {
        approveDecisionRequest("Approved");

        verify(notificationService).create(any(NotificationCreateRequest.class));
    }

    @Test
    void automaticManagerNotificationCreatedWhenLeaveRequestIsSubmitted() {
        User manager = user(2L, true, RoleType.MANAGER);
        User requester = user(1L, true, RoleType.EMPLOYEE);
        requester.setFirstName("Jane");
        requester.setLastName("Doe");
        requester.setManager(manager);
        when(userRepository.findById(1L)).thenReturn(Optional.of(requester));
        when(leaveTypeRepository.findById(3L)).thenReturn(Optional.of(leaveType(3L)));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        leaveRequestService.create(createRequest());

        verify(notificationService).create(any(NotificationCreateRequest.class));
    }

    @Test
    void automaticApprovalNotificationCreatedForRequester() {
        approveDecisionRequest("Approved");

        verify(notificationService).create(any(NotificationCreateRequest.class));
    }

    @Test
    void automaticRejectionNotificationCreatedForRequester() {
        rejectDecisionRequest("Rejected");

        verify(notificationService).create(any(NotificationCreateRequest.class));
    }

    @Test
    void noNotificationWhenManagerMissing() {
        User requester = user(1L, true, RoleType.EMPLOYEE);
        requester.setManager(null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(requester));
        when(leaveTypeRepository.findById(3L)).thenReturn(Optional.of(leaveType(3L)));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        leaveRequestService.create(createRequest());

        verify(notificationService, never()).create(any(NotificationCreateRequest.class));
    }

    private LeaveRequestCreateRequest createRequest() {
        LeaveRequestCreateRequest request = new LeaveRequestCreateRequest();
        request.setRequesterId(1L);
        request.setLeaveTypeId(3L);
        request.setStartDate(LocalDate.now().plusDays(1));
        request.setEndDate(LocalDate.now().plusDays(2));
        request.setReason(" Family event ");
        return request;
    }

    private LeaveRequestUpdateRequest updateRequest() {
        LeaveRequestUpdateRequest request = new LeaveRequestUpdateRequest();
        request.setLeaveTypeId(3L);
        request.setStartDate(LocalDate.now().plusDays(3));
        request.setEndDate(LocalDate.now().plusDays(4));
        request.setReason("Updated reason");
        return request;
    }

    private LeaveApprovalRequest approvalRequest(String comment) {
        return approvalRequest(2L, comment);
    }

    private LeaveApprovalRequest approvalRequest(Long approverId, String comment) {
        LeaveApprovalRequest request = new LeaveApprovalRequest();
        request.setApproverId(approverId);
        request.setComment(comment);
        return request;
    }

    private LeaveRejectionRequest rejectionRequest(String comment) {
        LeaveRejectionRequest request = new LeaveRejectionRequest();
        request.setApproverId(2L);
        request.setComment(comment);
        return request;
    }

    private LeaveRequestResponse approveDecisionRequest(String comment) {
        LeaveRequest leaveRequest = decisionRequest(LeaveStatus.PENDING);
        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(leaveRequest));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user(2L, true, RoleType.MANAGER)));
        when(leaveBalanceRepository.findByUserIdAndLeaveTypeIdAndYear(1L, 3L, leaveRequest.getStartDate().getYear()))
                .thenReturn(Optional.of(balance(BigDecimal.valueOf(10), BigDecimal.ZERO, BigDecimal.valueOf(10))));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        return leaveRequestService.approve(1L, approvalRequest(comment));
    }

    private LeaveRequestResponse rejectDecisionRequest(String comment) {
        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(decisionRequest(LeaveStatus.PENDING)));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user(2L, true, RoleType.MANAGER)));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        return leaveRequestService.reject(1L, rejectionRequest(comment));
    }

    private void assertDecisionConflict(LeaveStatus status) {
        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(decisionRequest(status)));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user(2L, true, RoleType.MANAGER)));

        assertThrows(LeaveDecisionNotAllowedException.class, () -> leaveRequestService.approve(1L, approvalRequest(null)));
    }

    private LeaveRequest decisionRequest(LeaveStatus status) {
        LeaveRequest leaveRequest = leaveRequest(1L, status);
        leaveRequest.setRequester(user(1L, true, RoleType.EMPLOYEE));
        leaveRequest.setApprover(user(2L, true, RoleType.MANAGER));
        return leaveRequest;
    }

    private LeaveBalance balance(BigDecimal totalDays, BigDecimal usedDays, BigDecimal remainingDays) {
        LeaveBalance balance = new LeaveBalance();
        balance.setId(1L);
        balance.setUser(user(1L, true));
        balance.setLeaveType(leaveType(3L));
        balance.setYear(LocalDate.now().plusDays(1).getYear());
        balance.setTotalDays(totalDays);
        balance.setUsedDays(usedDays);
        balance.setRemainingDays(remainingDays);
        balance.setCreatedAt(LocalDateTime.now());
        return balance;
    }

    private LeaveRequest leaveRequest(Long id, LeaveStatus status) {
        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setId(id);
        leaveRequest.setRequester(user(1L, true));
        leaveRequest.setLeaveType(leaveType(3L));
        leaveRequest.setStartDate(LocalDate.now().plusDays(1));
        leaveRequest.setEndDate(LocalDate.now().plusDays(2));
        leaveRequest.setRequestedDays(BigDecimal.valueOf(2));
        leaveRequest.setReason("Existing reason");
        leaveRequest.setStatus(status);
        leaveRequest.setSubmittedAt(LocalDateTime.now());
        leaveRequest.setCreatedAt(LocalDateTime.now());
        return leaveRequest;
    }

    private User user(Long id, boolean enabled) {
        return user(id, enabled, RoleType.EMPLOYEE);
    }

    private User user(Long id, boolean enabled, RoleType role) {
        User user = new User();
        user.setId(id);
        user.setUsername("user" + id);
        user.setEmail("user" + id + "@example.com");
        user.setPasswordHash("hash");
        user.setFirstName("User");
        user.setLastName(String.valueOf(id));
        user.setRole(role);
        user.setStatus(UserStatus.ACTIVE);
        user.setEnabled(enabled);
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
