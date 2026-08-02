package com.xtensus.hrmanagementapi.conge.type.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.xtensus.hrmanagementapi.common.exception.GlobalExceptionHandler;
import com.xtensus.hrmanagementapi.conge.type.dto.CongeTypeResponse;
import com.xtensus.hrmanagementapi.conge.type.service.CongeTypeService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.xtensus.hrmanagementapi.security.jwt.JwtService;
import com.xtensus.hrmanagementapi.security.user.CustomUserDetailsService;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(controllers = {CongeTypeController.class, GlobalExceptionHandler.class})
class CongeTypeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private CongeTypeService congeTypeService;

    @Test
    void listerTypesCongeActifs() throws Exception {
        CongeTypeResponse response = new CongeTypeResponse();
        response.setId(1L);
        response.setNom("Conge annuel");
        response.setJoursMaximum(new BigDecimal("30.00"));
        response.setCertificatObligatoire(false);
        response.setRemunere(true);
        response.setActif(true);
        when(congeTypeService.listerActifs()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/conge-types/actifs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nom").value("Conge annuel"))
                .andExpect(jsonPath("$[0].remunere").value(true));
    }
}


