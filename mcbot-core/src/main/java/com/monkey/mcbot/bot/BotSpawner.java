package com.monkey.mcbot.bot;

import com.mojang.authlib.GameProfile;
import com.monkey.mcbot.MinecraftBot;
import com.monkey.mcbot.api.model.BotLocation;
import com.monkey.mcbot.api.event.base.BotEventSource;
import com.monkey.mcbot.api.event.lifecycle.BotDespawnEvent;
import com.monkey.mcbot.api.event.lifecycle.BotDespawnPrepareEvent;
import com.monkey.mcbot.api.event.lifecycle.BotDespawnReason;
import com.monkey.mcbot.api.event.lifecycle.BotSpawnEvent;
import com.monkey.mcbot.api.event.lifecycle.BotSpawnPrepareEvent;
import com.monkey.mcbot.api.model.BotSnapshot;
import com.monkey.mcbot.integration.api.BotSnapshotMapper;
import com.monkey.mcbot.event.BotEventSourceContext;
import com.monkey.mcbot.bot.ai.ITrainingBot;
import com.monkey.mcbot.logging.MinecraftBotLogging;
import com.monkey.mcbot.nms.NMSBridgeManager;
import com.monkey.mcbot.utils.EntityUtils;
import com.monkey.mcbot.utils.equipment.BotEquipmentUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.UUID;

public class BotSpawner {

    private final MinecraftBot plugin;
    private final BotRegistry registry;
    private final BotProfileResolver profileResolver = new BotProfileResolver();

    public BotSpawner(MinecraftBot plugin, BotRegistry registry) {
        this.plugin = plugin;
        this.registry = registry;
    }

    public boolean spawn(Player viewer,
                      Player target,
                      Map<EquipmentSlot, ItemStack> armorMap,
                      Map<EquipmentSlot, Boolean> blastProtectionMap,
                      boolean follow,
                      int totem,
                      BotOptions botOptions) {

        if (viewer == null || !viewer.isOnline()) {
            return false;
        }

        Player registryOwner = resolveRegistryOwner(viewer, botOptions);
        UUID registryOwnerUUID = resolveRegistryOwnerUUID(registryOwner, botOptions);
        Player resolvedTarget = resolveTargetPlayer(registryOwner, target, botOptions);

        UUID botUUID = UUID.randomUUID();
        botOptions.setOwnerUUID(registryOwnerUUID);
        if (botOptions.getBotType() == BotType.TEAM_ALLY && botOptions.getTeamOwnerUUIDs().isEmpty()) {
            botOptions.addTeamOwner(registryOwnerUUID);
        }

        FileConfiguration config = plugin.getConfig();
        GameProfile profile = profileResolver.resolve(
                config, registryOwner, resolvedTarget, botUUID, botOptions
        );

        Location spawnLocation = resolveSpawnLocation(registryOwner, botOptions);
        BotEventSource spawnSource = BotEventSourceContext.currentOr(
                botOptions != null && botOptions.getCreationSource() == BotCreationSource.API
                        ? BotEventSource.API : BotEventSource.GUI);
        BotSpawnPrepareEvent prepareEvent = plugin.getBotEventDispatcher().publish(new BotSpawnPrepareEvent(
                plugin.getBotEventDispatcher().nextSequence(botUUID), registryOwnerUUID, botUUID,
                spawnSource, spawnLocation));
        if (prepareEvent.isCancelled()) {
            plugin.getBotEventDispatcher().forget(botUUID);
            return false;
        }
        spawnLocation = prepareEvent.getSpawnLocation();
        if (registry.isBotSpawned(registryOwnerUUID)) {
            despawnByOwnerUUID(registryOwnerUUID, BotDespawnReason.REPLACED);
        }
        botOptions.setSpawnLocation(BotLocation.of(spawnLocation));
        ServerLevel world = ((CraftWorld) spawnLocation.getWorld()).getHandle();
        BlockPos spawnPos = BlockPos.containing(spawnLocation.getX(), spawnLocation.getY(), spawnLocation.getZ());

        ITrainingBot bot = NMSBridgeManager.get().createTrainingBot(
                world,
                spawnPos,
                0,
                profile,
                resolvedTarget,
                follow,
                plugin,
                plugin.getLangString("messages.dead-bot-msg", "You have killed the bot!"),
                plugin.getLangString("messages.dead-bot-event-msg"),
                botOptions
        );

        bot.setTotemCount(totem);
        NMSBridgeManager.get().addToProfileCache(bot.asPlayer());
        world.addFreshEntity(bot.asPlayer());
        plugin.markCompatibilityBot(bot.asPlayer().getBukkitEntity());
        bot.getBotAI().manageTotem();
        BotEquipmentUtils.applyEquipment(bot.asPlayer(), armorMap, blastProtectionMap);
        applyCustomEquipment(bot, botOptions);

        BotBroadcaster.broadcastSpawn(bot, armorMap, blastProtectionMap);
        registry.registerBot(registryOwnerUUID, bot);

        if (botOptions == null || botOptions.isEnderPearls()) {
            bot.getBotAI().getInventoryController().addEnderpearls(16);
        } else {
            bot.getBotAI().getInventoryController().setItem(
                    com.monkey.mcbot.bot.ai.controllers.inventory.BotInventoryController.ENDERPEARL_SLOT,
                    net.minecraft.world.item.ItemStack.EMPTY
            );
        }

        if (botOptions == null || botOptions.isCombat()) {
            bot.getBotAI().getInventoryController().switchToSword();
        } else if (botOptions.isEnderPearls()) {
            bot.getBotAI().getInventoryController().switchToEnderpearl();
        }

        if (botOptions != null && (!botOptions.isCrystalPvp() || !botOptions.isExplosions())) {
            bot.getBotAI().getCPVPController().setEnabled(false);
            clearExplosiveItems(bot);
        }
        BotSnapshot snapshot = BotSnapshotMapper.toSnapshot(registryOwnerUUID, bot);
        if (snapshot != null) {
            plugin.getBotEventDispatcher().publish(new BotSpawnEvent(
                    plugin.getBotEventDispatcher().nextSequence(botUUID), snapshot,
                    BotEventSourceContext.currentOr(
                            botOptions != null && botOptions.getCreationSource() == BotCreationSource.API
                                    ? BotEventSource.API : BotEventSource.GUI),
                    bot.asPlayer().getBukkitEntity()
            ));
        }
        return true;
    }

