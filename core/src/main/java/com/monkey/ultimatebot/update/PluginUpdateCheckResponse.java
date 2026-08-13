package com.monkey.ultimatebot.update;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class PluginUpdateCheckResponse {
    private final boolean updateAvailable;
    private final String product;
    private final String currentVersion;
    private final String latestVersion;
    private final String downloadUrl;
    private final String message;

    public PluginUpdateCheckResponse(
            @JsonProperty("updateAvailable") boolean updateAvailable,
            @JsonProperty("product") String product,
            @JsonProperty("currentVersion") String currentVersion,
            @JsonProperty("latestVersion") String latestVersion,
            @JsonProperty("downloadUrl") String downloadUrl,
            @JsonProperty("message") String message) {
        this.updateAvailable = updateAvailable;
        this.product = product;
        this.currentVersion = currentVersion;
        this.latestVersion = latestVersion;
        this.downloadUrl = downloadUrl;
        this.message = message;
    }

    @JsonProperty("updateAvailable")
    public boolean updateAvailable() {
        return updateAvailable;
    }

    @JsonProperty("product")
    public String product() {
        return product;
    }

    @JsonProperty("currentVersion")
    public String currentVersion() {
        return currentVersion;
    }

    @JsonProperty("latestVersion")
    public String latestVersion() {
        return latestVersion;
    }

    @JsonProperty("downloadUrl")
    public String downloadUrl() {
        return downloadUrl;
    }

    @JsonProperty("message")
    public String message() {
        return message;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof PluginUpdateCheckResponse)) {
            return false;
        }
        PluginUpdateCheckResponse other = (PluginUpdateCheckResponse) obj;
        return updateAvailable == other.updateAvailable
                && Objects.equals(product, other.product)
                && Objects.equals(currentVersion, other.currentVersion)
                && Objects.equals(latestVersion, other.latestVersion)
                && Objects.equals(downloadUrl, other.downloadUrl)
                && Objects.equals(message, other.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(updateAvailable, product, currentVersion, latestVersion, downloadUrl, message);
    }

    @Override
    public String toString() {
        return "PluginUpdateCheckResponse[updateAvailable="
                + updateAvailable
                + ", product="
                + product
                + ", currentVersion="
                + currentVersion
                + ", latestVersion="
                + latestVersion
                + ", downloadUrl="
                + downloadUrl
                + ", message="
                + message
                + "]";
    }
}
