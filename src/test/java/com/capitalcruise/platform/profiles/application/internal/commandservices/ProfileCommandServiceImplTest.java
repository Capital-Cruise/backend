package com.capitalcruise.platform.profiles.application.internal.commandservices;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.capitalcruise.platform.profiles.domain.model.commands.CreateProfileCommand;
import com.capitalcruise.platform.profiles.infrastructure.persistence.jpa.repositories.ProfileRepository;
import com.capitalcruise.platform.shared.domain.exceptions.DuplicatedResourceException;
import com.capitalcruise.platform.shared.domain.exceptions.InvalidBusinessRuleException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProfileCommandServiceImplTest {

    @Mock
    private ProfileRepository profileRepository;

    @InjectMocks
    private ProfileCommandServiceImpl profileCommandService;

    @Test
    void CreateProfileWhenEmailIsUniqueShouldPersistProfile() {
        CreateProfileCommand command = new CreateProfileCommand(
                "Ana",
                "Rios",
                "ana.rios@mail.com",
                "12345678",
                50L
        );

        when(profileRepository.existsByEmail("ana.rios@mail.com")).thenReturn(false);
        when(profileRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        profileCommandService.handle(command);

        ArgumentCaptor<com.capitalcruise.platform.profiles.domain.model.aggregates.Profile> profileCaptor =
                ArgumentCaptor.forClass(com.capitalcruise.platform.profiles.domain.model.aggregates.Profile.class);
        verify(profileRepository).save(profileCaptor.capture());

        var persisted = profileCaptor.getValue();
        assertEquals("ana.rios@mail.com", persisted.getEmail());
        assertEquals("12345678", persisted.getDocumentNumber());
    }

    @Test
    void CreateProfileWhenEmailAlreadyExistsShouldThrowDuplicatedResourceException() {
        CreateProfileCommand command = new CreateProfileCommand(
                "Ana",
                "Rios",
                "ana.rios@mail.com",
                "12345678",
                50L
        );

        when(profileRepository.existsByEmail("ana.rios@mail.com")).thenReturn(true);

        assertThrows(DuplicatedResourceException.class, () -> profileCommandService.handle(command));

        verify(profileRepository, never()).save(any());
    }

    @Test
    void CreateProfileWhenDocumentNumberIsInvalidShouldThrowInvalidBusinessRuleException() {
        CreateProfileCommand command = new CreateProfileCommand(
                "Ana",
                "Rios",
                "ana.rios@mail.com",
                "1234AB",
                50L
        );

        assertThrows(InvalidBusinessRuleException.class, () -> profileCommandService.handle(command));

        verify(profileRepository, never()).save(any());
    }
}