    private UUID resolveRegistryOwnerUUID(Player registryOwner, BotOptions botOptions) {
        if (botOptions != null
                && botOptions.getCreationSource() == BotCreationSource.API
                && botOptions.getBotType() == BotType.EVENT
                && botOptions.getOwnerUUID() != null) {
            return botOptions.getOwnerUUID();
        }
        return registryOwner.getUniqueId();
    }

    private void clearExplosiveItems(ITrainingBot bot) {
        bot.getBotAI().getInventoryController().setItem(
                com.monkey.mcbot.bot.ai.controllers.inventory.BotInventoryController.CRYSTAL_SLOT,
                net.minecraft.world.item.ItemStack.EMPTY
        );
        bot.getBotAI().getInventoryController().setItem(
                com.monkey.mcbot.bot.ai.controllers.inventory.BotInventoryController.ANCHOR_SLOT,
                net.minecraft.world.item.ItemStack.EMPTY
        );
        bot.getBotAI().getInventoryController().setItem(
                com.monkey.mcbot.bot.ai.controllers.inventory.BotInventoryController.GLOW_SLOT,
                net.minecraft.world.item.ItemStack.EMPTY
        );
    }

    private Location resolveSpawnLocation(Player registryOwner, BotOptions botOptions) {
        if (botOptions != null && botOptions.getSpawnLocation() != null) {
            com.monkey.mcbot.api.model.BotLocation apiLocation = botOptions.getSpawnLocation();
            World world = apiLocation.worldUUID() == null ? null : Bukkit.getWorld(apiLocation.worldUUID());
            if (world == null && apiLocation.worldName() != null) {
                world = Bukkit.getWorld(apiLocation.worldName());
            }
            if (world != null) {
                return new Location(
                        world,
                        apiLocation.x(),
                        apiLocation.y(),
                        apiLocation.z(),
                        apiLocation.yaw(),
                        apiLocation.pitch()
                );
            }
        }

        Location loc = registryOwner.getLocation();
        Block block = loc.getWorld().getHighestBlockAt(loc);
        return new Location(loc.getWorld(), block.getX(), block.getY(), block.getZ(), loc.getYaw(), loc.getPitch());
    }

