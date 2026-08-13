package com.monkey.ultimatebot.license;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class LicenseValidationResponse {
    private final boolean allowed;
    private final String status;
    private final String reasonCode;
    private final String plan;
    private final Instant expiresAt;
    private final Integer maxServers;
    private final String message;
    private final Instant graceUntil;

    public LicenseValidationResponse(
            @JsonProperty("allowed") boolean allowed,
            @JsonProperty("status") String status,
            @JsonProperty("reasonCode") String reasonCode,
            @JsonProperty("plan") String plan,
            @JsonProperty("expiresAt") Instant expiresAt,
            @JsonProperty("maxServers") Integer maxServers,
            @JsonProperty("message") String message,
            @JsonProperty("graceUntil") Instant graceUntil) {
        this.allowed = allowed;
        this.status = status;
        this.reasonCode = reasonCode;
        this.plan = plan;
        this.expiresAt = expiresAt;
        this.maxServers = maxServers;
        this.message = message;
        this.graceUntil = graceUntil;
    }

    @JsonProperty("allowed")
    public boolean allowed() {
        return allowed;
    }

    @JsonProperty("status")
    public String status() {
        return status;
    }

    @JsonProperty("reasonCode")
    public String reasonCode() {
        return reasonCode;
    }

    @JsonProperty("plan")
    public String plan() {
        return plan;
    }

    @JsonProperty("expiresAt")
    public Instant expiresAt() {
        return expiresAt;
    }

    @JsonProperty("maxServers")
    public Integer maxServers() {
        return maxServers;
    }

    @JsonProperty("message")
    public String message() {
        return message;
    }

    @JsonProperty("graceUntil")
    public Instant graceUntil() {
        return graceUntil;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof LicenseValidationResponse)) {
            return false;
        }
        LicenseValidationResponse other = (LicenseValidationResponse) obj;
        return allowed == other.allowed
                && Objects.equals(status, other.status)
                && Objects.equals(reasonCode, other.reasonCode)
                && Objects.equals(plan, other.plan)
                && Objects.equals(expiresAt, other.expiresAt)
                && Objects.equals(maxServers, other.maxServers)
                && Objects.equals(message, other.message)
                && Objects.equals(graceUntil, other.graceUntil);
    }

    @Override
    public int hashCode() {
        return Objects.hash(allowed, status, reasonCode, plan, expiresAt, maxServers, message, graceUntil);
    }

    @Override
    public String toString() {
        return "LicenseValidationResponse[allowed="
                + allowed
                + ", status="
                + status
                + ", reasonCode="
                + reasonCode
                + ", plan="
                + plan
                + ", expiresAt="
                + expiresAt
                + ", maxServers="
                + maxServers
                + ", message="
                + message
                + ", graceUntil="
                + graceUntil
                + "]";
    }
}
