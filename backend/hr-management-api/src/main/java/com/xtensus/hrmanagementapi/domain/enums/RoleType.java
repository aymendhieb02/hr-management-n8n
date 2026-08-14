package com.xtensus.hrmanagementapi.domain.enums;

public enum RoleType {
    EMPLOYEE,
    DG,
    DT,
    HR,
    ADMIN;

    public static RoleType fromDatabaseRole(String role) {
        if (role == null) return EMPLOYEE;
        return switch (role) {
            case "EMPLOYE", "EMPLOYEE" -> EMPLOYEE;
            case "MANAGER", "DG" -> DG;
            case "DT" -> DT;
            case "RH", "HR" -> HR;
            case "ADMIN" -> ADMIN;
            default -> EMPLOYEE;
        };
    }

    public String toDatabaseRole() {
        return switch (this) {
            case EMPLOYEE -> "EMPLOYE";
            case DG -> "DG";
            case DT -> "DT";
            case HR -> "RH";
            case ADMIN -> "ADMIN";
        };
    }
}
