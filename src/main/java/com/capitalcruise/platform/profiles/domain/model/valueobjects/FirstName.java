package com.capitalcruise.platform.profiles.domain.model.valueobjects;

import com.capitalcruise.platform.shared.domain.exceptions.InvalidBusinessRuleException;

public class FirstName {

    private final String value;

    public FirstName(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidBusinessRuleException("First name cannot be empty");
        }
        String normalized = value.trim();
        if (normalized.length() > 80) {
            throw new InvalidBusinessRuleException("First name is too long");
        }
        this.value = normalized;
    }

    public String value() {
        return value;
    }
}

