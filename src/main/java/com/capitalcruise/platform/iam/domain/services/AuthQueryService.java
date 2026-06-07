package com.capitalcruise.platform.iam.domain.services;

import com.capitalcruise.platform.iam.domain.model.valueobjects.AuthenticatedUserSummary;

public interface AuthQueryService {

    AuthenticatedUserSummary me(String username);
}
