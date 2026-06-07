package com.capitalcruise.platform.profiles.domain.services;

import com.capitalcruise.platform.profiles.domain.model.aggregates.Profile;
import com.capitalcruise.platform.profiles.domain.model.queries.GetAllProfilesQuery;
import com.capitalcruise.platform.profiles.domain.model.queries.GetProfileByEmailQuery;
import com.capitalcruise.platform.profiles.domain.model.queries.GetProfileByIdQuery;
import java.util.List;

public interface ProfileQueryService {

    List<Profile> handle(GetAllProfilesQuery query);

    Profile handle(GetProfileByIdQuery query);

    Profile handle(GetProfileByEmailQuery query);
}

