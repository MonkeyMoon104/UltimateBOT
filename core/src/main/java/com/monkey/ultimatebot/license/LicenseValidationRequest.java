package com.monkey.ultimatebot.license;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

public final class LicenseValidationRequest {
    private final String licenseKey;
    private final String product;
    private final String pluginVersion;
    private final String installationId;
    private final String fingerprintHash;
    private final String hostFingerprint;
    private final Integer serverPort;

    public LicenseValidationRequest(
            @JsonProperty("licenseKey") String licenseKey,
            @JsonProperty("product") String product,
            @JsonProperty("pluginVersion") String pluginVersion,
            @JsonProperty("installationId") String installationId,
            @JsonProperty("fingerprintHash") String fingerprintHash,
            @JsonProperty("hostFingerprint") String hostFingerprint,
            @JsonProperty("serverPort") Integer serverPort) {
        this.licenseKey = licenseKey;
        this.product = product;
        this.pluginVersion = pluginVersion;
        this.installationId = installationId;
        this.fingerprintHash = fingerprintHash;
        this.hostFingerprint = hostFingerprint;
        this.serverPort = serverPort;
    }

    @JsonProperty("licenseKey")
    public String licenseKey() {
        return licenseKey;
    }

    @JsonProperty("product")
    public String product() {
        return product;
    }

    @JsonProperty("pluginVersion")
    public String pluginVersion() {
        return pluginVersion;
    }

    @JsonProperty("installationId")
    public String installationId() {
        return installationId;
    }

    @JsonProperty("fingerprintHash")
    public String fingerprintHash() {
        return fingerprintHash;
    }

    @JsonProperty("hostFingerprint")
    public String hostFingerprint() {
        return hostFingerprint;
    }

    @JsonProperty("serverPort")
    public Integer serverPort() {
        return serverPort;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof LicenseValidationRequest)) {
            return false;
        }
        LicenseValidationRequest other = (LicenseValidationRequest) obj;
        return Objects.equals(licenseKey, other.licenseKey)
                && Objects.equals(product, other.product)
                && Objects.equals(pluginVersion, other.pluginVersion)
                && Objects.equals(installationId, other.installationId)
                && Objects.equals(fingerprintHash, other.fingerprintHash)
                && Objects.equals(hostFingerprint, other.hostFingerprint)
                && Objects.equals(serverPort, other.serverPort);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                licenseKey, product, pluginVersion, installationId, fingerprintHash, hostFingerprint, serverPort);
    }

    @Override
    public String toString() {
        return "LicenseValidationRequest[licenseKey="
                + licenseKey
                + ", product="
                + product
                + ", pluginVersion="
                + pluginVersion
                + ", installationId="
                + installationId
                + ", fingerprintHash="
                + fingerprintHash
                + ", hostFingerprint="
                + hostFingerprint
                + ", serverPort="
                + serverPort
                + "]";
    }
}
