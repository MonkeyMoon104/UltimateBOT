package com.monkey.mcbot.api.model;

import java.net.URI;
import java.util.Objects;

public record BotSkin(
        BotSkinSource source,
        String playerReference,
        String textureValue,
        String textureSignature,
        String textureUrl
) {
    public BotSkin {
        source = Objects.requireNonNull(source, "source");

        playerReference = trimToNull(playerReference);
        textureValue = trimToNull(textureValue);
        textureSignature = trimToNull(textureSignature);
        textureUrl = trimToNull(textureUrl);

        switch (source) {
            case RANDOM, OWNER, FIRST_TEAM_OWNER -> {
                playerReference = null;
                textureValue = null;
                textureSignature = null;
                textureUrl = null;
            }
            case PLAYER_REFERENCE -> {
                if (playerReference == null) {
                    throw new IllegalArgumentException("playerReference is required for PLAYER_REFERENCE skin source");
                }
                textureValue = null;
                textureSignature = null;
                textureUrl = null;
            }
            case TEXTURE_VALUE -> {
                if (textureValue == null) {
                    throw new IllegalArgumentException("textureValue is required for TEXTURE_VALUE skin source");
                }
                playerReference = null;
                textureUrl = null;
            }
            case TEXTURE_URL -> {
                if (textureUrl == null) {
                    throw new IllegalArgumentException("textureUrl is required for TEXTURE_URL skin source");
                }
                validateUrl(textureUrl);
                playerReference = null;
                textureValue = null;
                textureSignature = null;
            }
        }
    }

    public static BotSkin random() {
        return new BotSkin(BotSkinSource.RANDOM, null, null, null, null);
    }

    public static BotSkin owner() {
        return new BotSkin(BotSkinSource.OWNER, null, null, null, null);
    }

    /**
     * Valid only for TEAM_ALLY mode.
     */
    public static BotSkin firstTeamOwner() {
        return new BotSkin(BotSkinSource.FIRST_TEAM_OWNER, null, null, null, null);
    }

    public static BotSkin player(String playerReference) {
        return new BotSkin(BotSkinSource.PLAYER_REFERENCE, playerReference, null, null, null);
    }

    public static BotSkin texture(String textureValue) {
        return new BotSkin(BotSkinSource.TEXTURE_VALUE, null, textureValue, null, null);
    }

    public static BotSkin texture(String textureValue, String textureSignature) {
        return new BotSkin(BotSkinSource.TEXTURE_VALUE, null, textureValue, textureSignature, null);
    }

    public static BotSkin url(String textureUrl) {
        return new BotSkin(BotSkinSource.TEXTURE_URL, null, null, null, textureUrl);
    }

    private static void validateUrl(String textureUrl) {
        try {
            URI uri = URI.create(textureUrl);
            String scheme = uri.getScheme();
            if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
                throw new IllegalArgumentException("textureUrl must use http/https");
            }
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("textureUrl is not a valid URL", ex);
        }
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
