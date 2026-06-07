package com.capitalcruise.platform.creditoperation.application.internal.queryservices;

import com.capitalcruise.platform.creditoperation.domain.model.aggregates.LoanOperation;
import com.capitalcruise.platform.creditoperation.domain.model.queries.GetAllLoanOperationsQuery;
import com.capitalcruise.platform.creditoperation.domain.model.queries.GetLoanOperationByIdQuery;
import com.capitalcruise.platform.creditoperation.domain.services.LoanOperationQueryService;
import com.capitalcruise.platform.creditoperation.infrastructure.persistence.jpa.repositories.LoanOperationRepository;
import com.capitalcruise.platform.shared.domain.exceptions.ResourceNotFoundException;
import jakarta.persistence.criteria.Predicate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoanOperationQueryServiceImpl implements LoanOperationQueryService {

    private final LoanOperationRepository loanOperationRepository;

    public LoanOperationQueryServiceImpl(LoanOperationRepository loanOperationRepository) {
        this.loanOperationRepository = loanOperationRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LoanOperation> handle(GetAllLoanOperationsQuery query) {
        PageRequest pageRequest = PageRequest.of(
                Math.max(query.page(), 0),
                Math.max(query.size(), 1),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
        return loanOperationRepository.findAll(buildSpecification(query), pageRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public LoanOperation handle(GetLoanOperationByIdQuery query) {
        return loanOperationRepository.findByIdAndUserId(query.operationId(), query.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Operation not found"));
    }

    private Specification<LoanOperation> buildSpecification(GetAllLoanOperationsQuery query) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("userId"), query.userId()));

            if (query.status() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), query.status()));
            }
            if (query.clientId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("clientId"), query.clientId()));
            }
            if (query.currency() != null) {
                predicates.add(criteriaBuilder.equal(root.get("operationCurrency"), query.currency()));
            }
            if (query.fromDate() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("createdAt"),
                        query.fromDate().atStartOfDay(ZoneOffset.UTC).toInstant()
                ));
            }
            if (query.toDate() != null) {
                predicates.add(criteriaBuilder.lessThan(
                        root.get("createdAt"),
                        query.toDate().plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant()
                ));
            }
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }
}
