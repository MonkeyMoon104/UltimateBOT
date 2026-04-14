package com.monkey.mcbot.licenseserver.api.dto;

public record PluginUpdateCheckResponse(
        boolean updateAvailable,
        String product,
        String currentVersion,
        String latestVersion,
        String downloadUrl,
        String message
) {
}
