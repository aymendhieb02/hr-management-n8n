package com.xtensus.hrmanagementapi.user.dto;

import com.xtensus.hrmanagementapi.domain.enums.RoleType;
import com.xtensus.hrmanagementapi.domain.enums.UserStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserResponse {

    private Long id;

    private String username;

    private String email;

    private String firstName;

    private String lastName;

    private String phone;

    private LocalDate hireDate;

    private RoleType role;

    private UserStatus status;

    private Boolean enabled;

    private ManagerSummary manager;

    private DepartmentSummary department;

    private PositionSummary position;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class ManagerSummary {

        private Long id;

        private String firstName;

        private String lastName;

        private String email;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class DepartmentSummary {

        private Long id;

        private String name;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class PositionSummary {

        private Long id;

        private String title;
    }
}
