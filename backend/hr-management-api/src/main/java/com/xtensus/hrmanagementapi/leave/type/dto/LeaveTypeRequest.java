package com.xtensus.hrmanagementapi.leave.type.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LeaveTypeRequest {

    @NotBlank
    @Size(max = 100)
    private String name;

    @Size(max = 255)
    private String description;

    @Positive
    private Integer maxDays;

    @NotNull
    private Boolean requiresMedicalCertificate;

    @NotNull
    private Boolean active;
}
