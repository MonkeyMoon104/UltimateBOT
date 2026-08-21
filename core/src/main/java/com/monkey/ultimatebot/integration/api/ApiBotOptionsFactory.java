package com.monkey.ultimatebot.integration.api;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.api.model.configuration.BotBlastProtection;
import com.monkey.ultimatebot.api.model.configuration.BotEquipmentSlot;
import com.monkey.ultimatebot.api.model.configuration.BotEquipmentSlotSetting;
import com.monkey.ultimatebot.api.model.configuration.BotSettings;
import com.monkey.ultimatebot.bot.BotCreationSource;
import com.monkey.ultimatebot.bot.BotOptions;
import com.monkey.ultimatebot.bot.BotType;
import com.monkey.ultimatebot.common.model.bot.BotArmorTier;
import com.monkey.ultimatebot.common.model.combat.DifficultyTier;
import com.monkey.ultimatebot.common.model.bot.EquipmentSlotKind;
import com.monkey.ultimatebot.utils.armor.ArmorCycle;
import com.monkey.ultimatebot.utils.armor.ArmorTier;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

final class ApiBotOptionsFactory {

    private final UltimateBot plugin;

    ApiBotOptionsFactory(UltimateBot plugin) {
        this.plugin = plugin;
    }

    BotOptions create(
            BotType type,
            UUID ownerUUID,
            UUID targetUUID,
            Set<UUID> targetUUIDs,
            Set<UUID> teamOwners,
            @Nullable UUID requestedBotUUID,
            Map<BotEquipmentSlot, BotEquipmentSlotSetting> equipmentSlots,
            BotSettings settings) {
        BotOptions options =
                new BotOptions(plugin, ArmorCycle.getDefaultArmorFromConfig(plugin.getLanguageConfig(), plugin));
        options.setBotType(type);
        options.setCreationSource(BotCreationSource.API);
        options.setRequestedBotUUID(requestedBotUUID);
        options.setEquipmentSlotSettings(equipmentSlots);
        options.setOwnerUUID(ownerUUID);
        options.setBotNameTemplate(settings.botNameTemplate());
        options.setBotSkin(settings.botSkin());
        options.setSpawnLocation(settings.spawnLocation());
        options.setAutoTarget(settings.autoTarget());
        options.setAutoTargetRange(settings.autoTargetRange());
        options.setAttackBots(settings.attackBots());
        options.setTargetMode(settings.targetMode());
        options.setRespectWorldGuardPvp(settings.respectWorldGuardPvp());
        options.setStayAfterOwnerDeath(settings.stayAfterOwnerDeath());
        options.setIdleWander(settings.idleWander());
        options.setIdleWanderRadius(settings.idleWanderRadius());
        options.setIdleReturnDistance(settings.idleReturnDistance());
        options.setIdleReturnDelayMs(settings.idleReturnDelayMs());
        options.setCrystalPvp(settings.crystalPvp());
        options.setExplosions(settings.explosions());
        options.setExplosionBlockDamage(settings.explosionBlockDamage());
        options.setEnderPearls(settings.enderPearls());
        options.setHealing(settings.healing());
        options.setKillMessageEnabled(settings.killMessageEnabled());
        options.setCustomKillMessage(settings.killMessage());
        options.setPreferredTargetUUID(targetUUID);
        options.setTargetUUIDs(targetUUIDs);
        options.setTeamOwnerUUIDs(teamOwners);
        options.setFollow(settings.follow());
        options.setCombat(settings.combat());
        options.setChangeableFollow(settings.changeableFollow());
        options.setChangeableCombat(settings.changeableCombat());
        options.setChangeableBlast(settings.changeableBlast());
        options.setChangeableArmor(settings.changeableArmor());
        options.setChangeableTotem(settings.changeableTotem());
        options.setChangeableDifficulty(settings.changeableDifficulty());
        options.setChangeableCombatMode(settings.changeableCombatMode());

        BotBlastProtection blast = settings.blastProtectionProfile();
        options.setBlastProtection(blast.feet(), blast.legs(), blast.chest(), blast.head());
        options.setArmorRange(toCoreArmor(settings.minArmorType()), toCoreArmor(settings.maxArmorType()));
        options.setArmorType(toCoreArmor(settings.armorType()));
        options.getArmor().putAll(settings.armorContents());
        copyTrimSettings(options, settings);
        options.setEquipmentContents(settings.equipmentContents());
        options.setDifficultyRange(
                toCoreDifficulty(settings.minDifficulty()), toCoreDifficulty(settings.maxDifficulty()));
        options.setDifficulty(toCoreDifficulty(settings.difficulty()));
        options.setCombatMode(settings.combatMode());
        options.setBrainKey(settings.brain());
        options.setCustomCombatTuning(settings.combatTuning());
        options.setTotemRange(settings.minTotemCount(), settings.maxTotemCount());
        options.setTotems(settings.totemCount());
        return options;
    }

    private static void copyTrimSettings(BotOptions options, BotSettings settings) {
        for (Map.Entry<EquipmentSlotKind, String> entry : settings.armorTrimPatternKeys().entrySet()) {
            options.setTrimPatternKey(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<EquipmentSlotKind, String> entry : settings.armorTrimMaterialKeys().entrySet()) {
            options.setTrimMaterialKey(entry.getKey(), entry.getValue());
        }
    }

    static com.monkey.ultimatebot.bot.ai.difficulty.DifficultyLevel toCoreDifficulty(DifficultyTier difficulty) {
        DifficultyTier resolved = difficulty == null ? DifficultyTier.EASY : difficulty;
        switch (resolved) {
            case EASY:
                return com.monkey.ultimatebot.bot.ai.difficulty.DifficultyLevel.EASY;
            case NORMAL:
                return com.monkey.ultimatebot.bot.ai.difficulty.DifficultyLevel.NORMAL;
            case MEDIUM:
                return com.monkey.ultimatebot.bot.ai.difficulty.DifficultyLevel.MEDIUM;
            case HARD:
                return com.monkey.ultimatebot.bot.ai.difficulty.DifficultyLevel.HARD;
            case GOD:
                return com.monkey.ultimatebot.bot.ai.difficulty.DifficultyLevel.GOD;
        }
        throw new IllegalStateException("Unexpected switch value");
    }

    private static ArmorTier toCoreArmor(BotArmorTier armorType) {
        BotArmorTier resolved = armorType == null ? BotArmorTier.LEATHER : armorType;
        switch (resolved) {
            case LEATHER:
                return ArmorTier.LEATHER;
            case IRON:
                return ArmorTier.IRON;
            case GOLDEN:
                return ArmorTier.GOLDEN;
            case DIAMOND:
                return ArmorTier.DIAMOND;
            case NETHERITE:
                return ArmorTier.NETHERITE;
        }
        throw new IllegalStateException("Unexpected switch value");
    }
}
