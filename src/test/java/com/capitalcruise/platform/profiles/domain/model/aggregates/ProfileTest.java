package com.capitalcruise.platform.profiles.domain.model.aggregates;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.capitalcruise.platform.shared.domain.exceptions.InvalidBusinessRuleException;
import org.junit.jupiter.api.Test;

class ProfileTest {

    @Test
    void CreateProfileWhenDataIsValidShouldBuildAggregateWithNormalizedEmail() {
        Profile profile = Profile.create("Ana", "Rios", "Ana.Rios@Mail.com", "12345678", 10L);

        assertEquals("ana.rios@mail.com", profile.getEmail());
        assertEquals("Ana", profile.getFirstName());
        assertEquals("Rios", profile.getLastName());
    }

    @Test
    void CreateProfileWhenFirstNameIsEmptyShouldThrowInvalidBusinessRuleException() {
        assertThrows(InvalidBusinessRuleException.class,
                () -> Profile.create(" ", "Rios", "ana@mail.com", "12345678", 10L));
    }
}

