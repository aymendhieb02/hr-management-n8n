package com.xtensus.hrmanagementapi.position.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PositionRequest {

    @NotBlank
    private String title;

    private String description;
}
