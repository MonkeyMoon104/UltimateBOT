package it.coralmc.sandbox.bot;

import it.coralmc.sandbox.SandboxTraining;
import it.coralmc.sandbox.bot.ai.TrainingBot;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public class BotManager {

    private final SandboxTraining plugin;
    private final BotSpawn spawner;

    public BotManager(SandboxTraining plugin) {
        this.plugin = plugin;
        this.spawner = new BotSpawn(plugin);
    }

    public void spawnBot(Player viewer,
                         Map<org.bukkit.inventory.EquipmentSlot, org.bukkit.inventory.ItemStack> armorMap,
                         Map<org.bukkit.inventory.EquipmentSlot, Boolean> blastProtectionMap,
                         boolean follow,
                         int totem) {
        spawner.spawnFakeBot(viewer, armorMap, blastProtectionMap, follow, totem);
    }

    public void despawnBot(Player owner) {
        spawner.despawnBot(owner);
    }

    public void despawnBotInWorld(Player owner, org.bukkit.World fromWorld) {
        spawner.despawnBotInWorld(owner, fromWorld);
    }

    public void despawnAllBots() {
        spawner.despawnAllBots();
    }

    public TrainingBot getBot(UUID ownerUUID) {
        return spawner.getBotByOwnerUUID(ownerUUID);
    }

    public boolean isBotSpawned(UUID ownerUUID) {
        return spawner.isBotSpawned(ownerUUID);
    }

    public void removeBot(UUID botUUID) {
        spawner.removeBot(botUUID);
    }

    public void updateArmor(UUID ownerUUID,
                            Map<org.bukkit.inventory.EquipmentSlot, org.bukkit.inventory.ItemStack> armorMap,
                            Map<org.bukkit.inventory.EquipmentSlot, Boolean> blastProtectionMap) {
        spawner.updateBotArmor(ownerUUID, armorMap, blastProtectionMap);
    }

    public void updateArmor(UUID ownerUUID,
                            Map<org.bukkit.inventory.EquipmentSlot, org.bukkit.inventory.ItemStack> armorMap) {
        spawner.updateBotArmor(ownerUUID, armorMap);
    }

    public void updateTotem(UUID ownerUUID, int totemCount) {
        spawner.updateBotTotemCount(ownerUUID, totemCount);
    }

    public void updateFollow(UUID ownerUUID, boolean follow) {
        spawner.updateBotFollow(ownerUUID, follow);
    }
}
