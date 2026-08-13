package com.monkey.ultimatebot.license;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.monkey.ultimatebot.common.net.CompatHttp;
import java.io.IOException;
import java.net.URI;
import java.util.Objects;

public final class LicenseHttpClient {

    private static final String DEFAULT_BASE_URL = "https://license.monkeymoon104.it";
    private static final String USER_AGENT = "UltimateBot-LicenseClient";
    private static final int MAX_ATTEMPTS = 2;
    private static final int CONNECT_TIMEOUT_MS = 2_000;
    private static final int REQUEST_TIMEOUT_MS = 3_000;
    private final ObjectMapper objectMapper;
    private final URI validateUri;
    private final URI heartbeatUri;

    public LicenseHttpClient(ObjectMapper objectMapper) {
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper");

        String baseUrl = System.getProperty(
                "ultimatebot.license.base-url",
                System.getenv().getOrDefault("ULTIMATEBOT_LICENSE_BASE_URL", DEFAULT_BASE_URL));
        this.validateUri = URI.create(baseUrl + "/api/v1/plugin/validate");
        this.heartbeatUri = URI.create(baseUrl + "/api/v1/plugin/heartbeat");
    }

    public LicenseValidationResponse validate(LicenseValidationRequest request) throws IOException {
        return send(validateUri, request);
    }

    public LicenseValidationResponse heartbeat(LicenseValidationRequest request) throws IOException {
        return send(heartbeatUri, request);
    }

    private LicenseValidationResponse send(URI uri, LicenseValidationRequest request) throws IOException {
        IOException firstFailure = null;
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                return sendOnce(uri, request);
            } catch (IOException error) {
                if (attempt == MAX_ATTEMPTS || !CompatHttp.isRetryableNetworkFailure(error)) {
                    if (firstFailure != null && !firstFailure.equals(error)) {
                        error.addSuppressed(firstFailure);
                    }
                    throw error;
                }
                firstFailure = error;
            }
        }
        throw new IllegalStateException("License request retry loop completed unexpectedly");
    }

    private LicenseValidationResponse sendOnce(URI uri, LicenseValidationRequest request) throws IOException {
        byte[] payload = objectMapper.writeValueAsBytes(request);
        CompatHttp.Response response =
                CompatHttp.postJson(uri, payload, CONNECT_TIMEOUT_MS, REQUEST_TIMEOUT_MS, USER_AGENT);
        if (response.statusCode() != 200) {
            throw new IOException(
                    "License endpoint returned HTTP " + response.statusCode() + " -> " + trimBody(response.body()));
        }
        return objectMapper.readValue(response.body(), LicenseValidationResponse.class);
    }

    private String trimBody(String body) {
        if (body == null || body.trim().isEmpty()) {
            return "empty response";
        }
        String normalized = body.replace('\n', ' ').replace('\r', ' ').trim();
        if (normalized.length() <= 500) {
            return normalized;
        }
        return normalized.substring(0, 500) + "...";
    }
}
