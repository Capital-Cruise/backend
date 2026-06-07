package com.capitalcruise.platform.profiles.domain.model.valueobjects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.capitalcruise.platform.shared.domain.exceptions.InvalidBusinessRuleException;
import org.junit.jupiter.api.Test;

class EmailAddressTest {

    @Test
    void EmailAddressWhenFormatIsValidShouldCreateValueObject() {
        EmailAddress emailAddress = new EmailAddress("Student@One.Academy");

        assertEquals("student@one.academy", emailAddress.value());
    }

    @Test
    void EmailAddressWhenFormatIsInvalidShouldThrowInvalidBusinessRuleException() {
        assertThrows(InvalidBusinessRuleException.class, () -> new EmailAddress("invalid-email"));
    }
}

