package com.monkey.mcbot.licenseserver.repo;

import com.monkey.mcbot.licenseserver.domain.LicenseEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LicenseRepository extends JpaRepository<LicenseEntity, UUID> {

    Optional<LicenseEntity> findByLicenseKeyHmac(String licenseKeyHmac);

    boolean existsByLicenseKeyHmac(String licenseKeyHmac);
}
