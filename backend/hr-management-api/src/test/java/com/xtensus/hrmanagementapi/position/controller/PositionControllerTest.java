package com.xtensus.hrmanagementapi.position.controller;

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
import com.xtensus.hrmanagementapi.position.dto.PositionRequest;
import com.xtensus.hrmanagementapi.position.dto.PositionResponse;
import com.xtensus.hrmanagementapi.position.exception.DuplicatePositionException;
import com.xtensus.hrmanagementapi.position.exception.PositionNotFoundException;
import com.xtensus.hrmanagementapi.position.service.PositionService;
import java.time.LocalDateTime;
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
class PositionControllerTest {

    @Mock
    private PositionService positionService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(new PositionController(positionService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void createPositionSuccessfully() throws Exception {
        when(positionService.create(any(PositionRequest.class)))
                .thenReturn(response(1L, "Developer", "Builds software"));

        mockMvc.perform(post("/api/positions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Developer",
                                  "description": "Builds software"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Developer"))
                .andExpect(jsonPath("$.description").value("Builds software"));
    }

    @Test
    void blankTitleReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/positions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": " ",
                                  "description": "Builds software"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.title").exists());
    }

    @Test
    void duplicateTitleReturnsConflict() throws Exception {
        when(positionService.create(any(PositionRequest.class)))
                .thenThrow(new DuplicatePositionException("Developer"));

        mockMvc.perform(post("/api/positions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Developer",
                                  "description": "Builds software"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Position already exists with title: Developer"));
    }

    @Test
    void getMissingPositionReturnsNotFound() throws Exception {
        when(positionService.findById(99L))
                .thenThrow(new PositionNotFoundException(99L));

        mockMvc.perform(get("/api/positions/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Position not found with id: 99"));
    }

    @Test
    void updatePositionSuccessfully() throws Exception {
        when(positionService.update(any(Long.class), any(PositionRequest.class)))
                .thenReturn(response(1L, "Senior Developer", "Builds and reviews software"));

        mockMvc.perform(put("/api/positions/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Senior Developer",
                                  "description": "Builds and reviews software"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Senior Developer"))
                .andExpect(jsonPath("$.description").value("Builds and reviews software"));
    }

    @Test
    void deletePositionSuccessfully() throws Exception {
        doNothing().when(positionService).delete(1L);

        mockMvc.perform(delete("/api/positions/1"))
                .andExpect(status().isNoContent());
    }

    private PositionResponse response(Long id, String title, String description) {
        PositionResponse response = new PositionResponse();
        response.setId(id);
        response.setTitle(title);
        response.setDescription(description);
        response.setCreatedAt(LocalDateTime.now());
        response.setUpdatedAt(LocalDateTime.now());
        return response;
    }
}
