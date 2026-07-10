package com.xtensus.hrmanagementapi.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PasswordUpdateRequest {

    @NotBlank
    @Size(min = 8, max = 100)
    private String newPassword;
}
