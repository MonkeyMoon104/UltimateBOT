package com.monkey.ultimatebot.bot;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.api.event.lifecycle.BotDespawnReason;
import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.bot.ai.difficulty.DifficultyLevel;
import java.util.Map;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.Nullable;

public class BotManager {

    private final BotSpawner spawner;
    private final BotUpdater updater;
    private final BotLookup lookup;
    private final BotRegistry registry;

    public BotManager(UltimateBot plugin) {
        java.util.Objects.requireNonNull(plugin, "plugin");
        BotRegistry registry = plugin.getBotRegistry();
        this.registry = java.util.Objects.requireNonNull(registry, "registry");
        this.spawner = new BotSpawner(plugin, registry);
        this.updater = new BotUpdater(registry);
        this.lookup = new BotLookup(registry);
    }

    public boolean spawn(
            Player viewer,
            Player target,
            Map<EquipmentSlot, ItemStack> armorMap,
            Map<EquipmentSlot, Boolean> blastProtectionMap,
            boolean follow,
            int totem,
            BotOptions botOptions) {
        return spawner.spawn(viewer, target, armorMap, blastProtectionMap, follow, totem, botOptions);
    }

    public boolean spawn(
            Player viewer,
            Map<EquipmentSlot, ItemStack> armorMap,
            Map<EquipmentSlot, Boolean> blastProtectionMap,
            boolean follow,
            int totem,
            BotOptions botOptions) {
        return spawner.spawn(viewer, viewer, armorMap, blastProtectionMap, follow, totem, botOptions);
    }

    public boolean despawn(Player owner) {
        return spawner.despawn(owner);
    }

    public boolean despawn(Player owner, BotDespawnReason reason) {
        return spawner.despawn(owner, reason);
    }

    public boolean despawnByOwnerUUID(UUID ownerUUID) {
        return spawner.despawnByOwnerUUID(ownerUUID);
    }

    public boolean despawnByOwnerUUID(UUID ownerUUID, BotDespawnReason reason) {
        return spawner.despawnByOwnerUUID(ownerUUID, reason);
    }

    public void despawnAll() {
        spawner.despawnAll();
    }

    public void despawnAll(BotDespawnReason reason) {
        spawner.despawnAll(reason);
    }

    public void despawnBotInWorld(Player owner, org.bukkit.World fromWorld) {
        spawner.despawnInWorld(owner, fromWorld);
    }

    public @Nullable ITrainingBot getBot(UUID ownerUUID) {
        return lookup.getBotByOwnerUUID(ownerUUID);
    }

    public @Nullable ITrainingBot getBotSafe(UUID ownerUUID) {
        return lookup.getBotSafe(ownerUUID);
    }

    public @Nullable ITrainingBot getBotByParticipant(@Nullable UUID participantUUID) {
        if (participantUUID == null) {
            return null;
        }

        ITrainingBot directBot = getBotSafe(participantUUID);
        if (directBot != null) {
            return directBot;
        }

        return getTeamAllyBotByTeamOwner(participantUUID);
    }

    public @Nullable BotType getBotTypeByParticipant(UUID participantUUID) {
        ITrainingBot bot = getBotByParticipant(participantUUID);
        if (bot == null || bot.getBrainController() == null) {
            return null;
        }

        BotOptions options = bot.getBrainController().getBotOptions();
        if (options != null) {
            return options.getBotType();
        }

        return hasActiveTeamAlly(participantUUID) ? BotType.TEAM_ALLY : BotType.SINGLE;
    }

    public boolean isBotSpawned(UUID ownerUUID) {
        return lookup.isBotSpawned(ownerUUID);
    }

    public void removeBot(UUID botUUID) {
        lookup.removeBot(botUUID);
    }

    public @Nullable UUID findTeamAllyPrimaryOwner(@Nullable UUID teamOwnerUUID) {
        if (teamOwnerUUID == null) {
            return null;
        }

        for (Map.Entry<UUID, ITrainingBot> entry : registry.getAllBots().entrySet()) {
            ITrainingBot bot = entry.getValue();
            if (bot == null || bot.getBrainController() == null) {
                continue;
            }

            BotOptions options = bot.getBrainController().getBotOptions();
            if (options == null || options.getBotType() != BotType.TEAM_ALLY) {
                continue;
            }

            if (options.isTeamOwner(teamOwnerUUID)) {
                return entry.getKey();
            }
        }

        return null;
    }

    public @Nullable ITrainingBot getTeamAllyBotByTeamOwner(UUID teamOwnerUUID) {
        UUID primaryOwner = findTeamAllyPrimaryOwner(teamOwnerUUID);
        return primaryOwner == null ? null : getBotSafe(primaryOwner);
    }

    public boolean hasActiveTeamAlly(UUID teamOwnerUUID) {
        return getTeamAllyBotByTeamOwner(teamOwnerUUID) != null;
    }

    public void updateArmor(
            UUID ownerUUID,
            Map<org.bukkit.inventory.EquipmentSlot, org.bukkit.inventory.ItemStack> armorMap,
            Map<org.bukkit.inventory.EquipmentSlot, Boolean> blastProtectionMap) {
        updater.updateArmor(ownerUUID, armorMap, blastProtectionMap);
    }

    public void updateArmor(
            UUID ownerUUID, Map<org.bukkit.inventory.EquipmentSlot, org.bukkit.inventory.ItemStack> armorMap) {
        updater.updateArmor(ownerUUID, armorMap);
    }

    public void updateTotem(UUID ownerUUID, int totemCount) {
        updater.updateTotem(ownerUUID, totemCount);
    }

    public void updateFollow(UUID ownerUUID, boolean follow) {
        updater.updateFollow(ownerUUID, follow);
    }

    public void updateCombat(UUID ownerUUID, boolean combat) {
        updater.updateCombat(ownerUUID, combat);
    }

    public void updateBotInventorySlot(UUID ownerUUID, int slot, org.bukkit.inventory.ItemStack item) {
        updater.updateInventorySlot(ownerUUID, slot, item);
    }

    public void switchBotSlot(UUID ownerUUID, int slot) {
        updater.switchBotSlot(ownerUUID, slot);
    }

    public void addBotEnderpearls(UUID ownerUUID, int count) {
        updater.addEnderpearls(ownerUUID, count);
    }

    public int getBotEnderpearlCount(UUID ownerUUID) {
        return updater.getBotEnderpearlCount(ownerUUID);
    }

    public void switchBotToSword(UUID ownerUUID) {
        ITrainingBot bot = getBot(ownerUUID);
        if (bot != null) {
            bot.getBotAI().getInventoryController().switchToSword();
        }
    }

    public void switchBotToEnderpearl(UUID ownerUUID) {
        ITrainingBot bot = getBot(ownerUUID);
        if (bot != null) {
            bot.getBotAI().getInventoryController().switchToEnderpearl();
        }
    }

    public void switchBotToEmpty(UUID ownerUUID) {
        ITrainingBot bot = getBot(ownerUUID);
        if (bot != null) {
            bot.getBotAI().getInventoryController().switchToEmptySlot();
        }
    }

    public boolean getSwordSlot(UUID ownerUUID) {
        return updater.getSwordSlot(ownerUUID);
    }

    public boolean getEpearlSlot(UUID ownerUUID) {
        return updater.getEpearlSlot(ownerUUID);
    }

    public void setDifficultyLevel(UUID uuid, DifficultyLevel difficulty) {
        var bot = getBot(uuid);
        if (bot == null) return;

        bot.getBotAI().setDifficulty(difficulty);
    }
}
