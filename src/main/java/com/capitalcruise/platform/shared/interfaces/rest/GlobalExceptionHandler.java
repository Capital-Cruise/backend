package com.capitalcruise.platform.shared.interfaces.rest;

import com.capitalcruise.platform.shared.domain.exceptions.DuplicatedResourceException;
import com.capitalcruise.platform.shared.domain.exceptions.ForbiddenBusinessOperationException;
import com.capitalcruise.platform.shared.domain.exceptions.InvalidBusinessRuleException;
import com.capitalcruise.platform.shared.domain.exceptions.InvalidStateTransitionException;
import com.capitalcruise.platform.shared.domain.exceptions.ResourceNotFoundException;
import com.capitalcruise.platform.shared.interfaces.rest.resources.ErrorResource;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResource> handleResourceNotFound(ResourceNotFoundException exception,
                                                                HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, exception.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(DuplicatedResourceException.class)
    public ResponseEntity<ErrorResource> handleDuplicatedResource(DuplicatedResourceException exception,
                                                                  HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, exception.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(InvalidBusinessRuleException.class)
    public ResponseEntity<ErrorResource> handleInvalidBusinessRule(InvalidBusinessRuleException exception,
                                                                   HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, exception.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(InvalidStateTransitionException.class)
    public ResponseEntity<ErrorResource> handleInvalidStateTransition(InvalidStateTransitionException exception,
                                                                      HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, exception.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(ForbiddenBusinessOperationException.class)
    public ResponseEntity<ErrorResource> handleForbiddenBusinessOperation(ForbiddenBusinessOperationException exception,
                                                                          HttpServletRequest request) {
        return buildResponse(HttpStatus.FORBIDDEN, exception.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResource> handleValidation(MethodArgumentNotValidException exception,
                                                          HttpServletRequest request) {
        List<String> details = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::formatFieldError)
                .collect(Collectors.toList());
        return buildResponse(HttpStatus.BAD_REQUEST, "Validation failed", request.getRequestURI(), details);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResource> handleMalformedJson(HttpMessageNotReadableException exception,
                                                             HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Malformed request", request.getRequestURI(), null);
    }

    @ExceptionHandler({BadCredentialsException.class, AuthenticationException.class})
    public ResponseEntity<ErrorResource> handleAuthentication(RuntimeException exception,
                                                              HttpServletRequest request) {
        return buildResponse(HttpStatus.UNAUTHORIZED, exception.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResource> handleAccessDenied(AccessDeniedException exception,
                                                            HttpServletRequest request) {
        return buildResponse(HttpStatus.FORBIDDEN, "Access denied", request.getRequestURI(), null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResource> handleUnknown(Exception exception,
                                                       HttpServletRequest request) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred",
                request.getRequestURI(),
                null);
    }

    private String formatFieldError(FieldError fieldError) {
        return fieldError.getField() + ": " + fieldError.getDefaultMessage();
    }

    private ResponseEntity<ErrorResource> buildResponse(HttpStatus status,
                                                        String message,
                                                        String path,
                                                        List<String> details) {
        ErrorResource errorResource = new ErrorResource(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path,
                details
        );
        return ResponseEntity.status(status).body(errorResource);
    }
}

