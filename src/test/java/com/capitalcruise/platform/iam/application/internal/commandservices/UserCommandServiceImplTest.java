package com.capitalcruise.platform.iam.application.internal.commandservices;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.capitalcruise.platform.iam.application.internal.outboundservices.PasswordHashingService;
import com.capitalcruise.platform.iam.application.internal.outboundservices.TokenService;
import com.capitalcruise.platform.iam.domain.model.commands.SignInCommand;
import com.capitalcruise.platform.iam.domain.model.commands.SignUpCommand;
import com.capitalcruise.platform.iam.domain.model.entities.Role;
import com.capitalcruise.platform.iam.domain.model.valueobjects.RoleName;
import com.capitalcruise.platform.iam.infrastructure.persistence.jpa.repositories.RoleRepository;
import com.capitalcruise.platform.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import com.capitalcruise.platform.shared.domain.exceptions.DuplicatedResourceException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

@ExtendWith(MockitoExtension.class)
class UserCommandServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordHashingService passwordHashingService;

    @Mock
    private TokenService tokenService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private UserCommandServiceImpl userCommandService;

    @Test
    void RegisterUserWhenUsernameIsAvailableShouldPersistUserWithHashedPassword() {
        SignUpCommand command = new SignUpCommand("student1", "PlainPass123", null);
        Role defaultRole = new Role(RoleName.ROLE_USER);

        when(userRepository.existsByUsernameIgnoreCase("student1")).thenReturn(false);
        when(roleRepository.findByName(RoleName.ROLE_USER)).thenReturn(Optional.of(defaultRole));
        when(passwordHashingService.hash("PlainPass123")).thenReturn("$2a$10$hashed");
        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        userCommandService.handle(command);

        ArgumentCaptor<com.capitalcruise.platform.iam.domain.model.aggregates.User> userCaptor =
                ArgumentCaptor.forClass(com.capitalcruise.platform.iam.domain.model.aggregates.User.class);
        verify(userRepository).save(userCaptor.capture());

        var persistedUser = userCaptor.getValue();
        assertEquals("student1", persistedUser.getUsername());
        assertEquals("$2a$10$hashed", persistedUser.getPasswordHash());
        assertTrue(persistedUser.roleNames().contains("ROLE_USER"));
    }

    @Test
    void RegisterUserWhenUsernameAlreadyExistsShouldThrowDuplicatedResourceException() {
        SignUpCommand command = new SignUpCommand("student1", "PlainPass123", null);
        when(userRepository.existsByUsernameIgnoreCase("student1")).thenReturn(true);

        assertThrows(DuplicatedResourceException.class, () -> userCommandService.handle(command));

        verify(userRepository, never()).save(any());
    }

    @Test
    void SignInWhenCredentialsAreValidShouldReturnToken() {
        SignUpCommand signUpCommand = new SignUpCommand("student2", "PlainPass123", null);
        Role defaultRole = new Role(RoleName.ROLE_USER);
        when(userRepository.existsByUsernameIgnoreCase("student2")).thenReturn(false);
        when(roleRepository.findByName(RoleName.ROLE_USER)).thenReturn(Optional.of(defaultRole));
        when(passwordHashingService.hash("PlainPass123")).thenReturn("$2a$10$hashed");
        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        var saved = userCommandService.handle(signUpCommand);

        when(userRepository.findByUsernameIgnoreCase("student2")).thenReturn(Optional.of(saved));
        when(tokenService.generateToken(saved)).thenReturn("jwt-token");

        SignInCommand signInCommand = new SignInCommand("student2", "PlainPass123");
        var response = userCommandService.handle(signInCommand);

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        assertEquals("jwt-token", response.token());
        assertEquals("student2", response.username());
    }

    @Test
    void SignInWhenPasswordIsInvalidShouldReturnUnauthorized() {
        SignInCommand signInCommand = new SignInCommand("student2", "wrong-password");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class, () -> userCommandService.handle(signInCommand));

        verify(tokenService, never()).generateToken(any());
    }
}

