package com.capitalcruise.platform.iam.domain.services;

import com.capitalcruise.platform.iam.domain.model.aggregates.User;
import com.capitalcruise.platform.iam.domain.model.queries.GetUserByUsernameQuery;
import java.util.Optional;

public interface UserQueryService {

    Optional<User> handle(GetUserByUsernameQuery query);
}

