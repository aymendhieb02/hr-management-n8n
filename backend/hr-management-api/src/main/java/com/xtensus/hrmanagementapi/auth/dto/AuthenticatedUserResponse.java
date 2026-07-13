package com.xtensus.hrmanagementapi.auth.dto;

import com.xtensus.hrmanagementapi.domain.enums.RoleType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AuthenticatedUserResponse {

    private Long id;

    private String username;

    private String email;

    private String firstName;

    private String lastName;

    private RoleType role;
}
