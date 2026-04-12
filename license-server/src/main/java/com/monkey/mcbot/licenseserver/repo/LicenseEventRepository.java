package com.monkey.mcbot.licenseserver.repo;

import com.monkey.mcbot.licenseserver.domain.LicenseEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LicenseEventRepository extends JpaRepository<LicenseEventEntity, UUID> {
}
