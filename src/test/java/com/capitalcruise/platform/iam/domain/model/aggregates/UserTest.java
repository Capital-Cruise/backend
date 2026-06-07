package com.capitalcruise.platform.iam.domain.model.aggregates;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.capitalcruise.platform.iam.domain.model.entities.Role;
import com.capitalcruise.platform.iam.domain.model.valueobjects.RoleName;
import com.capitalcruise.platform.shared.domain.exceptions.InvalidBusinessRuleException;
import java.util.Set;
import org.junit.jupiter.api.Test;

class UserTest {

    @Test
    void RegisterUserWhenRolesAreEmptyShouldThrowInvalidBusinessRuleException() {
        assertThrows(InvalidBusinessRuleException.class,
                () -> User.register("userx", "$2a$hash", Set.of()));
    }

    @Test
    void RegisterUserWhenUsernameIsBlankShouldThrowInvalidBusinessRuleException() {
        Role role = new Role(RoleName.ROLE_USER);
        assertThrows(InvalidBusinessRuleException.class,
                () -> User.register(" ", "$2a$hash", Set.of(role)));
    }
}

