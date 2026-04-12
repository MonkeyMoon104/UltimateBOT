package com.monkey.mcbot.licenseserver.repo;

import com.monkey.mcbot.licenseserver.domain.LicenseEntity;
import com.monkey.mcbot.licenseserver.domain.LicenseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LicenseRepository extends JpaRepository<LicenseEntity, UUID>,
        JpaSpecificationExecutor<LicenseEntity> {

    Optional<LicenseEntity> findByLicenseKeyHmac(String licenseKeyHmac);

    boolean existsByLicenseKeyHmac(String licenseKeyHmac);

    @Query("""
            select l from LicenseEntity l
            where l.status = :status
              and l.expiresAt is not null
              and l.expiresAt <= :horizon
            order by l.expiresAt asc
            """)
    List<LicenseEntity> findActiveExpiringBefore(LicenseStatus status, Instant horizon);
}