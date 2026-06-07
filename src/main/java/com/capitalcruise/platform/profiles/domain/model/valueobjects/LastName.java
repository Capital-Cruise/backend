package com.capitalcruise.platform.profiles.domain.model.valueobjects;

import com.capitalcruise.platform.shared.domain.exceptions.InvalidBusinessRuleException;

public class LastName {

    private final String value;

    public LastName(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidBusinessRuleException("Last name cannot be empty");
        }
        String normalized = value.trim();
        if (normalized.length() > 80) {
            throw new InvalidBusinessRuleException("Last name is too long");
        }
        this.value = normalized;
    }

    public String value() {
        return value;
    }
}

