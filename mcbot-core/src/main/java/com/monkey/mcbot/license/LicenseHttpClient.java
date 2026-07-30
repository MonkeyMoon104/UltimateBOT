package com.monkey.mcbot.license;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.Objects;

public final class LicenseHttpClient {

    private static final String DEFAULT_BASE_URL = "https://license.monkeymoon104.it";
    private static final int MAX_ATTEMPTS = 2;
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(2);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(3);
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final URI validateUri;
    private final URI heartbeatUri;

    public LicenseHttpClient(ObjectMapper objectMapper) {
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper");
        this.httpClient =
                HttpClient.newBuilder().connectTimeout(CONNECT_TIMEOUT).build();

        String baseUrl = System.getProperty(
                "mcbot.license.base-url", System.getenv().getOrDefault("MCBOT_LICENSE_BASE_URL", DEFAULT_BASE_URL));
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
                if (attempt == MAX_ATTEMPTS || !isRetryableNetworkFailure(error)) {
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
        try {
            HttpRequest httpRequest = HttpRequest.newBuilder(uri)
                    .timeout(REQUEST_TIMEOUT)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(request)))
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new IOException(
                        "License endpoint returned HTTP " + response.statusCode() + " -> " + trimBody(response.body()));
            }
            return objectMapper.readValue(response.body(), LicenseValidationResponse.class);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while validating license", ex);
        }
    }

    private static boolean isRetryableNetworkFailure(IOException error) {
        Throwable cause = error;
        while (cause != null) {
            if (cause instanceof HttpTimeoutException
                    || cause instanceof ConnectException
                    || cause instanceof UnknownHostException) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }

    private String trimBody(String body) {
        if (body == null || body.isBlank()) {
            return "empty response";
        }
        String normalized = body.replace('\n', ' ').replace('\r', ' ').trim();
        if (normalized.length() <= 500) {
            return normalized;
        }
        return normalized.substring(0, 500) + "...";
    }
}
