package com.capitalcruise.platform.iam.interfaces.rest.transform;

import com.capitalcruise.platform.iam.domain.model.aggregates.User;
import com.capitalcruise.platform.iam.interfaces.rest.resources.RegisteredUserResource;

public class RegisteredUserResourceFromEntityAssembler {

    private RegisteredUserResourceFromEntityAssembler() {
    }

    public static RegisteredUserResource toResourceFromEntity(User user) {
        return new RegisteredUserResource(user.getId(), user.getUsername(), user.roleNames());
    }
}

