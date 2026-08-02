package com.xtensus.hrmanagementapi.raison.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.xtensus.hrmanagementapi.common.exception.GlobalExceptionHandler;
import com.xtensus.hrmanagementapi.raison.dto.RaisonRequest;
import com.xtensus.hrmanagementapi.raison.dto.RaisonResponse;
import com.xtensus.hrmanagementapi.raison.exception.RaisonIntrouvableException;
import com.xtensus.hrmanagementapi.raison.service.RaisonService;
import com.xtensus.hrmanagementapi.security.jwt.JwtService;
import com.xtensus.hrmanagementapi.security.user.CustomUserDetailsService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(controllers = {RaisonController.class, GlobalExceptionHandler.class})
class RaisonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private RaisonService service;

    @Test
    void listerRaisons() throws Exception {
        when(service.lister()).thenReturn(List.of(response()));

        mockMvc.perform(get("/api/raisons"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].commentaire").value("Refus manque de solde"));
    }

    @Test
    void creerRaison() throws Exception {
        when(service.creer(any(RaisonRequest.class))).thenReturn(response());

        mockMvc.perform(post("/api/raisons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "commentaire": "Refus manque de solde"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.commentaire").value("Refus manque de solde"));
    }

    @Test
    void introuvableRetourne404() throws Exception {
        when(service.trouverParId(99L)).thenThrow(new RaisonIntrouvableException(99L));

        mockMvc.perform(get("/api/raisons/99"))
                .andExpect(status().isNotFound());
    }

    private RaisonResponse response() {
        RaisonResponse response = new RaisonResponse();
        response.setId(1L);
        response.setCommentaire("Refus manque de solde");
        response.setDateCreation(LocalDateTime.now());
        return response;
    }
}
