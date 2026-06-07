package com.capitalcruise.platform.profiles.domain.model.commands;

public record CreateProfileCommand(
        String firstName,
        String lastName,
        String email,
        String documentNumber,
        Long userId
) {
}

