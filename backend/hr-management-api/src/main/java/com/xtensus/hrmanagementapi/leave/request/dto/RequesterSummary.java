package com.xtensus.hrmanagementapi.leave.request.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RequesterSummary {

    private Long id;

    private String firstName;

    private String lastName;

    private String email;
}
