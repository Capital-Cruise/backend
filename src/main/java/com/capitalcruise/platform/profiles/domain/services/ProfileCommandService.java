package com.capitalcruise.platform.profiles.domain.services;

import com.capitalcruise.platform.profiles.domain.model.aggregates.Profile;
import com.capitalcruise.platform.profiles.domain.model.commands.CreateProfileCommand;

public interface ProfileCommandService {

    Profile handle(CreateProfileCommand command);
}

