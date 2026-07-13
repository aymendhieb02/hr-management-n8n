package com.xtensus.hrmanagementapi.leave.request.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LeaveApprovalRequest {

    @NotNull
    private Long approverId;

    @Size(max = 1000)
    private String comment;
}
