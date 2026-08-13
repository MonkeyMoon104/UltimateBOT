package com.monkey.ultimatebot.license;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.time.Instant;

public final class LicenseState {
    private final Instant lastSuccessfulValidationAt;

    @JsonCreator
    public LicenseState(@JsonProperty("lastSuccessfulValidationAt") Instant lastSuccessfulValidationAt) {
        this.lastSuccessfulValidationAt = lastSuccessfulValidationAt;
    }

    @JsonProperty("lastSuccessfulValidationAt")
    public Instant lastSuccessfulValidationAt() {
        return lastSuccessfulValidationAt;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof LicenseState)) {
            return false;
        }
        LicenseState other = (LicenseState) obj;
        return java.util.Objects.equals(lastSuccessfulValidationAt, other.lastSuccessfulValidationAt);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(lastSuccessfulValidationAt);
    }

    @Override
    public String toString() {
        return "LicenseState[lastSuccessfulValidationAt=" + lastSuccessfulValidationAt + "]";
    }
}
