package com.xtensus.hrmanagementapi.poste.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.xtensus.hrmanagementapi.common.exception.GlobalExceptionHandler;
import com.xtensus.hrmanagementapi.poste.dto.PosteRequest;
import com.xtensus.hrmanagementapi.poste.dto.PosteResponse;
import com.xtensus.hrmanagementapi.poste.exception.PosteExisteDejaException;
import com.xtensus.hrmanagementapi.poste.service.PosteService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.xtensus.hrmanagementapi.security.jwt.JwtService;
import com.xtensus.hrmanagementapi.security.user.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(controllers = {PosteController.class, GlobalExceptionHandler.class})
class PosteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private PosteService posteService;

    @Test
    void listerPostes() throws Exception {
        when(posteService.lister()).thenReturn(List.of(response()));

        mockMvc.perform(get("/api/postes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].intitule").value("Responsable RH"));
    }

    @Test
    void creerPoste() throws Exception {
        when(posteService.creer(any(PosteRequest.class))).thenReturn(response());

        mockMvc.perform(post("/api/postes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "intitule": "Responsable RH",
                                  "description": "Gestion RH",
                                  "niveauPoste": "Manager",
                                  "actif": true
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.intitule").value("Responsable RH"));
    }

    @Test
    void intituleObligatoireRetourne400() throws Exception {
        mockMvc.perform(post("/api/postes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "intitule": " "
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.intitule").exists());
    }

    @Test
    void doublonRetourne409() throws Exception {
        when(posteService.creer(any(PosteRequest.class))).thenThrow(new PosteExisteDejaException("Responsable RH"));

        mockMvc.perform(post("/api/postes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "intitule": "Responsable RH"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Un poste existe deja avec l'intitule 'Responsable RH'"));
    }

    private PosteResponse response() {
        PosteResponse response = new PosteResponse();
        response.setId(1L);
        response.setIntitule("Responsable RH");
        response.setDescription("Gestion RH");
        response.setNiveauPoste("Manager");
        response.setActif(true);
        response.setDateCreation(LocalDateTime.now());
        return response;
    }
}