    private void applyCustomEquipment(ITrainingBot bot, BotOptions botOptions) {
        if (botOptions == null || botOptions.getEquipmentContents().isEmpty()) {
            return;
        }

        for (Map.Entry<Integer, ItemStack> entry : botOptions.getEquipmentContents().entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) {
                continue;
            }
            net.minecraft.world.item.ItemStack nmsItem = org.bukkit.craftbukkit.inventory.CraftItemStack.asNMSCopy(entry.getValue());
            bot.getBotAI().getInventoryController().setItem(entry.getKey(), nmsItem);
        }
    }

    private Player resolveTargetPlayer(Player registryOwner, Player target, BotOptions botOptions) {
        if (target != null && target.isOnline()) {
            return target;
        }

        if (botOptions != null && botOptions.getPreferredTargetUUID() != null) {
            Player preferred = Bukkit.getPlayer(botOptions.getPreferredTargetUUID());
            if (preferred != null && preferred.isOnline()) {
                return preferred;
            }
        }

        if (botOptions != null && !botOptions.getTargetUUIDs().isEmpty()) {
            for (UUID targetUUID : botOptions.getTargetUUIDs()) {
                Player candidate = Bukkit.getPlayer(targetUUID);
                if (candidate != null && candidate.isOnline()) {
                    return candidate;
                }
            }
        }

        return registryOwner;
    }

    private Player resolveRegistryOwner(Player viewer, BotOptions botOptions) {
        if (botOptions == null || botOptions.getBotType() != BotType.TEAM_ALLY) {
            return viewer;
        }

        UUID explicitOwner = botOptions.getOwnerUUID();
        if (explicitOwner != null && botOptions.isTeamOwner(explicitOwner)) {
            Player explicitOwnerPlayer = Bukkit.getPlayer(explicitOwner);
            if (explicitOwnerPlayer != null && explicitOwnerPlayer.isOnline()) {
                return explicitOwnerPlayer;
            }
        }

        for (UUID ownerUUID : botOptions.getTeamOwnerUUIDs()) {
            Player owner = Bukkit.getPlayer(ownerUUID);
            if (owner != null && owner.isOnline()) {
                return owner;
            }
        }

        return viewer;
    }


    public boolean despawn(Player owner) {
        return despawn(owner, BotDespawnReason.MANUAL);
    }

    public boolean despawn(Player owner, BotDespawnReason reason) {
        if (owner == null) return false;

        UUID ownerUUID = owner.getUniqueId();
        UUID botUUID = registry.getBotUUID(ownerUUID);
        if (botUUID == null) return false;
        if (!allowDespawn(ownerUUID, reason)) return false;

        ServerPlayer handle = ((CraftPlayer) owner).getHandle();
        ServerLevel world = NMSBridgeManager.get().getServerLevel(handle);
        boolean removed = EntityUtils.removeEntity(world, botUUID);
        cleanupDespawnState(ownerUUID, botUUID, removed, reason);
        return true;
    }

    public boolean despawnByOwnerUUID(UUID ownerUUID) {
        return despawnByOwnerUUID(ownerUUID, BotDespawnReason.MANUAL);
    }

    public boolean despawnByOwnerUUID(UUID ownerUUID, BotDespawnReason reason) {
        if (ownerUUID == null) {
            return false;
        }

        UUID botUUID = registry.getBotUUID(ownerUUID);
        if (botUUID == null) {
            return false;
        }
        if (!allowDespawn(ownerUUID, reason)) return false;

        ITrainingBot bot = registry.getBot(ownerUUID);
        if (bot != null && bot.asPlayer().level() instanceof ServerLevel world) {
            boolean removed = EntityUtils.removeEntity(world, botUUID);
            cleanupDespawnState(ownerUUID, botUUID, removed, reason);
            return true;
        }

        Player owner = Bukkit.getPlayer(ownerUUID);
        if (owner != null) {
            return despawn(owner, reason);
        }

        boolean removed = EntityUtils.removeEntityInLoadedWorlds(botUUID);
        cleanupDespawnState(ownerUUID, botUUID, removed, reason);
        return true;
    }

    public void despawnAll() {
        despawnAll(BotDespawnReason.MANUAL);
    }

    public void despawnAll(BotDespawnReason reason) {
        registry.getAllBots().forEach((ownerUUID, bot) -> {
            Player owner = Bukkit.getPlayer(ownerUUID);
            if (owner != null && owner.isOnline()) {
                despawn(owner, reason);
            } else {
                UUID botUUID = bot != null && bot.asPlayer() != null ? bot.asPlayer().getUUID() : registry.getBotUUID(ownerUUID);
                if (!allowDespawn(ownerUUID, reason)) return;
                boolean removed = botUUID != null && EntityUtils.removeEntityInLoadedWorlds(botUUID);
                cleanupDespawnState(ownerUUID, botUUID, removed, reason);
            }
        });
    }

    public void despawnInWorld(Player owner, org.bukkit.World fromWorld) {
        despawnInWorld(owner, fromWorld, BotDespawnReason.WORLD_CHANGE);
    }

    public void despawnInWorld(Player owner, org.bukkit.World fromWorld, BotDespawnReason reason) {
        UUID ownerUUID = owner.getUniqueId();
        UUID botUUID = registry.getBotUUID(ownerUUID);
        if (botUUID == null) return;
        if (!allowDespawn(ownerUUID, reason)) return;

        ServerLevel world = ((org.bukkit.craftbukkit.CraftWorld) fromWorld).getHandle();
        boolean removed = EntityUtils.removeEntity(world, botUUID);
        cleanupDespawnState(ownerUUID, botUUID, removed, reason);
    }

    private void cleanupDespawnState(UUID ownerUUID, UUID botUUID, boolean removed, BotDespawnReason reason) {
        ITrainingBot bot = registry.getBot(ownerUUID);
        BotSnapshot snapshot = BotSnapshotMapper.toSnapshot(ownerUUID, bot);
        if (!removed && botUUID != null) {
            MinecraftBotLogging.warn(plugin.getLogger(), "Bot", "Despawn cleanup could not find entity " + botUUID + " for owner " + ownerUUID);
        }

        if (botUUID != null) {
            NMSBridgeManager.get().removeFromProfileCache(botUUID);
            plugin.forgetCompatibilityBot(botUUID);
        }

        registry.removeBot(ownerUUID);
        if (snapshot != null) {
            BotEventSource source = BotEventSourceContext.currentOr(BotEventSource.GUI);
            plugin.getBotEventDispatcher().publish(new BotDespawnEvent(
                    plugin.getBotEventDispatcher().nextSequence(snapshot.botUUID()), snapshot,
                    source, reason == BotDespawnReason.MANUAL
                            && (source == BotEventSource.API || source == BotEventSource.REMOTE_API)
                            ? BotDespawnReason.API_REQUEST : reason));
            plugin.getBotEventDispatcher().forget(snapshot.botUUID());
        }
    }

    private boolean allowDespawn(UUID ownerUUID, BotDespawnReason requestedReason) {
        BotEventSource source = BotEventSourceContext.currentOr(BotEventSource.GUI);
        BotDespawnReason reason = requestedReason == BotDespawnReason.MANUAL
                && (source == BotEventSource.API || source == BotEventSource.REMOTE_API)
                ? BotDespawnReason.API_REQUEST : requestedReason;
        if (reason != BotDespawnReason.MANUAL && reason != BotDespawnReason.API_REQUEST) return true;
        ITrainingBot bot = registry.getBot(ownerUUID);
        BotSnapshot snapshot = BotSnapshotMapper.toSnapshot(ownerUUID, bot);
        if (snapshot == null) return true;
        BotDespawnPrepareEvent event = plugin.getBotEventDispatcher().publish(new BotDespawnPrepareEvent(
                plugin.getBotEventDispatcher().nextSequence(snapshot.botUUID()), snapshot, source, reason));
        return !event.isCancelled();
    }
}
