package com.capitalcruise.platform.profiles.interfaces.rest;

import com.capitalcruise.platform.profiles.domain.model.queries.GetAllProfilesQuery;
import com.capitalcruise.platform.profiles.domain.model.queries.GetProfileByEmailQuery;
import com.capitalcruise.platform.profiles.domain.model.queries.GetProfileByIdQuery;
import com.capitalcruise.platform.profiles.domain.services.ProfileCommandService;
import com.capitalcruise.platform.profiles.domain.services.ProfileQueryService;
import com.capitalcruise.platform.profiles.interfaces.rest.resources.CreateProfileResource;
import com.capitalcruise.platform.profiles.interfaces.rest.resources.ProfileResource;
import com.capitalcruise.platform.profiles.interfaces.rest.transform.CreateProfileCommandFromResourceAssembler;
import com.capitalcruise.platform.profiles.interfaces.rest.transform.ProfileResourceFromEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/profiles")
@Tag(name = "Profiles")
public class ProfilesController {

    private final ProfileCommandService profileCommandService;
    private final ProfileQueryService profileQueryService;

    public ProfilesController(ProfileCommandService profileCommandService,
                              ProfileQueryService profileQueryService) {
        this.profileCommandService = profileCommandService;
        this.profileQueryService = profileQueryService;
    }

    @GetMapping
    @Operation(summary = "List all profiles")
    public ResponseEntity<List<ProfileResource>> getAllProfiles() {
        var profiles = profileQueryService.handle(new GetAllProfilesQuery());
        var resources = profiles.stream().map(ProfileResourceFromEntityAssembler::toResourceFromEntity).toList();
        return ResponseEntity.ok(resources);
    }

    @GetMapping("/{profileId}")
    @Operation(summary = "Get profile by id")
    public ResponseEntity<ProfileResource> getProfileById(@PathVariable Long profileId) {
        var profile = profileQueryService.handle(new GetProfileByIdQuery(profileId));
        var resource = ProfileResourceFromEntityAssembler.toResourceFromEntity(profile);
        return ResponseEntity.ok(resource);
    }

    @GetMapping("/by-email/{email}")
    @Operation(summary = "Get profile by email")
    public ResponseEntity<ProfileResource> getProfileByEmail(@PathVariable String email) {
        var profile = profileQueryService.handle(new GetProfileByEmailQuery(email));
        var resource = ProfileResourceFromEntityAssembler.toResourceFromEntity(profile);
        return ResponseEntity.ok(resource);
    }

    @PostMapping
    @Operation(summary = "Create profile")
    public ResponseEntity<ProfileResource> createProfile(@Valid @RequestBody CreateProfileResource requestResource) {
        var command = CreateProfileCommandFromResourceAssembler.toCommandFromResource(requestResource);
        var profile = profileCommandService.handle(command);
        var resource = ProfileResourceFromEntityAssembler.toResourceFromEntity(profile);
        return ResponseEntity.status(HttpStatus.CREATED).body(resource);
    }
}

