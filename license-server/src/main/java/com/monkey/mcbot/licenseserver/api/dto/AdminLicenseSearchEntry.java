package com.monkey.mcbot.licenseserver.api.dto;

import com.monkey.mcbot.licenseserver.domain.LicenseStatus;

import java.time.Instant;
import java.util.UUID;

public record AdminLicenseSearchEntry(
        UUID id,
        String licenseKeyPrefix,
        String licenseKey,
        String customerId,
        String productCode,
        String planCode,
        LicenseStatus status,
        Instant expiresAt,
        int maxServers,
        Instant createdAt
) {
}