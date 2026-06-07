package com.capitalcruise.platform.commercial.application.internal.commandservices;

import com.capitalcruise.platform.commercial.domain.model.aggregates.Vehicle;
import com.capitalcruise.platform.commercial.domain.model.commands.CreateVehicleCommand;
import com.capitalcruise.platform.commercial.domain.model.commands.UpdateVehicleCommand;
import com.capitalcruise.platform.commercial.domain.model.valueobjects.Currency;
import com.capitalcruise.platform.commercial.domain.services.VehicleCommandService;
import com.capitalcruise.platform.commercial.infrastructure.persistence.jpa.repositories.VehicleRepository;
import com.capitalcruise.platform.shared.domain.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VehicleCommandServiceImpl implements VehicleCommandService {

    private final VehicleRepository vehicleRepository;

    public VehicleCommandServiceImpl(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    @Override
    @Transactional
    public Vehicle handle(CreateVehicleCommand command) {
        Vehicle vehicle = Vehicle.create(
                command.brand(),
                command.model(),
                command.year(),
                command.vehicleType(),
                command.commercialPrice(),
                normalizeCurrency(command.currency()),
                command.description(),
                command.imageUrl()
        );
        return vehicleRepository.save(vehicle);
    }

    @Override
    @Transactional
    public Vehicle handle(UpdateVehicleCommand command) {
        Vehicle vehicle = vehicleRepository.findById(command.vehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));
        vehicle.update(
                command.brand(),
                command.model(),
                command.year(),
                command.vehicleType(),
                command.commercialPrice(),
                normalizeCurrency(command.currency()),
                command.description(),
                command.imageUrl()
        );
        return vehicleRepository.save(vehicle);
    }

    private Currency normalizeCurrency(Currency currency) {
        return currency;
    }
}
