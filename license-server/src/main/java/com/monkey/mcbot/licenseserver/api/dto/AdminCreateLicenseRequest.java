package com.monkey.mcbot.licenseserver.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record AdminCreateLicenseRequest(
        @NotBlank @Size(max = 128) String customerId,
        @NotBlank @Size(max = 64) String productCode,
        @NotBlank @Size(max = 64) String planCode,
        Instant expiresAt,
        @Min(1) int maxServers
) {
}
