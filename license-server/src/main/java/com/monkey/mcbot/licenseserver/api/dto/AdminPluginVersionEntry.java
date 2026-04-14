package com.monkey.mcbot.licenseserver.api.dto;

public record AdminPluginVersionEntry(
        String productCode,
        String versionName,
        String releaseUrl,
        boolean latest
) {
}
