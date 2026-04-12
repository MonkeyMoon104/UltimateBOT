package com.monkey.mcbot.license;

public record LicenseValidationRequest(
        String licenseKey,
        String product,
        String pluginVersion,
        String installationId,
        String fingerprintHash
) {
}
