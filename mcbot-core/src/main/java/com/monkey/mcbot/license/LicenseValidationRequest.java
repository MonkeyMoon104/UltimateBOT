package com.monkey.mcbot.license;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LicenseValidationRequest(
        @JsonProperty("licenseKey") String licenseKey,
        @JsonProperty("product") String product,
        @JsonProperty("pluginVersion") String pluginVersion,
        @JsonProperty("installationId") String installationId,
        @JsonProperty("fingerprintHash") String fingerprintHash,
        @JsonProperty("hostFingerprint") String hostFingerprint,
        @JsonProperty("serverPort") Integer serverPort
) {
}
