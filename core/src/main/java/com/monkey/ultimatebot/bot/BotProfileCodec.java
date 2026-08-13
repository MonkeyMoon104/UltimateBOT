package com.monkey.ultimatebot.bot;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.jspecify.annotations.Nullable;

final class BotProfileCodec {

    private BotProfileCodec() {}

    static String sanitizeName(@Nullable String candidate) {
        if (candidate == null || candidate.trim().isEmpty()) {
            return "CrystalBot";
        }

        String noSectionColors = candidate.replaceAll("(?i)\\u00A7[0-9A-FK-ORX]", "");
        String noAmpersandColors = noSectionColors.replaceAll("(?i)&[0-9A-FK-ORX]", "");
        String safe = noAmpersandColors.replaceAll("[^A-Za-z0-9_]", "_");
        if (safe.trim().isEmpty()) {
            safe = "CrystalBot";
        }
        return safe.length() > 16 ? safe.substring(0, 16) : safe;
    }

    static @Nullable String textureValueFromUrl(@Nullable String textureUrl) {
        if (textureUrl == null || textureUrl.trim().isEmpty()) {
            return null;
        }

        String payload = "{\"textures\":{\"SKIN\":{\"url\":\"" + textureUrl + "\"}}}";
        return Base64.getEncoder().encodeToString(payload.getBytes(StandardCharsets.UTF_8));
    }
}
