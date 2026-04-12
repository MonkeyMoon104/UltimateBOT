package com.monkey.mcbot.licenseserver.api.dto;

import com.monkey.mcbot.licenseserver.domain.LicenseStatus;

import java.time.Instant;

public record PluginLicenseResponse(
        boolean allowed,
        LicenseStatus status,
        String reasonCode,
        String plan,
        Instant expiresAt,
        Integer maxServers,
        String message,
        Instant graceUntil
) {

    public static PluginLicenseResponse allowed(LicenseStatus status,
                                                String plan,
                                                Instant expiresAt,
                                                Integer maxServers,
                                                String message,
                                                Instant graceUntil) {
        return new PluginLicenseResponse(true, status, null, plan, expiresAt, maxServers, message, graceUntil);
    }

    public static PluginLicenseResponse denied(LicenseStatus status, String reasonCode, String message) {
        return new PluginLicenseResponse(false, status, reasonCode, null, null, null, message, null);
    }
}
