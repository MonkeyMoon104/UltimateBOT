package com.monkey.mcbot.licenseserver.repo;

import com.monkey.mcbot.licenseserver.domain.LicenseActivationEntity;
import com.monkey.mcbot.licenseserver.domain.LicenseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LicenseActivationRepository extends JpaRepository<LicenseActivationEntity, UUID> {

    Optional<LicenseActivationEntity> findByLicenseAndInstallationId(LicenseEntity license, String installationId);
    List<LicenseActivationEntity> findAllByLicense(LicenseEntity license);

    @Query("""
            select count(a)
            from LicenseActivationEntity a
            where a.license = :license
              and a.lastSeenAt >= :activeSince
            """)
    long countActiveByLicense(LicenseEntity license, Instant activeSince);
}
