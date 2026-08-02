package com.xtensus.hrmanagementapi.typecontrat.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.xtensus.hrmanagementapi.common.exception.GlobalExceptionHandler;
import com.xtensus.hrmanagementapi.typecontrat.dto.TypeContratResponse;
import com.xtensus.hrmanagementapi.typecontrat.service.TypeContratService;
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
@WebMvcTest(controllers = {TypeContratController.class, GlobalExceptionHandler.class})
class TypeContratControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private TypeContratService typeContratService;

    @Test
    void listerTypesContrat() throws Exception {
        TypeContratResponse response = new TypeContratResponse();
        response.setId(1L);
        response.setLibelle("CDI");
        response.setActif(true);
        when(typeContratService.lister()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/type-contrats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].libelle").value("CDI"));
    }
}


