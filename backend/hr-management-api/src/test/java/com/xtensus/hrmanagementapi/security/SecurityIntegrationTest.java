package com.xtensus.hrmanagementapi.security;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.xtensus.hrmanagementapi.auth.dto.AuthenticatedUserResponse;
import com.xtensus.hrmanagementapi.auth.dto.LoginRequest;
import com.xtensus.hrmanagementapi.auth.dto.LoginResponse;
import com.xtensus.hrmanagementapi.auth.controller.AuthController;
import com.xtensus.hrmanagementapi.auth.service.AuthenticationService;
import com.xtensus.hrmanagementapi.department.dto.DepartmentRequest;
import com.xtensus.hrmanagementapi.department.dto.DepartmentResponse;
import com.xtensus.hrmanagementapi.department.service.DepartmentService;
import com.xtensus.hrmanagementapi.domain.entity.User;
import com.xtensus.hrmanagementapi.domain.enums.LeaveStatus;
import com.xtensus.hrmanagementapi.domain.enums.RoleType;
import com.xtensus.hrmanagementapi.domain.enums.UserStatus;
import com.xtensus.hrmanagementapi.leave.request.dto.LeaveApprovalRequest;
import com.xtensus.hrmanagementapi.leave.request.dto.LeaveRequestResponse;
import com.xtensus.hrmanagementapi.leave.request.service.LeaveRequestService;
import com.xtensus.hrmanagementapi.leave.accrual.dto.LeaveAccrualRunSummary;
import com.xtensus.hrmanagementapi.leave.accrual.service.LeaveAccrualService;
import com.xtensus.hrmanagementapi.leave.type.service.LeaveTypeService;
import com.xtensus.hrmanagementapi.medical.document.service.MedicalDocumentDownload;
import com.xtensus.hrmanagementapi.medical.document.service.MedicalDocumentService;
import com.xtensus.hrmanagementapi.position.service.PositionService;
import com.xtensus.hrmanagementapi.repository.UserRepository;
import com.xtensus.hrmanagementapi.security.jwt.JwtService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthController authController;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private DepartmentService departmentService;

    @MockitoBean
    private LeaveRequestService leaveRequestService;

    @MockitoBean
    private PositionService positionService;

    @MockitoBean
    private LeaveTypeService leaveTypeService;

    @MockitoBean
    private LeaveAccrualService leaveAccrualService;

    @MockitoBean
    private MedicalDocumentService medicalDocumentService;

    @MockitoBean
    private AuthenticationService authenticationService;

    @Test
    void loginRequestClassIsAvailableAtRuntime() throws Exception {
        Class<?> loginRequestClass = Class.forName("com.xtensus.hrmanagementapi.auth.dto.LoginRequest");

        assertSame(LoginRequest.class, loginRequestClass);
    }

    @Test
    void authControllerBeanLoads() {
        assertNotNull(authController);
    }

    @Test
    void allowedCorsPreflightForLoginSucceeds() throws Exception {
        mockMvc.perform(options("/api/auth/login")
                        .header("Origin", "http://localhost:4200")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "content-type,authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:4200"));
    }

    @Test
    void unauthorizedOriginDoesNotReceivePermissiveCorsHeader() throws Exception {
        mockMvc.perform(options("/api/auth/login")
                        .header("Origin", "http://evil.example")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "content-type,authorization"))
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }

    @Test
    void publicLoginEndpointWorksWithoutJwt() throws Exception {
        LoginResponse response = new LoginResponse();
        response.setAccessToken("token");
        response.setTokenType("Bearer");
        response.setExpiresIn(3600000);
        response.setUser(authenticatedUser(user(RoleType.EMPLOYEE)));
        when(authenticationService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "usernameOrEmail": "jdoe",
                                  "password": "StrongPass123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("token"))
                .andExpect(jsonPath("$.user.passwordHash").doesNotExist());
    }

    @Test
    void missingJwtReturnsUnauthorizedForProtectedEndpoint() throws Exception {
        mockMvc.perform(get("/api/departments"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authentication is required to access this resource."));
    }

    @Test
    void malformedJwtReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/departments")
                        .header("Authorization", "Bearer malformed.token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void validJwtAuthenticatesRequest() throws Exception {
        User employee = user(RoleType.EMPLOYEE);
        when(userRepository.findByUsernameIgnoreCase(employee.getUsername())).thenReturn(Optional.of(employee));

        mockMvc.perform(get("/api/departments")
                        .header("Authorization", bearer(employee)))
                .andExpect(status().isOk());
    }

    @Test
    void managerCanReadDepartmentsPositionsAndLeaveTypes() throws Exception {
        User manager = user(RoleType.MANAGER);
        when(userRepository.findByUsernameIgnoreCase(manager.getUsername())).thenReturn(Optional.of(manager));
        when(departmentService.findAll()).thenReturn(List.of());
        when(positionService.findAll()).thenReturn(List.of());
        when(leaveTypeService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/departments").header("Authorization", bearer(manager)))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/positions").header("Authorization", bearer(manager)))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/leave-types").header("Authorization", bearer(manager)))
                .andExpect(status().isOk());
    }

    @Test
    void employeeCannotCreateDepartment() throws Exception {
        User employee = user(RoleType.EMPLOYEE);
        when(userRepository.findByUsernameIgnoreCase(employee.getUsername())).thenReturn(Optional.of(employee));

        mockMvc.perform(post("/api/departments")
                        .header("Authorization", bearer(employee))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(departmentJson()))
                .andExpect(status().isForbidden());
    }

    @Test
    void hrCanCreateDepartment() throws Exception {
        User hr = user(RoleType.HR);
        when(userRepository.findByUsernameIgnoreCase(hr.getUsername())).thenReturn(Optional.of(hr));
        when(departmentService.create(any(DepartmentRequest.class))).thenReturn(departmentResponse());

        mockMvc.perform(post("/api/departments")
                        .header("Authorization", bearer(hr))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(departmentJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("People"));
    }

    @Test
    void adminCanCreateDepartment() throws Exception {
        User admin = user(RoleType.ADMIN);
        when(userRepository.findByUsernameIgnoreCase(admin.getUsername())).thenReturn(Optional.of(admin));
        when(departmentService.create(any(DepartmentRequest.class))).thenReturn(departmentResponse());

        mockMvc.perform(post("/api/departments")
                        .header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(departmentJson()))
                .andExpect(status().isCreated());
    }

    @Test
    void managerCanAccessApprovalEndpoint() throws Exception {
        User manager = user(RoleType.MANAGER);
        when(userRepository.findByUsernameIgnoreCase(manager.getUsername())).thenReturn(Optional.of(manager));
        when(leaveRequestService.approve(eq(1L), any(LeaveApprovalRequest.class))).thenReturn(leaveResponse());

        mockMvc.perform(patch("/api/leave-requests/1/approve")
                        .header("Authorization", bearer(manager))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "approverId": 1,
                                  "comment": "Approved"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void employeeCannotAccessApprovalEndpoint() throws Exception {
        User employee = user(RoleType.EMPLOYEE);
        when(userRepository.findByUsernameIgnoreCase(employee.getUsername())).thenReturn(Optional.of(employee));

        mockMvc.perform(patch("/api/leave-requests/1/approve")
                        .header("Authorization", bearer(employee))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "approverId": 1
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void managerCannotDownloadMedicalDocument() throws Exception {
        User manager = user(RoleType.MANAGER);
        when(userRepository.findByUsernameIgnoreCase(manager.getUsername())).thenReturn(Optional.of(manager));

        mockMvc.perform(get("/api/medical-documents/download/1")
                        .header("Authorization", bearer(manager)))
                .andExpect(status().isForbidden());
    }

    @Test
    void managerCannotUploadMedicalDocument() throws Exception {
        User manager = user(RoleType.MANAGER);
        when(userRepository.findByUsernameIgnoreCase(manager.getUsername())).thenReturn(Optional.of(manager));
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "certificate.pdf",
                "application/pdf",
                "pdf".getBytes()
        );

        mockMvc.perform(multipart("/api/medical-documents/upload/1")
                        .file(file)
                        .header("Authorization", bearer(manager)))
                .andExpect(status().isForbidden());
    }

    @Test
    void hrCanAccessMedicalDocumentDownload() throws Exception {
        User hr = user(RoleType.HR);
        when(userRepository.findByUsernameIgnoreCase(hr.getUsername())).thenReturn(Optional.of(hr));
        when(medicalDocumentService.download(1L)).thenReturn(new MedicalDocumentDownload(
                new ByteArrayResource("pdf".getBytes()),
                "certificate.pdf",
                "application/pdf",
                3L
        ));

        mockMvc.perform(get("/api/medical-documents/download/1")
                        .header("Authorization", bearer(hr)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"));
    }

    @Test
    void adminCanAccessMedicalDocumentDownload() throws Exception {
        User admin = user(RoleType.ADMIN);
        when(userRepository.findByUsernameIgnoreCase(admin.getUsername())).thenReturn(Optional.of(admin));
        when(medicalDocumentService.download(1L)).thenReturn(new MedicalDocumentDownload(
                new ByteArrayResource("pdf".getBytes()),
                "certificate.pdf",
                "application/pdf",
                3L
        ));

        mockMvc.perform(get("/api/medical-documents/download/1")
                        .header("Authorization", bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"));
    }

    @Test
    void hrCanTriggerManualLeaveAccrual() throws Exception {
        User hr = user(RoleType.HR);
        when(userRepository.findByUsernameIgnoreCase(hr.getUsername())).thenReturn(Optional.of(hr));
        when(leaveAccrualService.run(2026, 7)).thenReturn(accrualSummary());

        mockMvc.perform(post("/api/leave-accruals/run?year=2026&month=7")
                        .header("Authorization", bearer(hr)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.creditedUsers").value(1));
    }

    @Test
    void adminCanQueryLeaveAccruals() throws Exception {
        User admin = user(RoleType.ADMIN);
        when(userRepository.findByUsernameIgnoreCase(admin.getUsername())).thenReturn(Optional.of(admin));
        when(leaveAccrualService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/leave-accruals")
                        .header("Authorization", bearer(admin)))
                .andExpect(status().isOk());
    }

    @Test
    void employeeCannotTriggerManualLeaveAccrual() throws Exception {
        User employee = user(RoleType.EMPLOYEE);
        when(userRepository.findByUsernameIgnoreCase(employee.getUsername())).thenReturn(Optional.of(employee));

        mockMvc.perform(post("/api/leave-accruals/run")
                        .header("Authorization", bearer(employee)))
                .andExpect(status().isForbidden());
    }

    @Test
    void managerCannotTriggerManualLeaveAccrual() throws Exception {
        User manager = user(RoleType.MANAGER);
        when(userRepository.findByUsernameIgnoreCase(manager.getUsername())).thenReturn(Optional.of(manager));

        mockMvc.perform(post("/api/leave-accruals/run")
                        .header("Authorization", bearer(manager)))
                .andExpect(status().isForbidden());
    }

    @Test
    void authenticatedMeWorks() throws Exception {
        User employee = user(RoleType.EMPLOYEE);
        when(userRepository.findByUsernameIgnoreCase(employee.getUsername())).thenReturn(Optional.of(employee));
        when(authenticationService.me()).thenReturn(authenticatedUser(employee));

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", bearer(employee)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("jdoe"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    private String bearer(User user) {
        return "Bearer " + jwtService.generateToken(user);
    }

    private String departmentJson() {
        return """
                {
                  "name": "People",
                  "description": "People operations"
                }
                """;
    }

    private DepartmentResponse departmentResponse() {
        DepartmentResponse response = new DepartmentResponse();
        response.setId(1L);
        response.setName("People");
        response.setDescription("People operations");
        response.setCreatedAt(LocalDateTime.now());
        return response;
    }

    private LeaveRequestResponse leaveResponse() {
        LeaveRequestResponse response = new LeaveRequestResponse();
        response.setId(1L);
        response.setStatus(LeaveStatus.APPROVED);
        return response;
    }

    private LeaveAccrualRunSummary accrualSummary() {
        LeaveAccrualRunSummary summary = new LeaveAccrualRunSummary();
        summary.setYear(2026);
        summary.setMonth(7);
        summary.setEligibleUsers(1);
        summary.setCreditedUsers(1);
        summary.setSkippedUsers(0);
        summary.setFailedUsers(0);
        summary.setTotalCreditedDays(java.math.BigDecimal.valueOf(2.16));
        summary.setExecutedAt(LocalDateTime.now());
        return summary;
    }

    private AuthenticatedUserResponse authenticatedUser(User user) {
        AuthenticatedUserResponse response = new AuthenticatedUserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setRole(user.getRole());
        return response;
    }

    private User user(RoleType role) {
        User user = new User();
        user.setId(1L);
        user.setUsername("jdoe");
        user.setEmail("jdoe@example.com");
        user.setPasswordHash("hash");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setRole(role);
        user.setStatus(UserStatus.ACTIVE);
        user.setEnabled(true);
        return user;
    }
}
