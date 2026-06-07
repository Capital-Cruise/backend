package com.capitalcruise.platform.iam.domain.services;

import com.capitalcruise.platform.iam.domain.model.valueobjects.AuthenticationSession;

public interface AuthCommandService {

    AuthenticationSession login(String usernameOrEmail, String password);

    AuthenticationSession refresh(String refreshToken);

    void logout(String refreshToken);
}
