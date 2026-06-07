package com.capitalcruise.platform.iam.domain.model.valueobjects;

public enum RoleName {
    ROLE_USER,
    ROLE_ADMIN;

    public static RoleName from(String role) {
        if (role == null || role.isBlank()) {
            return ROLE_USER;
        }
        String normalized = role.trim().toUpperCase();
        if (!normalized.startsWith("ROLE_")) {
            normalized = "ROLE_" + normalized;
        }
        return RoleName.valueOf(normalized);
    }
}

