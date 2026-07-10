package com.xtensus.hrmanagementapi.leave.type.dto;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LeaveTypeResponse {

    private Long id;

    private String name;

    private String description;

    private Integer maxDays;

    private Boolean requiresMedicalCertificate;

    private Boolean active;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
