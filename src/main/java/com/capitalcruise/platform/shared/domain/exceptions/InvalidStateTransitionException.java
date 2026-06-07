package com.capitalcruise.platform.shared.domain.exceptions;

public class InvalidStateTransitionException extends DomainException {

    public InvalidStateTransitionException(String message) {
        super(message);
    }
}

