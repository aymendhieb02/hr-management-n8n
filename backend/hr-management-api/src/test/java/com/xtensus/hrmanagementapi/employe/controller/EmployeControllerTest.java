package com.xtensus.hrmanagementapi.employe.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xtensus.hrmanagementapi.security.user.CustomUserDetailsService;
import com.xtensus.hrmanagementapi.common.exception.GlobalExceptionHandler;
import com.xtensus.hrmanagementapi.employe.dto.EmployeRequest;
import com.xtensus.hrmanagementapi.employe.dto.EmployeResponse;
import com.xtensus.hrmanagementapi.employe.exception.EmployeExisteDejaException;
import com.xtensus.hrmanagementapi.employe.service.EmployeService;
import com.xtensus.hrmanagementapi.security.jwt.JwtService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = {EmployeController.class, GlobalExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
class EmployeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private EmployeService employeService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;
    @Test
    void findAllReturnsEmployes() throws Exception {
        EmployeResponse response = response(1L, "Dupont", "Amine", "amine@example.com");
        given(employeService.findAll()).willReturn(List.of(response));

        mockMvc.perform(get("/api/employes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nom").value("Dupont"))
                .andExpect(jsonPath("$[0].prenom").value("Amine"));
    }

    @Test
    void createReturnsCreatedEmploye() throws Exception {
        EmployeResponse response = response(1L, "Dupont", "Amine", "amine@example.com");
        given(employeService.create(any(EmployeRequest.class))).willReturn(response);

        mockMvc.perform(post("/api/employes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request("Dupont", "Amine", "amine@example.com"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("amine@example.com"));
    }

    @Test
    void blankNomReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/employes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request(" ", "Amine", "amine@example.com"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.nom").exists());
    }

    @Test
    void duplicateEmailReturnsConflict() throws Exception {
        doThrow(new EmployeExisteDejaException("amine@example.com"))
                .when(employeService).create(any(EmployeRequest.class));

        mockMvc.perform(post("/api/employes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request("Dupont", "Amine", "amine@example.com"))))
                .andExpect(status().isConflict());
    }

    private EmployeRequest request(String nom, String prenom, String email) {
        EmployeRequest request = new EmployeRequest();
        request.setUsername("amine.dupont");
        request.setNom(nom);
        request.setPrenom(prenom);
        request.setEmail(email);
        request.setActif(true);
        return request;
    }

    private EmployeResponse response(Long id, String nom, String prenom, String email) {
        EmployeResponse response = new EmployeResponse();
        response.setId(id);
        response.setNom(nom);
        response.setPrenom(prenom);
        response.setEmail(email);
        response.setActif(true);
        return response;
    }
}



