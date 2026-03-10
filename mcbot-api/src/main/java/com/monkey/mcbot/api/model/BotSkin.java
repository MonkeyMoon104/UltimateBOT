package com.monkey.mcbot.api.model;

import java.net.URI;
import java.util.Objects;

/**
 * Skin/profile configuration for API-spawned bots.
 *
 * @param source skin resolution source
 * @param playerReference player name/UUID reference (for {@link BotSkinSource#PLAYER_REFERENCE})
 * @param textureValue Mojang texture value payload (for {@link BotSkinSource#TEXTURE_VALUE})
 * @param textureSignature optional Mojang texture signature for signed payloads
 * @param textureUrl texture URL (for {@link BotSkinSource#TEXTURE_URL})
 */
public record BotSkin(
        BotSkinSource source,
        String playerReference,
        String textureValue,
        String textureSignature,
        String textureUrl
) {
    /**
     * Canonical constructor with source-specific normalization and validation.
     *
     * <p>Unused fields are nulled according to the selected source. Required fields
     * are validated and may throw {@link IllegalArgumentException}.</p>
     */
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

    /**
     * Creates a random skin configuration.
     *
     * @return random skin config
     */
    public static BotSkin random() {
        return new BotSkin(BotSkinSource.RANDOM, null, null, null, null);
    }

    /**
     * Creates a skin configuration that uses the primary owner skin.
     *
     * @return owner skin config
     */
    public static BotSkin owner() {
        return new BotSkin(BotSkinSource.OWNER, null, null, null, null);
    }

    /**
     * Creates a TEAM_ALLY skin configuration that uses the first online team owner.
     *
     * @return first-team-owner skin config
     */
    public static BotSkin firstTeamOwner() {
        return new BotSkin(BotSkinSource.FIRST_TEAM_OWNER, null, null, null, null);
    }

    /**
     * Creates a skin configuration resolved from an online player reference.
     *
     * @param playerReference player name or UUID string
     * @return player-reference skin config
     */
    public static BotSkin player(String playerReference) {
        return new BotSkin(BotSkinSource.PLAYER_REFERENCE, playerReference, null, null, null);
    }

    /**
     * Creates a skin configuration from an unsigned texture payload.
     *
     * @param textureValue texture value payload
     * @return texture-value skin config
     */
    public static BotSkin texture(String textureValue) {
        return new BotSkin(BotSkinSource.TEXTURE_VALUE, null, textureValue, null, null);
    }

    /**
     * Creates a skin configuration from a signed texture payload.
     *
     * @param textureValue texture value payload
     * @param textureSignature texture signature payload
     * @return texture-value skin config
     */
    public static BotSkin texture(String textureValue, String textureSignature) {
        return new BotSkin(BotSkinSource.TEXTURE_VALUE, null, textureValue, textureSignature, null);
    }

    /**
     * Creates a skin configuration from a texture URL.
     *
     * @param textureUrl texture URL with {@code http} or {@code https} scheme
     * @return texture-url skin config
     */
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
