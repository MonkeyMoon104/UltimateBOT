package com.monkey.mcbot.bot;

import com.monkey.mcbot.SandboxTraining;
import com.monkey.mcbot.bot.ai.ITrainingBot;
import com.monkey.mcbot.nms.NMSBridgeManager;
import com.monkey.mcbot.utils.EntityUtils;
import com.monkey.mcbot.utils.equipment.BotEquipmentUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.UUID;

public class BotSpawner {

    private final SandboxTraining plugin;
    private final BotRegistry registry;

    public BotSpawner(SandboxTraining plugin, BotRegistry registry) {
        this.plugin = plugin;
        this.registry = registry;
    }

    public void spawn(Player viewer,
                      Map<EquipmentSlot, ItemStack> armorMap,
                      Map<EquipmentSlot, Boolean> blastProtectionMap,
                      boolean follow,
                      int totem,
                      BotOptions botOptions) {

        if (viewer == null || !viewer.isOnline()) {
            if (plugin != null) {
                plugin.getLogger().warning("Attempted to spawn bot for null/offline player");
            }
            return;
        }

        if (registry.isBotSpawned(viewer.getUniqueId())) {
            if (plugin != null) {
                plugin.getLogger().info("Bot already exists for " + viewer.getName() + ", removing old one");
            }
            despawn(viewer);
        }

        ServerPlayer handle = ((CraftPlayer) viewer).getHandle();
        ServerLevel world = NMSBridgeManager.get().getServerLevel(handle);

        UUID botUUID = UUID.randomUUID();
        FileConfiguration config = plugin.getConfig();
        String rawName = config.getString("bot.name", "CrystalBot");
        String botName = rawName.replace("%player%", viewer.getName());

        Location loc = viewer.getLocation();
        Block block = loc.getWorld().getHighestBlockAt(loc);

        ITrainingBot bot = NMSBridgeManager.get().createTrainingBot(
                world,
                BlockPos.containing(block.getX(), block.getY(), block.getZ()),
                0,
                BotFactory.createProfile(viewer, botUUID, botName),
                viewer,
                follow,
                plugin,
                config.getString("messages.dead-bot-msg", "You have killed the bot!"),
                config.getString("messages.dead-bot-event-msg"),
                botOptions
        );

        bot.setTotemCount(totem);
        NMSBridgeManager.get().addToProfileCache(bot.asPlayer());
        world.addFreshEntity(bot.asPlayer());
        bot.getBotAI().manageTotem();
        BotEquipmentUtils.applyEquipment(bot.asPlayer(), armorMap, blastProtectionMap);

        BotBroadcaster.broadcastSpawn(bot, armorMap, blastProtectionMap);
        registry.registerBot(viewer.getUniqueId(), bot);

        bot.getBotAI().getInventoryController().addEnderpearls(16);
        bot.getBotAI().getInventoryController().switchToEnderpearl();

        if (plugin != null) {
            plugin.getLogger().info("Bot spawned successfully for " + viewer.getName());
        }
    }

    public void despawn(Player owner) {
        if (owner == null) return;

        UUID ownerUUID = owner.getUniqueId();
        UUID botUUID = registry.getBotUUID(ownerUUID);
        if (botUUID == null) return;

        ServerPlayer handle = ((CraftPlayer) owner).getHandle();
        ServerLevel world = NMSBridgeManager.get().getServerLevel(handle);
        if (EntityUtils.removeEntity(world, botUUID)) {
            registry.removeBot(ownerUUID);
        }
    }

    public void despawnAll() {
        registry.getAllBots().forEach((ownerUUID, bot) -> {
            Player owner = Bukkit.getPlayer(ownerUUID);
            if (owner != null && owner.isOnline()) {
                despawn(owner);
            } else {
                registry.removeBot(ownerUUID);
            }
        });
    }
    public void despawnInWorld(Player owner, org.bukkit.World fromWorld) {
        UUID ownerUUID = owner.getUniqueId();
        UUID botUUID = registry.getBotUUID(ownerUUID);
        if (botUUID == null) return;

        ServerLevel world = ((org.bukkit.craftbukkit.CraftWorld) fromWorld).getHandle();
        if (EntityUtils.removeEntity(world, botUUID)) {
            registry.removeBot(ownerUUID);
        }
    }
}