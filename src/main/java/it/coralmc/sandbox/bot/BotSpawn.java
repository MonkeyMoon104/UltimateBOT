package it.coralmc.sandbox.bot;

import it.coralmc.sandbox.SandboxTraining;
import it.coralmc.sandbox.bot.ai.TrainingBot;
import it.coralmc.sandbox.utils.EntityUtils;
import it.coralmc.sandbox.utils.equipment.BotEquipmentUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.*;

public class BotSpawn {

    private final SandboxTraining plugin;

    public BotSpawn(SandboxTraining plugin) {
        this.plugin = plugin;
    }

    public void spawnFakeBot(Player viewer,
                             Map<org.bukkit.inventory.EquipmentSlot, org.bukkit.inventory.ItemStack> armorMap,
                             Map<org.bukkit.inventory.EquipmentSlot, Boolean> blastProtectionMap,
                             boolean follow,
                             int totem) {
        ServerPlayer handle = ((CraftPlayer) viewer).getHandle();
        ServerLevel world = handle.serverLevel();

        UUID botUUID = UUID.randomUUID();
        FileConfiguration config = plugin.getConfig();
        String botName = config.getString("bot.name", "CrystalBot");

        Location loc = viewer.getLocation();
        Block block = loc.getWorld().getHighestBlockAt(loc);

        TrainingBot bot = new TrainingBot(
                world,
                BlockPos.containing(block.getX(), block.getY(), block.getZ()),
                0,
                BotFactory.createProfile(viewer, botUUID, botName),
                viewer,
                follow,
                this,
                plugin,
                config.getString("messages.dead-bot-msg", "You have killed the bot!")
        );

        bot.setTotemCount(totem);
        world.addFreshEntity(bot);
        bot.getBotAI().manageTotem();
        BotEquipmentUtils.applyEquipment(bot, armorMap, blastProtectionMap);

        BotBroadcaster.broadcastSpawn(bot, armorMap, blastProtectionMap);
        plugin.getBotRegistry().registerBot(viewer.getUniqueId(), bot);
    }

    public boolean isBotSpawned(UUID playerUUID) {
        return plugin.getBotRegistry().isBotSpawned(playerUUID);
    }

    public void despawnBot(Player owner) {
        UUID ownerUUID = owner.getUniqueId();
        UUID botUUID = plugin.getBotRegistry().getBotUUID(ownerUUID);
        if (botUUID == null) return;

        ServerLevel world = ((CraftPlayer) owner).getHandle().serverLevel();
        if (EntityUtils.removeEntity(world, botUUID)) {
            plugin.getBotRegistry().removeBot(ownerUUID);
        }
    }

    public void despawnAllBots() {
        for (Map.Entry<UUID, TrainingBot> entry : new HashMap<>(plugin.getBotRegistry().getAllBots()).entrySet()) {
            Player owner = Bukkit.getPlayer(entry.getKey());
            if (owner != null && owner.isOnline()) {
                despawnBot(owner);
            } else {
                plugin.getBotRegistry().removeBot(entry.getKey());
            }
        }
    }

    public void updateBotArmor(UUID ownerUUID,
                               Map<org.bukkit.inventory.EquipmentSlot, org.bukkit.inventory.ItemStack> armorMap,
                               Map<org.bukkit.inventory.EquipmentSlot, Boolean> blastProtectionMap) {
        BotEquipmentUtils.updateBotArmor(ownerUUID, armorMap, blastProtectionMap, plugin.getBotRegistry());
    }

    public void updateBotArmor(UUID ownerUUID,
                               Map<org.bukkit.inventory.EquipmentSlot, org.bukkit.inventory.ItemStack> armorMap) {
        updateBotArmor(ownerUUID, armorMap, new HashMap<>());
    }

    public void removeBot(UUID botUUID) {
        plugin.getBotRegistry().removeBotByUUID(botUUID);
    }

    public TrainingBot getBotByOwnerUUID(UUID ownerUUID) {
        ServerLevel world = EntityUtils.getPlayerWorld(ownerUUID);
        if (world == null) return null;

        UUID botUUID = plugin.getBotRegistry().getBotUUID(ownerUUID);
        Entity entity = EntityUtils.findBotByUUID(world, botUUID);

        return (entity instanceof TrainingBot trainingBot) ? trainingBot : null;
    }

    public void updateBotTotemCount(UUID ownerUUID, int totemCount) {
        TrainingBot bot = getBotByOwnerUUID(ownerUUID);
        if (bot != null) {
            bot.setTotemCount(totemCount);
            bot.getBotAI().manageTotem();
        }
    }

    public void updateBotFollow(UUID ownerUUID, boolean follow) {
        TrainingBot bot = getBotByOwnerUUID(ownerUUID);
        if (bot != null) {
            bot.setFollow(follow);
        }
    }

    public void despawnBotInWorld(Player owner, World fromWorld) {
        UUID ownerUUID = owner.getUniqueId();
        UUID botUUID = plugin.getBotRegistry().getBotUUID(ownerUUID);
        if (botUUID == null) return;

        ServerLevel world = ((CraftWorld) fromWorld).getHandle();
        if (EntityUtils.removeEntity(world, botUUID)) {
            plugin.getBotRegistry().removeBot(ownerUUID);
        }
    }
}
