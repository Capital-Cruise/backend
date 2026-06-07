package com.capitalcruise.platform.shared.domain.exceptions;

public class ForbiddenBusinessOperationException extends DomainException {

    public ForbiddenBusinessOperationException(String message) {
        super(message);
    }
}

