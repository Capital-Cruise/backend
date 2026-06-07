package com.capitalcruise.platform.profiles.interfaces.rest.transform;

import com.capitalcruise.platform.profiles.domain.model.aggregates.Profile;
import com.capitalcruise.platform.profiles.interfaces.rest.resources.ProfileResource;

public class ProfileResourceFromEntityAssembler {

    private ProfileResourceFromEntityAssembler() {
    }

    public static ProfileResource toResourceFromEntity(Profile profile) {
        return new ProfileResource(
                profile.getId(),
                profile.getFirstName(),
                profile.getLastName(),
                profile.getEmail(),
                profile.getDocumentNumber()
        );
    }
}

