package com.capitalcruise.platform.profiles.domain.model.valueobjects;

import com.capitalcruise.platform.shared.domain.exceptions.InvalidBusinessRuleException;
import java.util.regex.Pattern;

public class DocumentNumber {

    private static final Pattern DOCUMENT_PATTERN = Pattern.compile("^[0-9]{8}$");

    private final String value;

    public DocumentNumber(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidBusinessRuleException("Document number cannot be empty");
        }
        String normalized = value.trim();
        if (!DOCUMENT_PATTERN.matcher(normalized).matches()) {
            throw new InvalidBusinessRuleException("Document number format is invalid");
        }
        this.value = normalized;
    }

    public String value() {
        return value;
    }
}

