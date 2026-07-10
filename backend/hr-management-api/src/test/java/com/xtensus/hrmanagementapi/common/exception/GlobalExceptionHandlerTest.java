package com.xtensus.hrmanagementapi.common.exception;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.xtensus.hrmanagementapi.department.controller.DepartmentController;
import com.xtensus.hrmanagementapi.department.dto.DepartmentRequest;
import com.xtensus.hrmanagementapi.department.exception.DepartmentNotFoundException;
import com.xtensus.hrmanagementapi.department.exception.DuplicateDepartmentException;
import com.xtensus.hrmanagementapi.department.service.DepartmentService;
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
class GlobalExceptionHandlerTest {

    @Mock
    private DepartmentService departmentService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(new DepartmentController(departmentService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void blankDepartmentNameReturnsValidationError() throws Exception {
        mockMvc.perform(post("/api/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": " ",
                                  "description": "People operations"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors.name").exists());
    }

    @Test
    void missingDepartmentReturnsNotFound() throws Exception {
        when(departmentService.findById(99L))
                .thenThrow(new DepartmentNotFoundException(99L));

        mockMvc.perform(get("/api/departments/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Department not found with id: 99"));
    }

    @Test
    void duplicateDepartmentReturnsConflict() throws Exception {
        when(departmentService.create(any(DepartmentRequest.class)))
                .thenThrow(new DuplicateDepartmentException("HR"));

        mockMvc.perform(post("/api/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "HR",
                                  "description": "Human Resources"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Department already exists with name: HR"));
    }

    @Test
    void malformedJsonReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Malformed or invalid request body"));
    }

    @Test
    void unexpectedExceptionDoesNotExposeStackTrace() throws Exception {
        when(departmentService.findAll())
                .thenThrow(new RuntimeException("database password leaked"));

        mockMvc.perform(get("/api/departments"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"))
                .andExpect(jsonPath("$.message").value(not(containsString("RuntimeException"))))
                .andExpect(jsonPath("$.message").value(not(containsString("database password leaked"))))
                .andExpect(jsonPath("$.stackTrace").doesNotExist());
    }
}
