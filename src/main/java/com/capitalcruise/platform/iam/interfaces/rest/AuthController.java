package com.capitalcruise.platform.iam.interfaces.rest;

import com.capitalcruise.platform.iam.domain.services.AuthCommandService;
import com.capitalcruise.platform.iam.domain.services.AuthQueryService;
import com.capitalcruise.platform.iam.interfaces.rest.resources.AuthSessionResource;
import com.capitalcruise.platform.iam.interfaces.rest.resources.AuthUserResource;
import com.capitalcruise.platform.iam.interfaces.rest.resources.LoginRequestResource;
import com.capitalcruise.platform.iam.interfaces.rest.resources.LogoutRequestResource;
import com.capitalcruise.platform.iam.interfaces.rest.resources.RefreshTokenRequestResource;
import com.capitalcruise.platform.iam.interfaces.rest.transform.AuthSessionResourceFromValueObjectAssembler;
import com.capitalcruise.platform.iam.interfaces.rest.transform.AuthUserResourceFromValueObjectAssembler;
import com.capitalcruise.platform.iam.interfaces.rest.transform.LoginRequestResourceToUsernamePasswordAssembler;
import com.capitalcruise.platform.iam.interfaces.rest.transform.RefreshTokenRequestResourceToValueAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "IAM")
public class AuthController {

    private final AuthCommandService authCommandService;
    private final AuthQueryService authQueryService;

    public AuthController(AuthCommandService authCommandService,
                          AuthQueryService authQueryService) {
        this.authCommandService = authCommandService;
        this.authQueryService = authQueryService;
    }

    @PostMapping("/login")
    @Operation(summary = "Login internal user")
    public ResponseEntity<AuthSessionResource> login(@Valid @RequestBody LoginRequestResource requestResource) {
        var credentials = LoginRequestResourceToUsernamePasswordAssembler.toCredentials(requestResource);
        var session = authCommandService.login(credentials.usernameOrEmail(), credentials.password());
        return ResponseEntity.ok(AuthSessionResourceFromValueObjectAssembler.toResource(session));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token")
    public ResponseEntity<AuthSessionResource> refresh(@Valid @RequestBody RefreshTokenRequestResource requestResource) {
        var token = RefreshTokenRequestResourceToValueAssembler.toToken(requestResource);
        var session = authCommandService.refresh(token);
        return ResponseEntity.ok(AuthSessionResourceFromValueObjectAssembler.toResource(session));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout user and revoke refresh token")
    public ResponseEntity<Void> logout(@Valid @RequestBody LogoutRequestResource requestResource) {
        authCommandService.logout(requestResource.refreshToken());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user")
    public ResponseEntity<AuthUserResource> me(Authentication authentication,
                                               @AuthenticationPrincipal UserDetails userDetails) {
        if (authentication == null || !authentication.isAuthenticated() || userDetails == null) {
            throw new BadCredentialsException("Unauthorized");
        }
        var user = authQueryService.me(userDetails.getUsername());
        return ResponseEntity.ok(AuthUserResourceFromValueObjectAssembler.toResource(user));
    }
}
