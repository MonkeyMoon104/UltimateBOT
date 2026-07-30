package com.monkey.mcbot.update;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PluginUpdateCheckRequest(
        @JsonProperty("product") String product,
        @JsonProperty("currentVersion") String currentVersion) {}
