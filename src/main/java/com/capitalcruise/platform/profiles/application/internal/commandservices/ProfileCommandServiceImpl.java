package com.capitalcruise.platform.profiles.application.internal.commandservices;

import com.capitalcruise.platform.profiles.domain.model.aggregates.Profile;
import com.capitalcruise.platform.profiles.domain.model.commands.CreateProfileCommand;
import com.capitalcruise.platform.profiles.domain.model.valueobjects.EmailAddress;
import com.capitalcruise.platform.profiles.domain.services.ProfileCommandService;
import com.capitalcruise.platform.profiles.infrastructure.persistence.jpa.repositories.ProfileRepository;
import com.capitalcruise.platform.shared.domain.exceptions.DuplicatedResourceException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileCommandServiceImpl implements ProfileCommandService {

    private final ProfileRepository profileRepository;

    public ProfileCommandServiceImpl(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    @Override
    @Transactional
    public Profile handle(CreateProfileCommand command) {
        String normalizedEmail = new EmailAddress(command.email()).value();
        if (profileRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicatedResourceException("Email already exists");
        }

        Profile profile = Profile.create(
                command.firstName(),
                command.lastName(),
                normalizedEmail,
                command.documentNumber(),
                command.userId()
        );
        return profileRepository.save(profile);
    }
}

