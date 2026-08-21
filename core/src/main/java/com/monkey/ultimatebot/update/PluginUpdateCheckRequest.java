package com.monkey.ultimatebot.update;

import com.monkey.ultimatebot.libs.jackson.annotation.JsonProperty;
import java.util.Objects;

public final class PluginUpdateCheckRequest {
    private final String product;
    private final String currentVersion;

    public PluginUpdateCheckRequest(
            @JsonProperty("product") String product, @JsonProperty("currentVersion") String currentVersion) {
        this.product = product;
        this.currentVersion = currentVersion;
    }

    @JsonProperty("product")
    public String product() {
        return product;
    }

    @JsonProperty("currentVersion")
    public String currentVersion() {
        return currentVersion;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof PluginUpdateCheckRequest)) {
            return false;
        }
        PluginUpdateCheckRequest other = (PluginUpdateCheckRequest) obj;
        return Objects.equals(product, other.product) && Objects.equals(currentVersion, other.currentVersion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(product, currentVersion);
    }

    @Override
    public String toString() {
        return "PluginUpdateCheckRequest[product=" + product + ", currentVersion=" + currentVersion + "]";
    }
}
