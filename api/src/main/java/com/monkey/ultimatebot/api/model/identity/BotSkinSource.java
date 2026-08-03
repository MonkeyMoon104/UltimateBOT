package com.monkey.ultimatebot.api.model.identity;

/**
 * Supported strategies for resolving bot skin/profile texture.
 */
public enum BotSkinSource {
    /** Use a random profile/skin. */
    RANDOM,
    /** Use the single owner's skin. */
    OWNER,
    /** Use the first online team owner skin (TEAM_ALLY only). */
    FIRST_TEAM_OWNER,
    /** Resolve skin from a provided online player reference. */
    PLAYER_REFERENCE,
    /** Use explicit Mojang texture value/signature payload. */
    TEXTURE_VALUE,
    /** Use a texture URL that will be converted to texture payload. */
    TEXTURE_URL
}
