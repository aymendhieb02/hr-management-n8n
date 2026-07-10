package com.xtensus.hrmanagementapi.leave.type.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.xtensus.hrmanagementapi.common.exception.GlobalExceptionHandler;
import com.xtensus.hrmanagementapi.leave.type.dto.LeaveTypeRequest;
import com.xtensus.hrmanagementapi.leave.type.dto.LeaveTypeResponse;
import com.xtensus.hrmanagementapi.leave.type.exception.DuplicateLeaveTypeException;
import com.xtensus.hrmanagementapi.leave.type.exception.LeaveTypeNotFoundException;
import com.xtensus.hrmanagementapi.leave.type.service.LeaveTypeService;
import java.time.LocalDateTime;
import java.util.List;
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
class LeaveTypeControllerTest {

    @Mock
    private LeaveTypeService leaveTypeService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(new LeaveTypeController(leaveTypeService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void createLeaveTypeSuccessfully() throws Exception {
        when(leaveTypeService.create(any(LeaveTypeRequest.class)))
                .thenReturn(response(1L, "Annual Leave", 30, false, true));

        mockMvc.perform(post("/api/leave-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Annual Leave",
                                  "description": "Paid annual leave",
                                  "maxDays": 30,
                                  "requiresMedicalCertificate": false,
                                  "active": true
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Annual Leave"))
                .andExpect(jsonPath("$.maxDays").value(30));
    }

    @Test
    void createLeaveTypeWithNullMaxDaysSuccessfully() throws Exception {
        when(leaveTypeService.create(any(LeaveTypeRequest.class)))
                .thenReturn(response(2L, "Unpaid Leave", null, false, true));

        mockMvc.perform(post("/api/leave-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Unpaid Leave",
                                  "description": "Leave without pay",
                                  "maxDays": null,
                                  "requiresMedicalCertificate": false,
                                  "active": true
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.name").value("Unpaid Leave"))
                .andExpect(jsonPath("$.maxDays").doesNotExist());
    }

    @Test
    void blankNameReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/leave-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": " ",
                                  "description": "Paid annual leave",
                                  "maxDays": 30,
                                  "requiresMedicalCertificate": false,
                                  "active": true
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.name").exists());
    }

    @Test
    void maxDaysEqualToZeroReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/leave-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Annual Leave",
                                  "description": "Paid annual leave",
                                  "maxDays": 0,
                                  "requiresMedicalCertificate": false,
                                  "active": true
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.maxDays").exists());
    }

    @Test
    void duplicateNameReturnsConflict() throws Exception {
        when(leaveTypeService.create(any(LeaveTypeRequest.class)))
                .thenThrow(new DuplicateLeaveTypeException("Annual Leave"));

        mockMvc.perform(post("/api/leave-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Annual Leave",
                                  "description": "Paid annual leave",
                                  "maxDays": 30,
                                  "requiresMedicalCertificate": false,
                                  "active": true
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Leave type already exists with name: Annual Leave"));
    }

    @Test
    void missingLeaveTypeReturnsNotFound() throws Exception {
        when(leaveTypeService.findById(99L))
                .thenThrow(new LeaveTypeNotFoundException(99L));

        mockMvc.perform(get("/api/leave-types/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Leave type not found with id: 99"));
    }

    @Test
    void updateLeaveTypeSuccessfully() throws Exception {
        when(leaveTypeService.update(any(Long.class), any(LeaveTypeRequest.class)))
                .thenReturn(response(1L, "Sick Leave", 15, true, true));

        mockMvc.perform(put("/api/leave-types/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Sick Leave",
                                  "description": "Medical leave",
                                  "maxDays": 15,
                                  "requiresMedicalCertificate": true,
                                  "active": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Sick Leave"))
                .andExpect(jsonPath("$.requiresMedicalCertificate").value(true));
    }

    @Test
    void getActiveLeaveTypesSuccessfully() throws Exception {
        when(leaveTypeService.findActive())
                .thenReturn(List.of(
                        response(1L, "Annual Leave", 30, false, true),
                        response(2L, "Sick Leave", 15, true, true)
                ));

        mockMvc.perform(get("/api/leave-types/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Annual Leave"))
                .andExpect(jsonPath("$[1].name").value("Sick Leave"));
    }

    @Test
    void deleteLeaveTypeSuccessfully() throws Exception {
        doNothing().when(leaveTypeService).delete(1L);

        mockMvc.perform(delete("/api/leave-types/1"))
                .andExpect(status().isNoContent());
    }

    private LeaveTypeResponse response(
            Long id,
            String name,
            Integer maxDays,
            Boolean requiresMedicalCertificate,
            Boolean active
    ) {
        LeaveTypeResponse response = new LeaveTypeResponse();
        response.setId(id);
        response.setName(name);
        response.setDescription(name + " description");
        response.setMaxDays(maxDays);
        response.setRequiresMedicalCertificate(requiresMedicalCertificate);
        response.setActive(active);
        response.setCreatedAt(LocalDateTime.now());
        response.setUpdatedAt(LocalDateTime.now());
        return response;
    }
}
