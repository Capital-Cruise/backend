package com.capitalcruise.platform.iam.domain.model.commands;

public record SignUpCommand(
        String username,
        String password,
        String role
) {
}

