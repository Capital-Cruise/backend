package com.capitalcruise.platform.profiles.domain.model.valueobjects;

import com.capitalcruise.platform.shared.domain.exceptions.InvalidBusinessRuleException;
import java.util.regex.Pattern;

public class EmailAddress {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    private final String value;

    public EmailAddress(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidBusinessRuleException("Email cannot be empty");
        }
        String normalized = value.trim().toLowerCase();
        if (!EMAIL_PATTERN.matcher(normalized).matches()) {
            throw new InvalidBusinessRuleException("Email format is invalid");
        }
        if (normalized.length() > 120) {
            throw new InvalidBusinessRuleException("Email is too long");
        }
        this.value = normalized;
    }

    public String value() {
        return value;
    }
}

