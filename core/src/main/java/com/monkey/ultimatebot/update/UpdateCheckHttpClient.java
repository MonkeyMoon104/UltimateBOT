package com.monkey.ultimatebot.update;

import com.monkey.ultimatebot.libs.jackson.databind.ObjectMapper;
import com.monkey.ultimatebot.common.net.CompatHttp;
import java.io.IOException;
import java.net.URI;

public final class UpdateCheckHttpClient {

    private static final String DEFAULT_BASE_URL = "https://license.monkeymoon104.it";
    private static final String USER_AGENT = "UltimateBot-UpdateClient";
    private static final int CONNECT_TIMEOUT_MS = 3_000;
    private static final int REQUEST_TIMEOUT_MS = 5_000;
    private final ObjectMapper objectMapper;
    private final URI checkUri;

    public UpdateCheckHttpClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;

        String baseUrl = System.getProperty(
                "ultimatebot.update.base-url",
                System.getenv().getOrDefault("ULTIMATEBOT_UPDATE_BASE_URL", DEFAULT_BASE_URL));
        this.checkUri = URI.create(baseUrl + "/api/v1/plugin/updates/check");
    }

    public PluginUpdateCheckResponse check(PluginUpdateCheckRequest request) throws IOException {
        byte[] payload = objectMapper.writeValueAsBytes(request);
        CompatHttp.Response response =
                CompatHttp.postJson(checkUri, payload, CONNECT_TIMEOUT_MS, REQUEST_TIMEOUT_MS, USER_AGENT);
        if (response.statusCode() != 200) {
            throw new IOException("Update endpoint returned HTTP " + response.statusCode());
        }
        return objectMapper.readValue(response.body(), PluginUpdateCheckResponse.class);
    }
}
