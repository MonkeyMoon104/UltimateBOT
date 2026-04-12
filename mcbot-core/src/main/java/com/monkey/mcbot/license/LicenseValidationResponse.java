package com.monkey.mcbot.license;

import java.time.Instant;

public record LicenseValidationResponse(
        boolean allowed,
        String status,
        String reasonCode,
        String plan,
        Instant expiresAt,
        Integer maxServers,
        String message,
        Instant graceUntil
) {
}
