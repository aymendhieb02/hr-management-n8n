package com.xtensus.hrmanagementapi.user.dto;

import com.xtensus.hrmanagementapi.domain.enums.RoleType;
import com.xtensus.hrmanagementapi.domain.enums.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserCreateRequest {

    @NotBlank
    @Size(max = 100)
    private String username;

    @NotBlank
    @Email
    @Size(max = 150)
    private String email;

    @NotBlank
    @Size(min = 8, max = 100)
    private String password;

    @NotBlank
    @Size(max = 100)
    private String firstName;

    @NotBlank
    @Size(max = 100)
    private String lastName;

    @Size(max = 30)
    private String phone;

    private LocalDate hireDate;

    @NotNull
    private RoleType role;

    @NotNull
    private UserStatus status;

    @NotNull
    private Boolean enabled;

    private Long managerId;

    private Long departmentId;

    private Long positionId;
}
