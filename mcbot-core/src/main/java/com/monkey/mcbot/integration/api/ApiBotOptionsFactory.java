package com.monkey.mcbot.integration.api;

import com.monkey.mcbot.MinecraftBot;
import com.monkey.mcbot.api.model.BotArmorType;
import com.monkey.mcbot.api.model.BotBlastProtection;
import com.monkey.mcbot.api.model.BotRank;
import com.monkey.mcbot.api.model.BotSettings;
import com.monkey.mcbot.bot.BotCreationSource;
import com.monkey.mcbot.bot.BotOptions;
import com.monkey.mcbot.bot.BotType;
import com.monkey.mcbot.utils.armor.ArmorCycle;
import com.monkey.mcbot.utils.armor.ArmorTier;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Maps the public API settings model into the mutable runtime options model. */
final class ApiBotOptionsFactory {

    private final MinecraftBot plugin;

    ApiBotOptionsFactory(MinecraftBot plugin) {
        this.plugin = plugin;
    }

    BotOptions create(BotType type,
                      UUID ownerUUID,
                      UUID targetUUID,
                      Set<UUID> targetUUIDs,
                      Set<UUID> teamOwners,
                      BotSettings settings) {
        BotOptions options = new BotOptions(
                plugin,
                ArmorCycle.getDefaultArmorFromConfig(plugin.getLanguageConfig(), plugin)
        );
        options.setBotType(type);
        options.setCreationSource(BotCreationSource.API);
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
        options.setChangeableRank(settings.changeableRank());

        BotBlastProtection blast = settings.blastProtectionProfile();
        options.setBlastProtection(blast.feet(), blast.legs(), blast.chest(), blast.head());
        options.setArmorRange(toCoreArmor(settings.minArmorType()), toCoreArmor(settings.maxArmorType()));
        options.setArmorType(toCoreArmor(settings.armorType()));
        options.getArmor().putAll(settings.armorContents());
        copyTrimSettings(options, settings);
        options.setEquipmentContents(settings.equipmentContents());
        options.setRankRange(toCoreRank(settings.minRank()), toCoreRank(settings.maxRank()));
        options.setRank(toCoreRank(settings.rank()));
        options.setTotemRange(settings.minTotemCount(), settings.maxTotemCount());
        options.setTotems(settings.totemCount());
        return options;
    }

    private static void copyTrimSettings(BotOptions options, BotSettings settings) {
        for (Map.Entry<org.bukkit.inventory.EquipmentSlot, String> entry
                : settings.armorTrimPatternKeys().entrySet()) {
            options.setTrimPatternKey(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<org.bukkit.inventory.EquipmentSlot, String> entry
                : settings.armorTrimMaterialKeys().entrySet()) {
            options.setTrimMaterialKey(entry.getKey(), entry.getValue());
        }
    }

    static com.monkey.mcbot.bot.ai.rank.BotRank toCoreRank(BotRank rank) {
        if (rank == null) {
            return com.monkey.mcbot.bot.ai.rank.BotRank.EASY;
        }
        return switch (rank) {
            case EASY -> com.monkey.mcbot.bot.ai.rank.BotRank.EASY;
            case NORMAL -> com.monkey.mcbot.bot.ai.rank.BotRank.NORMAL;
            case MEDIUM -> com.monkey.mcbot.bot.ai.rank.BotRank.MEDIUM;
            case HARD -> com.monkey.mcbot.bot.ai.rank.BotRank.HARD;
            case GOD -> com.monkey.mcbot.bot.ai.rank.BotRank.GOD;
        };
    }

    private static ArmorTier toCoreArmor(BotArmorType armorType) {
        if (armorType == null) {
            return ArmorTier.LEATHER;
        }
        return switch (armorType) {
            case LEATHER -> ArmorTier.LEATHER;
            case IRON -> ArmorTier.IRON;
            case GOLDEN -> ArmorTier.GOLDEN;
            case DIAMOND -> ArmorTier.DIAMOND;
            case NETHERITE -> ArmorTier.NETHERITE;
        };
    }
}
