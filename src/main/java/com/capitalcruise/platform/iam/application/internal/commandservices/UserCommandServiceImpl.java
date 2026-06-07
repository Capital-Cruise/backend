package com.capitalcruise.platform.iam.application.internal.commandservices;

import com.capitalcruise.platform.iam.application.internal.outboundservices.PasswordHashingService;
import com.capitalcruise.platform.iam.application.internal.outboundservices.TokenService;
import com.capitalcruise.platform.iam.domain.model.aggregates.User;
import com.capitalcruise.platform.iam.domain.model.commands.SignInCommand;
import com.capitalcruise.platform.iam.domain.model.commands.SignUpCommand;
import com.capitalcruise.platform.iam.domain.model.entities.Role;
import com.capitalcruise.platform.iam.domain.model.valueobjects.AuthenticatedUser;
import com.capitalcruise.platform.iam.domain.model.valueobjects.RoleName;
import com.capitalcruise.platform.iam.domain.services.UserCommandService;
import com.capitalcruise.platform.iam.infrastructure.persistence.jpa.repositories.RoleRepository;
import com.capitalcruise.platform.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import com.capitalcruise.platform.shared.domain.exceptions.DuplicatedResourceException;
import com.capitalcruise.platform.shared.domain.exceptions.InvalidBusinessRuleException;
import java.util.Set;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserCommandServiceImpl implements UserCommandService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordHashingService passwordHashingService;
    private final TokenService tokenService;
    private final AuthenticationManager authenticationManager;

    public UserCommandServiceImpl(UserRepository userRepository,
                                  RoleRepository roleRepository,
                                  PasswordHashingService passwordHashingService,
                                  TokenService tokenService,
                                  AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordHashingService = passwordHashingService;
        this.tokenService = tokenService;
        this.authenticationManager = authenticationManager;
    }

    @Override
    @Transactional
    public User handle(SignUpCommand command) {
        String normalizedUsername = normalizeUsername(command.username());
        if (userRepository.existsByUsernameIgnoreCase(normalizedUsername)) {
            throw new DuplicatedResourceException("Username already exists");
        }

        Role role = resolveRole(command.role());
        String passwordHash = passwordHashingService.hash(validatePassword(command.password()));
        User user = User.register(normalizedUsername, passwordHash, Set.of(role));
        return userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthenticatedUser handle(SignInCommand command) {
        String normalizedUsername = normalizeUsername(command.username());
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                normalizedUsername,
                command.password()
        ));

        User user = userRepository.findByUsernameIgnoreCase(normalizedUsername)
                .orElseThrow(() -> new InvalidBusinessRuleException("Invalid credentials"));

        String token = tokenService.generateToken(user);
        return new AuthenticatedUser(user.getUsername(), token, user.roleNames());
    }

    private Role resolveRole(String requestedRole) {
        RoleName roleName;
        try {
            roleName = RoleName.from(requestedRole);
        } catch (IllegalArgumentException exception) {
            throw new InvalidBusinessRuleException("Role is not valid");
        }

        return roleRepository.findByName(roleName)
                .orElseThrow(() -> new InvalidBusinessRuleException("Role does not exist in catalog"));
    }

    private String normalizeUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new InvalidBusinessRuleException("Username cannot be empty");
        }
        return username.trim().toLowerCase();
    }

    private String validatePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new InvalidBusinessRuleException("Password cannot be empty");
        }
        return password;
    }
}

