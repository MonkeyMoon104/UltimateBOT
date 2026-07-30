package com.monkey.mcbot.update;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PluginUpdateCheckResponse(
        @JsonProperty("updateAvailable") boolean updateAvailable,
        @JsonProperty("product") String product,
        @JsonProperty("currentVersion") String currentVersion,
        @JsonProperty("latestVersion") String latestVersion,
        @JsonProperty("downloadUrl") String downloadUrl,
        @JsonProperty("message") String message) {}
