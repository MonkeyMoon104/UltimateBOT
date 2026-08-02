package com.monkey.ultimatebot.license;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LicenseValidationResponse(
        @JsonProperty("allowed") boolean allowed,
        @JsonProperty("status") String status,
        @JsonProperty("reasonCode") String reasonCode,
        @JsonProperty("plan") String plan,
        @JsonProperty("expiresAt") Instant expiresAt,
        @JsonProperty("maxServers") Integer maxServers,
        @JsonProperty("message") String message,
        @JsonProperty("graceUntil") Instant graceUntil) {}
