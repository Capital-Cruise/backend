package com.capitalcruise.platform.commercial.domain.model.valueobjects;

import com.capitalcruise.platform.shared.domain.exceptions.InvalidBusinessRuleException;

public class DocumentNumber {

    private final String value;

    public DocumentNumber(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidBusinessRuleException("Document number cannot be empty");
        }
        String normalized = value.trim();
        if (normalized.length() > 40) {
            throw new InvalidBusinessRuleException("Document number is too long");
        }
        this.value = normalized;
    }

    public String value() {
        return value;
    }
}
