package com.capitalcruise.platform.shared.interfaces.rest.resources;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResource(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        List<String> details
) {
}

