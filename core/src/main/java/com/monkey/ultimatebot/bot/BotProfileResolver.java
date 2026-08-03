package com.monkey.ultimatebot.bot;

import com.mojang.authlib.GameProfile;
import com.monkey.ultimatebot.api.model.identity.BotSkin;
import com.monkey.ultimatebot.api.model.identity.BotSkinSource;
import com.monkey.ultimatebot.placeholders.PlaceholderApiSupport;
import java.util.Locale;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

/** Resolves the protocol-safe name and skin profile used by a spawned bot. */
final class BotProfileResolver {

    GameProfile resolve(FileConfiguration config, Player owner, Player target, UUID botUUID, BotOptions options) {
        String template = resolveNameTemplate(config, options);
        String botName = resolveName(template, owner, target, options);
        return resolveProfile(owner, botUUID, botName, options);
    }

    private String resolveNameTemplate(FileConfiguration config, BotOptions options) {
        String configured = config.getString("bot.name", "CrystalBot");
        if (options == null || options.getCreationSource() != BotCreationSource.API) {
            return configured;
        }

        String customTemplate = options.getBotNameTemplate();
        return customTemplate == null || customTemplate.isBlank() ? configured : customTemplate;
    }

    private String resolveName(String nameTemplate, Player owner, Player target, BotOptions options) {
        String template = nameTemplate == null || nameTemplate.isBlank() ? "CrystalBot" : nameTemplate;
        Player firstOwner = resolveFirstTeamOwner(options);
        String firstOwnerName = firstOwner == null ? owner.getName() : firstOwner.getName();
        String ownersCount = options == null
                ? "1"
                : String.valueOf(Math.max(
                        1,
                        options.getTeamOwnerUUIDs().isEmpty()
                                ? 1
                                : options.getTeamOwnerUUIDs().size()));

        String replaced = template.replace("%player%", owner.getName())
                .replace("%owner%", owner.getName())
                .replace("%owner_name%", owner.getName())
                .replace("%target%", target == null ? owner.getName() : target.getName())
                .replace("%first_owner%", firstOwnerName)
                .replace("%owners_count%", ownersCount)
                .replace(
                        "%mode%",
                        options == null
                                ? BotType.SINGLE.name().toLowerCase(Locale.ROOT)
                                : options.getBotType().name().toLowerCase(Locale.ROOT));

        return BotProfileCodec.sanitizeName(PlaceholderApiSupport.apply(owner, replaced));
    }

    private GameProfile resolveProfile(Player owner, UUID botUUID, String botName, BotOptions options) {
        if (options == null || options.getCreationSource() != BotCreationSource.API) {
            return BotFactory.createProfile(owner, botUUID, botName);
        }

        BotSkin skin = options.getBotSkin();
        BotSkinSource source = skin == null ? null : skin.source();
        if (source == null) {
            return BotFactory.createProfile(owner, botUUID, botName);
        }

        return switch (source) {
            case RANDOM -> BotFactory.createRandomProfile(botUUID, botName);
            case OWNER -> BotFactory.createProfile(owner, botUUID, botName);
            case FIRST_TEAM_OWNER -> profileFromPlayerOrOwner(resolveFirstTeamOwner(options), owner, botUUID, botName);
            case PLAYER_REFERENCE ->
                profileFromPlayerOrOwner(
                        resolvePlayerReference(java.util.Objects.requireNonNull(
                                skin.playerReference(), "player-reference skin value")),
                        owner,
                        botUUID,
                        botName);
            case TEXTURE_VALUE ->
                BotFactory.createProfileWithTexture(
                        botUUID,
                        botName,
                        java.util.Objects.requireNonNull(skin.textureValue(), "texture skin value"),
                        skin.textureSignature());
            case TEXTURE_URL ->
                BotFactory.createProfileWithTexture(
                        botUUID,
                        botName,
                        BotProfileCodec.textureValueFromUrl(
                                java.util.Objects.requireNonNull(skin.textureUrl(), "texture skin URL")),
                        null);
        };
    }

    private GameProfile profileFromPlayerOrOwner(
            @Nullable Player candidate, Player owner, UUID botUUID, String botName) {
        Player source = candidate != null && candidate.isOnline() ? candidate : owner;
        return BotFactory.createProfile(source, botUUID, botName);
    }

    private @Nullable Player resolveFirstTeamOwner(@Nullable BotOptions options) {
        if (options == null || options.getBotType() != BotType.TEAM_ALLY) {
            return null;
        }
        for (UUID ownerUUID : options.getTeamOwnerUUIDs()) {
            Player owner = Bukkit.getPlayer(ownerUUID);
            if (owner != null && owner.isOnline()) {
                return owner;
            }
        }
        return null;
    }

    private @Nullable Player resolvePlayerReference(@Nullable String reference) {
        if (reference == null || reference.isBlank()) {
            return null;
        }

        Player exact = Bukkit.getPlayerExact(reference);
        if (exact != null && exact.isOnline()) {
            return exact;
        }

        try {
            Player byUuid = Bukkit.getPlayer(UUID.fromString(reference));
            if (byUuid != null && byUuid.isOnline()) {
                return byUuid;
            }
        } catch (IllegalArgumentException invalidUuid) {
            Bukkit.getLogger().finest(() -> "Skin player reference is not a UUID: " + reference);
        }

        Player fuzzy = Bukkit.getPlayer(reference);
        return fuzzy != null && fuzzy.isOnline() ? fuzzy : null;
    }
}
