package com.capitalcruise.platform.profiles.application.internal.queryservices;

import com.capitalcruise.platform.profiles.domain.model.aggregates.Profile;
import com.capitalcruise.platform.profiles.domain.model.queries.GetAllProfilesQuery;
import com.capitalcruise.platform.profiles.domain.model.queries.GetProfileByEmailQuery;
import com.capitalcruise.platform.profiles.domain.model.queries.GetProfileByIdQuery;
import com.capitalcruise.platform.profiles.domain.model.valueobjects.EmailAddress;
import com.capitalcruise.platform.profiles.domain.services.ProfileQueryService;
import com.capitalcruise.platform.profiles.infrastructure.persistence.jpa.repositories.ProfileRepository;
import com.capitalcruise.platform.shared.domain.exceptions.ResourceNotFoundException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileQueryServiceImpl implements ProfileQueryService {

    private final ProfileRepository profileRepository;

    public ProfileQueryServiceImpl(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Profile> handle(GetAllProfilesQuery query) {
        return profileRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Profile handle(GetProfileByIdQuery query) {
        return profileRepository.findById(query.profileId())
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public Profile handle(GetProfileByEmailQuery query) {
        String normalizedEmail = new EmailAddress(query.email()).value();
        return profileRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));
    }
}

