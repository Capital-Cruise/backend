package com.capitalcruise.platform.creditoperation.infrastructure.persistence.jpa.repositories;

import com.capitalcruise.platform.creditoperation.domain.model.entities.PublicQuoteShare;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PublicQuoteShareRepository extends JpaRepository<PublicQuoteShare, Long> {
    Optional<PublicQuoteShare> findByShareToken(String shareToken);

    Optional<PublicQuoteShare> findFirstByOperationIdAndActiveTrueOrderByCreatedAtDesc(Long operationId);
}
