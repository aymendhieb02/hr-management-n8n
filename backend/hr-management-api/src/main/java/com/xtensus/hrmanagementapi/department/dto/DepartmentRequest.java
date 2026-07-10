package com.xtensus.hrmanagementapi.department.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DepartmentRequest {

    @NotBlank
    private String name;

    private String description;
}
