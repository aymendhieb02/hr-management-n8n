package com.xtensus.hrmanagementapi.auth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Getter
@Setter
@NoArgsConstructor
public class LoginResponse {

    @JsonIgnore
    private String accessToken;

    private long expiresIn;

    private AuthenticatedUserResponse user;
}
