package com.monkey.mcbot.licenseserver.api.dto;

import com.monkey.mcbot.licenseserver.domain.LicenseStatus;

import java.time.Instant;
import java.util.UUID;

public record AdminCreateLicenseResponse(
        UUID id,
        String licenseKey,
        LicenseStatus status,
        Instant expiresAt,
        int maxServers
) {
}
