package com.monkey.mcbot.licenseserver.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminRevokeLicenseRequest(
        @NotBlank @Size(max = 255) String reason
) {
}
