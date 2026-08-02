package com.xtensus.hrmanagementapi.domain.enums;

public enum RoleType {
    EMPLOYEE,
    MANAGER,
    HR,
    ADMIN;

    public static RoleType fromDatabaseRole(String role) {
        if (role == null) return EMPLOYEE;
        return switch (role) {
            case "EMPLOYE", "EMPLOYEE" -> EMPLOYEE;
            case "MANAGER" -> MANAGER;
            case "RH", "HR" -> HR;
            case "ADMIN" -> ADMIN;
            default -> EMPLOYEE;
        };
    }

    public String toDatabaseRole() {
        return switch (this) {
            case EMPLOYEE -> "EMPLOYE";
            case MANAGER -> "MANAGER";
            case HR -> "RH";
            case ADMIN -> "ADMIN";
        };
    }
}
