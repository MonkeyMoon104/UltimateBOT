package it.coralmc.sandbox.bot;

import it.coralmc.sandbox.SandboxTraining;
import it.coralmc.sandbox.bot.ai.TrainingBot;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.UUID;

public class BotManager {

    private final BotSpawner spawner;
    private final BotUpdater updater;
    private final BotLookup lookup;

    public BotManager(SandboxTraining plugin) {
        BotRegistry registry = plugin.getBotRegistry();
        this.spawner = new BotSpawner(plugin, registry);
        this.updater = new BotUpdater(registry);
        this.lookup = new BotLookup(registry);
    }

    public void spawn(Player viewer,
                      Map<EquipmentSlot, ItemStack> armorMap,
                      Map<EquipmentSlot, Boolean> blastProtectionMap,
                      boolean follow,
                      int totem,
                      BotOptions botOptions) {
        spawner.spawn(viewer, armorMap, blastProtectionMap, follow, totem, botOptions);
    }

    public void despawn(Player owner) {
        spawner.despawn(owner);
    }

    public void despawnAll() {
        spawner.despawnAll();
    }

    public void despawnBotInWorld(Player owner, org.bukkit.World fromWorld) {
        spawner.despawnInWorld(owner, fromWorld);
    }

    public TrainingBot getBot(UUID ownerUUID) {
        return lookup.getBotByOwnerUUID(ownerUUID);
    }

    public boolean isBotSpawned(UUID ownerUUID) {
        return lookup.isBotSpawned(ownerUUID);
    }

    public void removeBot(UUID botUUID) {
        lookup.removeBot(botUUID);
    }

    public void updateArmor(UUID ownerUUID,
                            Map<org.bukkit.inventory.EquipmentSlot, org.bukkit.inventory.ItemStack> armorMap,
                            Map<org.bukkit.inventory.EquipmentSlot, Boolean> blastProtectionMap) {
        updater.updateArmor(ownerUUID, armorMap, blastProtectionMap);
    }

    public void updateArmor(UUID ownerUUID,
                            Map<org.bukkit.inventory.EquipmentSlot, org.bukkit.inventory.ItemStack> armorMap) {
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
        TrainingBot bot = getBot(ownerUUID);
        if (bot != null) {
            bot.getBotAI().getInventoryController().switchToSword();
        }
    }

    public void switchBotToEnderpearl(UUID ownerUUID) {
        TrainingBot bot = getBot(ownerUUID);
        if (bot != null) {
            bot.getBotAI().getInventoryController().switchToEnderpearl();
        }
    }

    public boolean getSwordSlot(UUID ownerUUID) {
        return updater.getSwordSlot(ownerUUID);
    }

    public boolean getEpearlSlot(UUID ownerUUID) {
        return updater.getEpearlSlot(ownerUUID);
    }
}