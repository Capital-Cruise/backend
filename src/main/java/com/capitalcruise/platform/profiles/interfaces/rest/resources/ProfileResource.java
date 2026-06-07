package com.capitalcruise.platform.profiles.interfaces.rest.resources;

public record ProfileResource(
        Long id,
        String firstName,
        String lastName,
        String email,
        String documentNumber
) {
}

