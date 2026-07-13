package com.xtensus.hrmanagementapi.leave.request.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.xtensus.hrmanagementapi.common.exception.GlobalExceptionHandler;
import com.xtensus.hrmanagementapi.domain.enums.LeaveStatus;
import com.xtensus.hrmanagementapi.leave.request.dto.LeaveApprovalRequest;
import com.xtensus.hrmanagementapi.leave.request.dto.LeaveRequestResponse;
import com.xtensus.hrmanagementapi.leave.request.dto.LeaveRejectionRequest;
import com.xtensus.hrmanagementapi.leave.request.service.LeaveRequestService;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@ExtendWith(MockitoExtension.class)
class LeaveRequestControllerTest {

    @Mock
    private LeaveRequestService leaveRequestService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(new LeaveRequestController(leaveRequestService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void approveReturnsOk() throws Exception {
        when(leaveRequestService.approve(eq(1L), any(LeaveApprovalRequest.class)))
                .thenReturn(response(LeaveStatus.APPROVED, "Approved"));

        mockMvc.perform(patch("/api/leave-requests/1/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "approverId": 2,
                                  "comment": "Approved"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.decisionComment").value("Approved"));
    }

    @Test
    void rejectReturnsOk() throws Exception {
        when(leaveRequestService.reject(eq(1L), any(LeaveRejectionRequest.class)))
                .thenReturn(response(LeaveStatus.REJECTED, "Insufficient coverage"));

        mockMvc.perform(patch("/api/leave-requests/1/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "approverId": 2,
                                  "comment": "Insufficient coverage"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"))
                .andExpect(jsonPath("$.decisionComment").value("Insufficient coverage"));
    }

    @Test
    void rejectionRequiresNonBlankComment() throws Exception {
        mockMvc.perform(patch("/api/leave-requests/1/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "approverId": 2,
                                  "comment": " "
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.comment").exists());
    }

    @Test
    void approvalRequiresApproverId() throws Exception {
        mockMvc.perform(patch("/api/leave-requests/1/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "comment": "Approved"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.approverId").exists());
    }

    private LeaveRequestResponse response(LeaveStatus status, String decisionComment) {
        LeaveRequestResponse response = new LeaveRequestResponse();
        response.setId(1L);
        response.setStatus(status);
        response.setDecisionComment(decisionComment);
        response.setDecisionAt(LocalDateTime.now());
        return response;
    }
}
