package com.capitalcruise.platform.commercial.infrastructure.persistence.jpa.repositories;

import com.capitalcruise.platform.commercial.domain.model.aggregates.Client;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ClientRepository extends JpaRepository<Client, Long>, JpaSpecificationExecutor<Client> {

    boolean existsByDocumentNumber(String documentNumber);

    Optional<Client> findByDocumentNumber(String documentNumber);
}
