package com.monkey.mcbot.licenseserver.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "license")
public record LicenseServerProperties(
        @NotBlank String hmacSecret,
        @NotBlank String adminToken,
        @Min(1) int activeWindowMinutes,
        @Min(1) int gracePeriodHours
) {
}
