package com.monkey.mcbot.licenseserver.repo;

import com.monkey.mcbot.licenseserver.domain.LicenseEntity;
import com.monkey.mcbot.licenseserver.domain.LicenseStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds a JPA {@link Specification} for the license search endpoint.
 * Each filter is only applied when its value is non-null/non-blank,
 * which avoids the Hibernate bug with nullable JPQL named parameters.
 */
public final class LicenseSearchSpec {

    private LicenseSearchSpec() {}

    public static Specification<LicenseEntity> of(
            String prefix,
            String product,
            LicenseStatus status,
            String customer
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (prefix != null && !prefix.isBlank()) {
                // licenseKeyPrefix ILIKE 'XX%'
                predicates.add(cb.like(
                        cb.lower(root.get("licenseKeyPrefix")),
                        prefix.toLowerCase() + "%"
                ));
            }

            if (product != null && !product.isBlank()) {
                predicates.add(cb.equal(
                        cb.lower(root.get("productCode")),
                        product.toLowerCase()
                ));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (customer != null && !customer.isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.get("customerId")),
                        "%" + customer.toLowerCase() + "%"
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}