package com.capitalcruise.platform.commercial.application.internal.queryservices;

import com.capitalcruise.platform.commercial.domain.model.aggregates.Vehicle;
import com.capitalcruise.platform.commercial.domain.model.queries.GetAllVehiclesQuery;
import com.capitalcruise.platform.commercial.domain.model.queries.GetVehicleByIdQuery;
import com.capitalcruise.platform.commercial.domain.model.queries.GetVehicleSummaryQuery;
import com.capitalcruise.platform.commercial.domain.model.valueobjects.Currency;
import com.capitalcruise.platform.commercial.domain.services.VehicleQueryService;
import com.capitalcruise.platform.commercial.infrastructure.persistence.jpa.repositories.VehicleRepository;
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
public class VehicleQueryServiceImpl implements VehicleQueryService {

    private final VehicleRepository vehicleRepository;

    public VehicleQueryServiceImpl(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Vehicle> handle(GetAllVehiclesQuery query) {
        PageRequest pageRequest = PageRequest.of(
                Math.max(query.page(), 0),
                Math.max(query.size(), 1),
                parseSort(query.sort())
        );
        return vehicleRepository.findAll(buildSpecification(query.search(), query.brand(), query.currency()), pageRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public Vehicle handle(GetVehicleByIdQuery query) {
        return vehicleRepository.findById(query.vehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public long handle(GetVehicleSummaryQuery query) {
        return vehicleRepository.count();
    }

    private Specification<Vehicle> buildSpecification(String search, String brand, String currency) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(brand)) {
                predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(root.get("brand")), brand.trim().toLowerCase()));
            }

            if (StringUtils.hasText(currency)) {
                Currency resolvedCurrency = Currency.valueOf(currency.trim().toUpperCase());
                predicates.add(criteriaBuilder.equal(root.get("currency"), resolvedCurrency));
            }

            if (StringUtils.hasText(search)) {
                String likePattern = "%" + search.trim().toLowerCase() + "%";
                Predicate brandPredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("brand")), likePattern);
                Predicate modelPredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("model")), likePattern);
                Predicate typePredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("vehicleType").as(String.class)), likePattern);
                Predicate descriptionPredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), likePattern);
                predicates.add(criteriaBuilder.or(brandPredicate, modelPredicate, typePredicate, descriptionPredicate));
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
