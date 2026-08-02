package com.xtensus.hrmanagementapi.conge.statut.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.xtensus.hrmanagementapi.common.exception.GlobalExceptionHandler;
import com.xtensus.hrmanagementapi.conge.statut.dto.CongeDemandeStatutRequest;
import com.xtensus.hrmanagementapi.conge.statut.dto.CongeDemandeStatutResponse;
import com.xtensus.hrmanagementapi.conge.statut.exception.CongeDemandeStatutExisteDejaException;
import com.xtensus.hrmanagementapi.conge.statut.service.CongeDemandeStatutService;
import com.xtensus.hrmanagementapi.security.jwt.JwtService;
import com.xtensus.hrmanagementapi.security.user.CustomUserDetailsService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(controllers = {CongeDemandeStatutController.class, GlobalExceptionHandler.class})
class CongeDemandeStatutControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private CongeDemandeStatutService service;

    @Test
    void listerStatuts() throws Exception {
        when(service.lister()).thenReturn(List.of(response()));

        mockMvc.perform(get("/api/conge-demande-statuts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].libelle").value("EN_ATTENTE"));
    }

    @Test
    void creerStatut() throws Exception {
        when(service.creer(any(CongeDemandeStatutRequest.class))).thenReturn(response());

        mockMvc.perform(post("/api/conge-demande-statuts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "libelle": "EN_ATTENTE",
                                  "description": "Demande en attente",
                                  "actif": true
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.libelle").value("EN_ATTENTE"));
    }

    @Test
    void doublonRetourne409() throws Exception {
        when(service.creer(any(CongeDemandeStatutRequest.class)))
                .thenThrow(new CongeDemandeStatutExisteDejaException("EN_ATTENTE"));

        mockMvc.perform(post("/api/conge-demande-statuts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "libelle": "EN_ATTENTE",
                                  "actif": true
                                }
                                """))
                .andExpect(status().isConflict());
    }

    private CongeDemandeStatutResponse response() {
        CongeDemandeStatutResponse response = new CongeDemandeStatutResponse();
        response.setId(1L);
        response.setLibelle("EN_ATTENTE");
        response.setDescription("Demande en attente");
        response.setActif(true);
        return response;
    }
}
