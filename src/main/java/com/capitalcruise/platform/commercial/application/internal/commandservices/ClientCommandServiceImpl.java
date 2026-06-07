package com.capitalcruise.platform.commercial.application.internal.commandservices;

import com.capitalcruise.platform.commercial.domain.model.aggregates.Client;
import com.capitalcruise.platform.commercial.domain.model.commands.CreateClientCommand;
import com.capitalcruise.platform.commercial.domain.model.commands.UpdateClientCommand;
import com.capitalcruise.platform.commercial.domain.model.valueobjects.DocumentNumber;
import com.capitalcruise.platform.commercial.domain.model.valueobjects.EmailAddress;
import com.capitalcruise.platform.commercial.domain.services.ClientCommandService;
import com.capitalcruise.platform.commercial.infrastructure.persistence.jpa.repositories.ClientRepository;
import com.capitalcruise.platform.shared.domain.exceptions.DuplicatedResourceException;
import com.capitalcruise.platform.shared.domain.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClientCommandServiceImpl implements ClientCommandService {

    private final ClientRepository clientRepository;

    public ClientCommandServiceImpl(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Override
    @Transactional
    public Client handle(CreateClientCommand command) {
        String normalizedDocumentNumber = new DocumentNumber(command.documentNumber()).value();
        if (clientRepository.existsByDocumentNumber(normalizedDocumentNumber)) {
            throw new DuplicatedResourceException("Document number already exists");
        }

        Client client = Client.create(
                command.firstName(),
                command.lastName(),
                command.documentType(),
                normalizedDocumentNumber,
                normalizeEmail(command.email()),
                command.phone(),
                command.address(),
                command.monthlyIncome(),
                command.notes()
        );
        return clientRepository.save(client);
    }

    @Override
    @Transactional
    public Client handle(UpdateClientCommand command) {
        Client client = clientRepository.findById(command.clientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client not found"));

        String normalizedDocumentNumber = new DocumentNumber(command.documentNumber()).value();
        clientRepository.findByDocumentNumber(normalizedDocumentNumber)
                .filter(existing -> !existing.getId().equals(client.getId()))
                .ifPresent(existing -> {
                    throw new DuplicatedResourceException("Document number already exists");
                });

        client.update(
                command.firstName(),
                command.lastName(),
                command.documentType(),
                normalizedDocumentNumber,
                normalizeEmail(command.email()),
                command.phone(),
                command.address(),
                command.monthlyIncome(),
                command.notes()
        );
        return clientRepository.save(client);
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return new EmailAddress(email).value();
    }
}
