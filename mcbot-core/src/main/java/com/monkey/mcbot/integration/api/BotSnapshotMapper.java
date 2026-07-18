package com.monkey.mcbot.integration.api;

import com.monkey.mcbot.api.model.BotSnapshot;
import com.monkey.mcbot.api.model.BotSource;
import com.monkey.mcbot.bot.BotCreationSource;
import com.monkey.mcbot.bot.BotOptions;
import com.monkey.mcbot.bot.ai.ITrainingBot;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;

final class BotSnapshotMapper {

    private BotSnapshotMapper() {
    }

    static BotSnapshot toSnapshot(UUID ownerUUID, ITrainingBot bot) {
        if (bot == null) {
            return null;
        }

        UUID botUUID = bot.asPlayer() != null ? bot.asPlayer().getUUID() : null;

        String botType = "UNKNOWN";
        String botRank = "UNKNOWN";
        String minBotRank = "EASY";
        String maxBotRank = "GOD";
        int minTotemCount = -1;
        int maxTotemCount = -1;
        Set<UUID> targetUUIDs = Set.of();
        BotSource source = BotSource.CORE;
        boolean autoTarget = false;
        double autoTargetRange = 16.0D;
        boolean respectWorldGuardPvp = false;
        boolean stayAfterOwnerDeath = false;
        boolean crystalPvp = true;
        boolean enderPearls = true;
        boolean killMessageEnabled = true;
        if (bot.getBrainController() != null) {
            BotOptions options = bot.getBrainController().getBotOptions();
            if (options != null && options.getBotType() != null) {
                botType = options.getBotType().name();
                botRank = options.getRank().name();
                minBotRank = options.getMinRank().name();
                maxBotRank = options.getMaxRank().name();
                minTotemCount = options.getMinTotemCount();
                maxTotemCount = options.getMaxTotemCount();
                targetUUIDs = options.getTargetUUIDs();
                source = options.getCreationSource() == BotCreationSource.API
                        ? BotSource.API
                        : BotSource.CORE;
                autoTarget = options.isAutoTarget();
                autoTargetRange = options.getAutoTargetRange();
                respectWorldGuardPvp = options.isRespectWorldGuardPvp();
                stayAfterOwnerDeath = options.isStayAfterOwnerDeath();
                crystalPvp = options.isCrystalPvp();
                enderPearls = options.isEnderPearls();
                killMessageEnabled = options.isKillMessageEnabled();
            }
        }

        Player target = bot.getTargetPlayer();
        UUID targetUUID = target != null ? target.getUniqueId() : null;

        return new BotSnapshot(
                ownerUUID,
                botUUID,
                botType,
                botRank,
                minBotRank,
                maxBotRank,
                bot.isFollow(),
                bot.isCombat(),
                bot.getTotemCount(),
                minTotemCount,
                maxTotemCount,
                targetUUID,
                targetUUIDs,
                source,
                autoTarget,
                autoTargetRange,
                respectWorldGuardPvp,
                stayAfterOwnerDeath,
                crystalPvp,
                enderPearls,
                killMessageEnabled
        );
    }
}
