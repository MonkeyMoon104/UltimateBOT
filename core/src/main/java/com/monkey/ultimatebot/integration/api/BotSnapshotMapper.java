package com.monkey.ultimatebot.integration.api;

import com.monkey.ultimatebot.api.model.configuration.BotEquipmentSlot;
import com.monkey.ultimatebot.api.model.configuration.BotEquipmentSlotSetting;
import com.monkey.ultimatebot.api.model.runtime.BotLocation;
import com.monkey.ultimatebot.api.model.runtime.BotSnapshot;
import com.monkey.ultimatebot.bot.BotOptions;
import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.common.model.settings.BlastProtectionSettings;
import com.monkey.ultimatebot.common.model.bot.BotArmorTier;
import com.monkey.ultimatebot.common.model.bot.BotMode;
import com.monkey.ultimatebot.common.model.bot.BotSource;
import com.monkey.ultimatebot.common.model.bot.BotTargetMode;
import com.monkey.ultimatebot.common.model.brain.BrainKey;
import com.monkey.ultimatebot.common.model.combat.CombatMode;
import com.monkey.ultimatebot.common.model.combat.CombatTuning;
import com.monkey.ultimatebot.common.model.combat.DifficultyTier;
import com.monkey.ultimatebot.common.model.bot.EquipmentSlotKind;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

public final class BotSnapshotMapper {

    private BotSnapshotMapper() {}

    public static @Nullable BotSnapshot toSnapshot(UUID ownerUUID, @Nullable ITrainingBot bot) {
        if (bot == null) {
            return null;
        }

        UUID botUUID = bot.asBukkitPlayer() != null ? bot.asBukkitPlayer().getUniqueId() : null;

        BotMode botMode = BotMode.SINGLE;
        DifficultyTier difficulty = DifficultyTier.EASY;
        DifficultyTier minDifficulty = DifficultyTier.EASY;
        DifficultyTier maxDifficulty = DifficultyTier.GOD;
        CombatMode combatMode = CombatMode.SWORD;
        BrainKey brain = null;
        CombatTuning combatTuning = CombatTuning.builder().build();
        boolean customizedCombatTuning = false;
        int minTotemCount = -1;
        int maxTotemCount = -1;
        Set<UUID> targetUUIDs = Collections.emptySet();
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
        Set<UUID> teamOwnerUUIDs = Collections.emptySet();
        String botNameTemplate = "UltimateBot";
        String botSkinSource = "RANDOM";
        BotArmorTier minArmor = BotArmorTier.LEATHER;
        BotArmorTier maxArmor = BotArmorTier.NETHERITE;
        BotArmorTier armor = BotArmorTier.LEATHER;
        BlastProtectionSettings blastProtection = BlastProtectionSettings.all(false);
        boolean changeableFollow = true;
        boolean changeableCombat = true;
        boolean changeableBlast = true;
        boolean changeableArmor = true;
        boolean changeableTotem = true;
        boolean changeableDifficulty = true;
        boolean changeableCombatMode = true;
        BotLocation spawnLocation = null;
        String killMessage = null;
        Map<BotEquipmentSlot, BotEquipmentSlotSetting> equipmentSlots = Collections.emptyMap();
        if (bot.getBrainController() != null) {
            BotOptions options = bot.getBrainController().getBotOptions();
            if (options != null && options.getBotType() != null) {
                botMode = options.getBotType().toCommon();
                difficulty = DifficultyTier.valueOf(options.getDifficulty().name());
                minDifficulty =
                        DifficultyTier.valueOf(options.getMinDifficulty().name());
                maxDifficulty =
                        DifficultyTier.valueOf(options.getMaxDifficulty().name());
                combatMode = options.getCombatMode();
                brain = options.getBrainKey();
                combatTuning = options.getCombatTuning();
                customizedCombatTuning = options.getCustomCombatTuning() != null;
                minTotemCount = options.getMinTotemCount();
                maxTotemCount = options.getMaxTotemCount();
                targetUUIDs = options.getTargetUUIDs();
                source = options.getCreationSource().toCommon();
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
                teamOwnerUUIDs = options.getTeamOwnerUUIDs();
                botNameTemplate = options.getBotNameTemplate() != null ? options.getBotNameTemplate() : "UltimateBot";
                botSkinSource = options.getBotSkin().source().name();
                minArmor = BotArmorTier.valueOf(options.getMinArmorTier().name());
                maxArmor = BotArmorTier.valueOf(options.getMaxArmorTier().name());
                org.bukkit.inventory.ItemStack chestplate = options.getArmor().get(EquipmentSlotKind.CHEST);
                com.monkey.ultimatebot.utils.armor.ArmorTier currentArmor = chestplate == null
                        ? null
                        : com.monkey.ultimatebot.utils.armor.ArmorTier.fromMaterial(
                                chestplate.getType(), EquipmentSlotKind.CHEST);
                armor = currentArmor == null ? minArmor : BotArmorTier.valueOf(currentArmor.name());
                blastProtection = new BlastProtectionSettings(
                        options.getBlast().getOrDefault(EquipmentSlotKind.FEET, false),
                        options.getBlast().getOrDefault(EquipmentSlotKind.LEGS, false),
                        options.getBlast().getOrDefault(EquipmentSlotKind.CHEST, false),
                        options.getBlast().getOrDefault(EquipmentSlotKind.HEAD, false));
                changeableFollow = options.isChangeableFollow();
                changeableCombat = options.isChangeableCombat();
                changeableBlast = options.isChangeableBlast();
                changeableArmor = options.isChangeableArmor();
                changeableTotem = options.isChangeableTotem();
                changeableDifficulty = options.isChangeableDifficulty();
                changeableCombatMode = options.isChangeableCombatMode();
                spawnLocation = options.getSpawnLocation();
                killMessage = options.getCustomKillMessage();
                equipmentSlots = options.getEquipmentSlotSettings();
            }
        }

        org.bukkit.entity.LivingEntity target = bot.getBrainController() == null
                ? bot.getTargetPlayer()
                : bot.getBrainController().getActiveTarget();
        UUID targetUUID = target != null ? target.getUniqueId() : null;

        return new BotSnapshot(
                ownerUUID,
                botUUID,
                botMode,
                difficulty,
                minDifficulty,
                maxDifficulty,
                combatMode,
                brain,
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
                killMessageEnabled,
                teamOwnerUUIDs,
                botNameTemplate,
                botSkinSource,
                minArmor,
                maxArmor,
                armor,
                blastProtection,
                changeableFollow,
                changeableCombat,
                changeableBlast,
                changeableArmor,
                changeableTotem,
                changeableDifficulty,
                changeableCombatMode,
                spawnLocation,
                killMessage,
                equipmentSlots);
    }
}
