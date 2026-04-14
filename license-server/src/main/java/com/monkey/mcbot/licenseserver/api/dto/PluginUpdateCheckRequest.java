package com.monkey.mcbot.licenseserver.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PluginUpdateCheckRequest(
        @NotBlank @Size(max = 64) String product,
        @NotBlank @Size(max = 64) String currentVersion
) {
}
