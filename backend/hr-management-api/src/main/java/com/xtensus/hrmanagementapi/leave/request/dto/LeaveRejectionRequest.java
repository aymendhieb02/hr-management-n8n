package com.xtensus.hrmanagementapi.leave.request.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LeaveRejectionRequest {

    @NotNull
    private Long approverId;

    @NotBlank
    @Size(max = 1000)
    private String comment;
}
