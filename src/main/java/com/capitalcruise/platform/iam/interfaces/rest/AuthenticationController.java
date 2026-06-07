package com.capitalcruise.platform.iam.interfaces.rest;

import com.capitalcruise.platform.iam.domain.services.UserCommandService;
import com.capitalcruise.platform.iam.interfaces.rest.resources.AuthenticatedUserResource;
import com.capitalcruise.platform.iam.interfaces.rest.resources.RegisteredUserResource;
import com.capitalcruise.platform.iam.interfaces.rest.resources.SignInRequestResource;
import com.capitalcruise.platform.iam.interfaces.rest.resources.SignUpRequestResource;
import com.capitalcruise.platform.iam.interfaces.rest.transform.AuthenticatedUserResourceFromValueObjectAssembler;
import com.capitalcruise.platform.iam.interfaces.rest.transform.RegisteredUserResourceFromEntityAssembler;
import com.capitalcruise.platform.iam.interfaces.rest.transform.SignInCommandFromResourceAssembler;
import com.capitalcruise.platform.iam.interfaces.rest.transform.SignUpCommandFromResourceAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/authentication")
@Tag(name = "IAM")
public class AuthenticationController {

    private final UserCommandService userCommandService;

    public AuthenticationController(UserCommandService userCommandService) {
        this.userCommandService = userCommandService;
    }

    @PostMapping("/sign-up")
    @Operation(summary = "Register user")
    public ResponseEntity<RegisteredUserResource> signUp(@Valid @RequestBody SignUpRequestResource requestResource) {
        var command = SignUpCommandFromResourceAssembler.toCommandFromResource(requestResource);
        var createdUser = userCommandService.handle(command);
        var responseResource = RegisteredUserResourceFromEntityAssembler.toResourceFromEntity(createdUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseResource);
    }

    @PostMapping("/sign-in")
    @Operation(summary = "Authenticate user")
    public ResponseEntity<AuthenticatedUserResource> signIn(@Valid @RequestBody SignInRequestResource requestResource) {
        var command = SignInCommandFromResourceAssembler.toCommandFromResource(requestResource);
        var authenticatedUser = userCommandService.handle(command);
        var responseResource = AuthenticatedUserResourceFromValueObjectAssembler.toResourceFromValueObject(authenticatedUser);
        return ResponseEntity.ok(responseResource);
    }
}

