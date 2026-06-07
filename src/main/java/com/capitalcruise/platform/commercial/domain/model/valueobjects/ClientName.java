package com.capitalcruise.platform.commercial.domain.model.valueobjects;

import com.capitalcruise.platform.shared.domain.exceptions.InvalidBusinessRuleException;

public class ClientName {

    private final String firstName;
    private final String lastName;

    public ClientName(String firstName, String lastName) {
        this.firstName = normalizeRequired(firstName, "First name");
        this.lastName = normalizeRequired(lastName, "Last name");
    }

    public String firstName() {
        return firstName;
    }

    public String lastName() {
        return lastName;
    }

    private String normalizeRequired(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new InvalidBusinessRuleException(fieldName + " cannot be empty");
        }
        String normalized = value.trim();
        if (normalized.length() > 80) {
            throw new InvalidBusinessRuleException(fieldName + " is too long");
        }
        return normalized;
    }
}
