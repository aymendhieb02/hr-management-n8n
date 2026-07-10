package com.xtensus.hrmanagementapi.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.xtensus.hrmanagementapi.common.exception.GlobalExceptionHandler;
import com.xtensus.hrmanagementapi.domain.enums.RoleType;
import com.xtensus.hrmanagementapi.domain.enums.UserStatus;
import com.xtensus.hrmanagementapi.user.dto.UserCreateRequest;
import com.xtensus.hrmanagementapi.user.dto.UserResponse;
import com.xtensus.hrmanagementapi.user.exception.DuplicateEmailException;
import com.xtensus.hrmanagementapi.user.exception.DuplicateUsernameException;
import com.xtensus.hrmanagementapi.user.exception.UserNotFoundException;
import com.xtensus.hrmanagementapi.user.service.UserService;
import java.time.LocalDate;
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
class UserControllerTest {

    @Mock
    private UserService userService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(new UserController(userService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void createUserSuccessfully() throws Exception {
        when(userService.create(any(UserCreateRequest.class)))
                .thenReturn(response(1L, "jdoe", "jdoe@example.com", RoleType.EMPLOYEE));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("jdoe"))
                .andExpect(jsonPath("$.email").value("jdoe@example.com"));
    }

    @Test
    void blankUsernameReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateJson().replace("\"jdoe\"", "\" \"")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.username").exists());
    }

    @Test
    void invalidEmailReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateJson().replace("jdoe@example.com", "not-an-email")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.email").exists());
    }

    @Test
    void shortPasswordReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateJson().replace("StrongPass123", "short")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.password").exists());
    }

    @Test
    void duplicateUsernameReturnsConflict() throws Exception {
        when(userService.create(any(UserCreateRequest.class)))
                .thenThrow(new DuplicateUsernameException("jdoe"));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateJson()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("User already exists with username: jdoe"));
    }

    @Test
    void duplicateEmailReturnsConflict() throws Exception {
        when(userService.create(any(UserCreateRequest.class)))
                .thenThrow(new DuplicateEmailException("jdoe@example.com"));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateJson()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("User already exists with email: jdoe@example.com"));
    }

    @Test
    void responseNeverContainsPasswordHash() throws Exception {
        when(userService.create(any(UserCreateRequest.class)))
                .thenReturn(response(1L, "jdoe", "jdoe@example.com", RoleType.EMPLOYEE));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void getUsersByRoleSuccessfully() throws Exception {
        when(userService.findByRole(RoleType.MANAGER))
                .thenReturn(List.of(response(2L, "manager", "manager@example.com", RoleType.MANAGER)));

        mockMvc.perform(get("/api/users/role/MANAGER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].role").value("MANAGER"));
    }

    @Test
    void getTeamMembersSuccessfully() throws Exception {
        when(userService.findTeamMembers(2L))
                .thenReturn(List.of(response(1L, "jdoe", "jdoe@example.com", RoleType.EMPLOYEE)));

        mockMvc.perform(get("/api/users/2/team"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("jdoe"));
    }

    @Test
    void missingUserReturnsNotFound() throws Exception {
        when(userService.findById(99L))
                .thenThrow(new UserNotFoundException(99L));

        mockMvc.perform(get("/api/users/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found with id: 99"));
    }

    @Test
    void deleteUserSuccessfullyWhenNotReferenced() throws Exception {
        doNothing().when(userService).delete(1L);

        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());
    }

    private String validCreateJson() {
        return """
                {
                  "username": "jdoe",
                  "email": "jdoe@example.com",
                  "password": "StrongPass123",
                  "firstName": "John",
                  "lastName": "Doe",
                  "phone": "123456789",
                  "hireDate": "2026-01-15",
                  "role": "EMPLOYEE",
                  "status": "ACTIVE",
                  "enabled": true,
                  "managerId": null,
                  "departmentId": null,
                  "positionId": null
                }
                """;
    }

    private UserResponse response(Long id, String username, String email, RoleType role) {
        UserResponse response = new UserResponse();
        response.setId(id);
        response.setUsername(username);
        response.setEmail(email);
        response.setFirstName("John");
        response.setLastName("Doe");
        response.setPhone("123456789");
        response.setHireDate(LocalDate.of(2026, 1, 15));
        response.setRole(role);
        response.setStatus(UserStatus.ACTIVE);
        response.setEnabled(true);
        response.setCreatedAt(LocalDateTime.now());
        response.setUpdatedAt(LocalDateTime.now());
        return response;
    }
}
