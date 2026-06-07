package com.capitalcruise.platform.commercial.application.internal.queryservices;

import com.capitalcruise.platform.commercial.domain.model.aggregates.Client;
import com.capitalcruise.platform.commercial.domain.model.queries.GetAllClientsQuery;
import com.capitalcruise.platform.commercial.domain.model.queries.GetClientByIdQuery;
import com.capitalcruise.platform.commercial.domain.model.queries.GetClientSummaryQuery;
import com.capitalcruise.platform.commercial.domain.services.ClientQueryService;
import com.capitalcruise.platform.commercial.infrastructure.persistence.jpa.repositories.ClientRepository;
import com.capitalcruise.platform.shared.domain.exceptions.ResourceNotFoundException;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class ClientQueryServiceImpl implements ClientQueryService {

    private final ClientRepository clientRepository;

    public ClientQueryServiceImpl(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Client> handle(GetAllClientsQuery query) {
        PageRequest pageRequest = PageRequest.of(
                Math.max(query.page(), 0),
                Math.max(query.size(), 1),
                parseSort(query.sort())
        );
        return clientRepository.findAll(buildSpecification(query.search(), query.documentNumber()), pageRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public Client handle(GetClientByIdQuery query) {
        return clientRepository.findById(query.clientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public long handle(GetClientSummaryQuery query) {
        return clientRepository.count();
    }

    private Specification<Client> buildSpecification(String search, String documentNumber) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(documentNumber)) {
                predicates.add(criteriaBuilder.equal(root.get("documentNumber"), documentNumber.trim()));
            }

            if (StringUtils.hasText(search)) {
                String likePattern = "%" + search.trim().toLowerCase() + "%";
                Predicate firstName = criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")), likePattern);
                Predicate lastName = criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")), likePattern);
                Predicate document = criteriaBuilder.like(criteriaBuilder.lower(root.get("documentNumber")), likePattern);
                Predicate email = criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), likePattern);
                Predicate fullName = criteriaBuilder.like(
                        criteriaBuilder.lower(
                                criteriaBuilder.concat(
                                        criteriaBuilder.concat(root.get("firstName"), " "),
                                        root.get("lastName")
                                )
                        ),
                        likePattern
                );
                predicates.add(criteriaBuilder.or(firstName, lastName, document, email, fullName));
            }

            if (predicates.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Sort parseSort(String sort) {
        if (!StringUtils.hasText(sort)) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }

        String[] parts = sort.split(",");
        String property = parts[0].trim();
        Sort.Direction direction = Sort.Direction.DESC;
        if (parts.length > 1 && StringUtils.hasText(parts[1])) {
            try {
                direction = Sort.Direction.fromString(parts[1].trim());
            } catch (IllegalArgumentException ignored) {
                direction = Sort.Direction.ASC;
            }
        }
        return Sort.by(direction, property);
    }
}
