package com.monkey.ultimatebot.api.model.identity;

import com.monkey.ultimatebot.common.util.TextValues;
import java.net.URI;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * Skin/profile configuration for API-spawned bots.
 *
 * @param source skin resolution source
 * @param playerReference player name/UUID reference (for {@link BotSkinSource#PLAYER_REFERENCE})
 * @param textureValue Mojang texture value payload (for {@link BotSkinSource#TEXTURE_VALUE})
 * @param textureSignature optional Mojang texture signature for signed payloads
 * @param textureUrl texture URL (for {@link BotSkinSource#TEXTURE_URL})
 */
public final class BotSkin {
    private final BotSkinSource source;
    private final @Nullable String playerReference;
    private final @Nullable String textureValue;
    private final @Nullable String textureSignature;
    private final @Nullable String textureUrl;

    public BotSkin(
            BotSkinSource source,
            @Nullable String playerReference,
            @Nullable String textureValue,
            @Nullable String textureSignature,
            @Nullable String textureUrl) {
        Objects.requireNonNull(source, "source");

        String normalizedPlayerReference = TextValues.trimToNull(playerReference);
        String normalizedTextureValue = TextValues.trimToNull(textureValue);
        String normalizedTextureSignature = TextValues.trimToNull(textureSignature);
        String normalizedTextureUrl = TextValues.trimToNull(textureUrl);

        switch (source) {
            case RANDOM:
            case OWNER:
            case FIRST_TEAM_OWNER:
                normalizedPlayerReference = null;
                normalizedTextureValue = null;
                normalizedTextureSignature = null;
                normalizedTextureUrl = null;
                break;
            case PLAYER_REFERENCE:
                if (normalizedPlayerReference == null) {
                    throw new IllegalArgumentException(
                            "playerReference is required for PLAYER_REFERENCE skin source");
                }
                normalizedTextureValue = null;
                normalizedTextureSignature = null;
                normalizedTextureUrl = null;
                break;
            case TEXTURE_VALUE:
                if (normalizedTextureValue == null) {
                    throw new IllegalArgumentException(
                            "textureValue is required for TEXTURE_VALUE skin source");
                }
                normalizedPlayerReference = null;
                normalizedTextureUrl = null;
                break;
            case TEXTURE_URL:
                if (normalizedTextureUrl == null) {
                    throw new IllegalArgumentException("textureUrl is required for TEXTURE_URL skin source");
                }
                validateUrl(normalizedTextureUrl);
                normalizedPlayerReference = null;
                normalizedTextureValue = null;
                normalizedTextureSignature = null;
                break;
            default:
                throw new IllegalStateException("Unexpected skin source: " + source);
        }

        this.source = source;
        this.playerReference = normalizedPlayerReference;
        this.textureValue = normalizedTextureValue;
        this.textureSignature = normalizedTextureSignature;
        this.textureUrl = normalizedTextureUrl;
    }

    public BotSkinSource source() {
        return source;
    }

    public @Nullable String playerReference() {
        return playerReference;
    }

    public @Nullable String textureValue() {
        return textureValue;
    }

    public @Nullable String textureSignature() {
        return textureSignature;
    }

    public @Nullable String textureUrl() {
        return textureUrl;
    }

    /** Creates a random skin configuration. */
    public static BotSkin random() {
        return new BotSkin(BotSkinSource.RANDOM, null, null, null, null);
    }

    /** Creates a skin configuration that uses the primary owner skin. */
    public static BotSkin owner() {
        return new BotSkin(BotSkinSource.OWNER, null, null, null, null);
    }

    /** Creates a TEAM_ALLY skin configuration that uses the first online team owner. */
    public static BotSkin firstTeamOwner() {
        return new BotSkin(BotSkinSource.FIRST_TEAM_OWNER, null, null, null, null);
    }

    /** Creates a skin configuration resolved from an online player reference. */
    public static BotSkin player(String playerReference) {
        return new BotSkin(BotSkinSource.PLAYER_REFERENCE, playerReference, null, null, null);
    }

    /** Creates a skin configuration from an unsigned texture payload. */
    public static BotSkin texture(String textureValue) {
        return new BotSkin(BotSkinSource.TEXTURE_VALUE, null, textureValue, null, null);
    }

    /** Creates a skin configuration from a signed texture payload. */
    public static BotSkin texture(String textureValue, String textureSignature) {
        return new BotSkin(BotSkinSource.TEXTURE_VALUE, null, textureValue, textureSignature, null);
    }

    /** Creates a skin configuration from a texture URL. */
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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof BotSkin)) {
            return false;
        }
        BotSkin other = (BotSkin) obj;
        return Objects.equals(source, other.source)
                && Objects.equals(playerReference, other.playerReference)
                && Objects.equals(textureValue, other.textureValue)
                && Objects.equals(textureSignature, other.textureSignature)
                && Objects.equals(textureUrl, other.textureUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, playerReference, textureValue, textureSignature, textureUrl);
    }

    @Override
    public String toString() {
        return "BotSkin[source="
                + source
                + ", playerReference="
                + playerReference
                + ", textureValue="
                + textureValue
                + ", textureSignature="
                + textureSignature
                + ", textureUrl="
                + textureUrl
                + "]";
    }
}
