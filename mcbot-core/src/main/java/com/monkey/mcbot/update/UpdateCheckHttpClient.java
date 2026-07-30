package com.monkey.mcbot.update;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public final class UpdateCheckHttpClient {

    private static final String DEFAULT_BASE_URL = "https://license.monkeymoon104.it";
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final URI checkUri;

    public UpdateCheckHttpClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient =
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();

        String baseUrl = System.getProperty(
                "mcbot.update.base-url",
                System.getenv()
                        .getOrDefault(
                                "MCBOT_UPDATE_BASE_URL",
                                System.getProperty(
                                        "mcbot.license.base-url",
                                        System.getenv().getOrDefault("MCBOT_LICENSE_BASE_URL", DEFAULT_BASE_URL))));
        this.checkUri = URI.create(baseUrl + "/api/v1/plugin/updates/check");
    }

    public PluginUpdateCheckResponse check(PluginUpdateCheckRequest request) throws IOException {
        try {
            HttpRequest httpRequest = HttpRequest.newBuilder(checkUri)
                    .timeout(Duration.ofSeconds(5))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(request)))
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new IOException("Update endpoint returned HTTP " + response.statusCode());
            }
            return objectMapper.readValue(response.body(), PluginUpdateCheckResponse.class);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while checking updates", ex);
        }
    }
}
