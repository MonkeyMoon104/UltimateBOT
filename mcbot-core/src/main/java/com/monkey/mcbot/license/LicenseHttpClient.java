package com.monkey.mcbot.license;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public final class LicenseHttpClient {

    private static final String DEFAULT_BASE_URL = "https://license.monkeymoon104.it";
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final URI validateUri;
    private final URI heartbeatUri;

    public LicenseHttpClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        String baseUrl = System.getProperty(
                "mcbot.license.base-url",
                System.getenv().getOrDefault("MCBOT_LICENSE_BASE_URL", DEFAULT_BASE_URL)
        );
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
        try {
            HttpRequest httpRequest = HttpRequest.newBuilder(uri)
                    .timeout(Duration.ofSeconds(15))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(request)))
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new IOException("License endpoint returned HTTP " + response.statusCode() + " -> " + trimBody(response.body()));
            }
            return objectMapper.readValue(response.body(), LicenseValidationResponse.class);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while validating license", ex);
        }
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
