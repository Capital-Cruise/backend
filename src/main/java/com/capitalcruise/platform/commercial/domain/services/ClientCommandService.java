package com.capitalcruise.platform.commercial.domain.services;

import com.capitalcruise.platform.commercial.domain.model.aggregates.Client;
import com.capitalcruise.platform.commercial.domain.model.commands.CreateClientCommand;
import com.capitalcruise.platform.commercial.domain.model.commands.UpdateClientCommand;

public interface ClientCommandService {

    Client handle(CreateClientCommand command);

    Client handle(UpdateClientCommand command);
}
