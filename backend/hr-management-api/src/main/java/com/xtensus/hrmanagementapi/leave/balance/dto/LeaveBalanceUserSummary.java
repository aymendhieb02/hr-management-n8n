package com.xtensus.hrmanagementapi.leave.balance.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LeaveBalanceUserSummary {

    private Long id;

    private String firstName;

    private String lastName;

    private String email;
}
