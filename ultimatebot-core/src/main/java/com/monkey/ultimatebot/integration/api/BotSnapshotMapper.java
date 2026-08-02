package com.monkey.ultimatebot.integration.api;

import com.monkey.ultimatebot.api.model.BotSnapshot;
import com.monkey.ultimatebot.api.model.BotSource;
import com.monkey.ultimatebot.api.model.BotTargetMode;
import com.monkey.ultimatebot.bot.BotOptions;
import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.common.model.CombatMode;
import com.monkey.ultimatebot.common.model.CombatTuning;
import java.util.Set;
import java.util.UUID;

public final class BotSnapshotMapper {

    private BotSnapshotMapper() {}

    public static BotSnapshot toSnapshot(UUID ownerUUID, ITrainingBot bot) {
        if (bot == null) {
            return null;
        }

        UUID botUUID = bot.asPlayer() != null ? bot.asPlayer().getUUID() : null;

        String botType = "UNKNOWN";
        String botDifficulty = "UNKNOWN";
        String minDifficultyLevel = "EASY";
        String maxDifficultyLevel = "GOD";
        CombatMode combatMode = CombatMode.SWORD;
        CombatTuning combatTuning = CombatTuning.builder().build();
        boolean customizedCombatTuning = false;
        int minTotemCount = -1;
        int maxTotemCount = -1;
        Set<UUID> targetUUIDs = Set.of();
        BotSource source = BotSource.CORE;
        boolean autoTarget = false;
        double autoTargetRange = 16.0D;
        boolean attackBots = false;
        BotTargetMode targetMode = BotTargetMode.PLAYERS;
        boolean respectWorldGuardPvp = false;
        boolean stayAfterOwnerDeath = false;
        boolean idleWander = false;
        double idleWanderRadius = 10.0D;
        double idleReturnDistance = 24.0D;
        long idleReturnDelayMs = 8000L;
        boolean crystalPvp = true;
        boolean explosions = true;
        boolean explosionBlockDamage = false;
        boolean enderPearls = true;
        boolean healing = true;
        boolean killMessageEnabled = true;
        if (bot.getBrainController() != null) {
            BotOptions options = bot.getBrainController().getBotOptions();
            if (options != null && options.getBotType() != null) {
                botType = options.getBotType().name();
                botDifficulty = options.getDifficulty().name();
                minDifficultyLevel = options.getMinDifficulty().name();
                maxDifficultyLevel = options.getMaxDifficulty().name();
                combatMode = options.getCombatMode();
                combatTuning = options.getCombatTuning();
                customizedCombatTuning = options.getCustomCombatTuning() != null;
                minTotemCount = options.getMinTotemCount();
                maxTotemCount = options.getMaxTotemCount();
                targetUUIDs = options.getTargetUUIDs();
                source = BotSource.fromCommon(options.getCreationSource().toCommon());
                autoTarget = options.isAutoTarget();
                autoTargetRange = options.getAutoTargetRange();
                attackBots = options.isAttackBots();
                targetMode = options.getTargetMode();
                respectWorldGuardPvp = options.isRespectWorldGuardPvp();
                stayAfterOwnerDeath = options.isStayAfterOwnerDeath();
                idleWander = options.isIdleWander();
                idleWanderRadius = options.getIdleWanderRadius();
                idleReturnDistance = options.getIdleReturnDistance();
                idleReturnDelayMs = options.getIdleReturnDelayMs();
                crystalPvp = options.isCrystalPvp();
                explosions = options.isExplosions();
                explosionBlockDamage = options.isExplosionBlockDamage();
                enderPearls = options.isEnderPearls();
                healing = options.isHealing();
                killMessageEnabled = options.isKillMessageEnabled();
            }
        }

        org.bukkit.entity.LivingEntity target = bot.getBrainController() == null
                ? bot.getTargetPlayer()
                : bot.getBrainController().getActiveTarget();
        UUID targetUUID = target != null ? target.getUniqueId() : null;

        return new BotSnapshot(
                ownerUUID,
                botUUID,
                botType,
                botDifficulty,
                minDifficultyLevel,
                maxDifficultyLevel,
                combatMode,
                combatTuning,
                customizedCombatTuning,
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
                attackBots,
                targetMode,
                respectWorldGuardPvp,
                stayAfterOwnerDeath,
                idleWander,
                idleWanderRadius,
                idleReturnDistance,
                idleReturnDelayMs,
                crystalPvp,
                explosions,
                explosionBlockDamage,
                enderPearls,
                healing,
                killMessageEnabled);
    }
}
