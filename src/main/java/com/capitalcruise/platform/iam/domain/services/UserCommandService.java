package com.capitalcruise.platform.iam.domain.services;

import com.capitalcruise.platform.iam.domain.model.aggregates.User;
import com.capitalcruise.platform.iam.domain.model.commands.SignInCommand;
import com.capitalcruise.platform.iam.domain.model.commands.SignUpCommand;
import com.capitalcruise.platform.iam.domain.model.valueobjects.AuthenticatedUser;

public interface UserCommandService {

    User handle(SignUpCommand command);

    AuthenticatedUser handle(SignInCommand command);
}

