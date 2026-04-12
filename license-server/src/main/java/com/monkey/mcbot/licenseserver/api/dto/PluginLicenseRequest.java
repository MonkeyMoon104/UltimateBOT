package com.monkey.mcbot.licenseserver.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PluginLicenseRequest(
        @NotBlank @Size(max = 32) String licenseKey,
        @NotBlank @Size(max = 64) String product,
        @NotBlank @Size(max = 64) String pluginVersion,
        @NotBlank @Size(max = 64) String installationId,
        @NotBlank @Size(max = 128) String fingerprintHash
) {
}
