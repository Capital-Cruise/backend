package com.capitalcruise.platform.profiles.domain.model.valueobjects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.capitalcruise.platform.shared.domain.exceptions.InvalidBusinessRuleException;
import org.junit.jupiter.api.Test;

class DocumentNumberTest {

    @Test
    void DocumentNumberWhenFormatIsValidShouldCreateValueObject() {
        DocumentNumber documentNumber = new DocumentNumber("12345678");

        assertEquals("12345678", documentNumber.value());
    }

    @Test
    void DocumentNumberWhenFormatIsInvalidShouldThrowInvalidBusinessRuleException() {
        assertThrows(InvalidBusinessRuleException.class, () -> new DocumentNumber("ABC-123"));
    }
}

