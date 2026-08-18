package com.monkey.ultimatebot.integration.api.internal;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.access.entity.EntityLookupAccess;
import com.monkey.ultimatebot.api.model.configuration.BotSettings;
import com.monkey.ultimatebot.api.model.identity.BotSkin;
import com.monkey.ultimatebot.api.model.identity.BotSkinSource;
import com.monkey.ultimatebot.api.model.runtime.BotOperationResult;
import com.monkey.ultimatebot.bot.BotRegistry;
import com.monkey.ultimatebot.bot.BotType;
import com.monkey.ultimatebot.common.model.BotArmorTier;
import com.monkey.ultimatebot.common.model.DifficultyTier;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import org.bukkit.Bukkit;
import org.jspecify.annotations.Nullable;

public final class CoreBotSpawnSupport {

    private final UltimateBot plugin;
    private final BotRegistry botRegistry;
    private final Function<String, Optional<UUID>> parsePlayerReference;

    public CoreBotSpawnSupport(
            UltimateBot plugin, BotRegistry botRegistry, Function<String, Optional<UUID>> parsePlayerReference) {
        this.plugin = plugin;
        this.botRegistry = botRegistry;
        this.parsePlayerReference = parsePlayerReference;
    }

    public BotSettings defaultSettings() {
        return BotSettings.builder()
                .setBotNameTemplate(plugin.getConfig().getString("bot.name", "CrystalBot"))
                .setBotSkinOwner()
                .follow(false)
                .setChangeableFollow(true)
                .combat(false)
                .setChangeableCombat(true)
                .blastProtection(0, 0, 0, 0)
                .setChangeableBlast(true)
                .armorValue(BotArmorTier.LEATHER, BotArmorTier.NETHERITE)
                .armor(BotArmorTier.NETHERITE)
                .setChangeableArmor(true)
                .totemValue(-1, Integer.MAX_VALUE)
                .totemCount(-1)
                .setChangeableTotem(true)
                .difficultyValue(DifficultyTier.EASY, DifficultyTier.GOD)
                .difficulty(DifficultyTier.EASY)
                .setChangeableDifficulty(true)
                .healing(plugin.getConfig().getBoolean("bot.combat.healing", true))
                .build();
    }

    public @Nullable String validateRequestedBotUUID(@Nullable UUID requestedBotUUID) {
        if (requestedBotUUID == null) {
            return null;
        }
        if (requestedBotUUID.equals(new UUID(0L, 0L))) {
            return "Bot UUID cannot be the nil UUID.";
        }
        if (botRegistry.getOwnerUUIDByBotUUID(requestedBotUUID) != null) {
            return "Bot UUID is already assigned to an active UltimateBot.";
        }
        if (Bukkit.getPlayer(requestedBotUUID) != null || EntityLookupAccess.get(requestedBotUUID) != null) {
            return "Bot UUID is already used by a loaded player or entity.";
        }
        return null;
    }

    public @Nullable String validateSkinForMode(BotType botType, @Nullable BotSettings settings) {
        if (settings == null || settings.botSkin() == null || settings.botSkin().source() == null) {
            return null;
        }

        BotSkin skin = settings.botSkin();
        BotSkinSource source = skin.source();

        if (source == BotSkinSource.FIRST_TEAM_OWNER && botType != BotType.TEAM_ALLY) {
            return "BotSkin.firstTeamOwner() is valid only for TEAM_ALLY mode.";
        }

        if (source == BotSkinSource.PLAYER_REFERENCE) {
            String reference = skin.playerReference();
            if (reference == null || reference.trim().isEmpty() || !parsePlayerReference.apply(reference).isPresent()) {
                return "BotSkin.player(\"...\") requires an online valid player reference.";
            }
        }

        return null;
    }

    public BotOperationResult spawnFailure(@Nullable String reason) {
        return BotOperationResult.failure(reason != null ? reason : "Bot operation failed.");
    }
}
