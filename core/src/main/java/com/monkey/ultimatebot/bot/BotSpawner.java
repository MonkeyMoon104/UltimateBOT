package com.monkey.ultimatebot.bot;

import com.monkey.ultimatebot.compat.ItemStackAccess;
import com.monkey.ultimatebot.compat.EntityLookupAccess;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.api.event.base.BotEventSource;
import com.monkey.ultimatebot.api.event.lifecycle.BotDespawnEvent;
import com.monkey.ultimatebot.api.event.lifecycle.BotDespawnPrepareEvent;
import com.monkey.ultimatebot.api.event.lifecycle.BotDespawnReason;
import com.monkey.ultimatebot.api.event.lifecycle.BotSpawnEvent;
import com.monkey.ultimatebot.api.event.lifecycle.BotSpawnPrepareEvent;
import com.monkey.ultimatebot.api.model.runtime.BotLocation;
import com.monkey.ultimatebot.api.model.runtime.BotSnapshot;
import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.event.BotEventSourceContext;
import com.monkey.ultimatebot.integration.api.BotSnapshotMapper;
import com.monkey.ultimatebot.logging.UltimateBotLogging;
import com.monkey.ultimatebot.nms.NMSBridgeManager;
import com.monkey.ultimatebot.protocol.BotProfileData;
import com.monkey.ultimatebot.utils.EntityUtils;
import com.monkey.ultimatebot.utils.equipment.BotEquipmentUtils;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.Nullable;

public class BotSpawner {

    private final UltimateBot plugin;
    private final BotRegistry registry;
    private final BotProfileResolver profileResolver = new BotProfileResolver();

    public BotSpawner(UltimateBot plugin, BotRegistry registry) {
        this.plugin = java.util.Objects.requireNonNull(plugin, "plugin");
        this.registry = java.util.Objects.requireNonNull(registry, "registry");
    }

    public boolean spawn(
            Player viewer,
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

        UUID botUUID = botOptions.getRequestedBotUUID() == null ? UUID.randomUUID() : botOptions.getRequestedBotUUID();
        if (!isBotUUIDAvailable(botUUID)) {
            return false;
        }
        botOptions.setOwnerUUID(registryOwnerUUID);
        if (botOptions.getBotType() == BotType.TEAM_ALLY
                && botOptions.getTeamOwnerUUIDs().isEmpty()) {
            botOptions.addTeamOwner(registryOwnerUUID);
        }

        FileConfiguration config = plugin.getConfig();
        BotProfileData profile = profileResolver.resolve(config, registryOwner, resolvedTarget, botUUID, botOptions);

        Location spawnLocation = resolveSpawnLocation(registryOwner, botOptions);
        BotEventSource spawnSource = BotEventSourceContext.currentOr(
                botOptions != null && botOptions.getCreationSource() == BotCreationSource.API
                        ? BotEventSource.API
                        : BotEventSource.GUI);
        BotSpawnPrepareEvent prepareEvent = plugin.getBotEventDispatcher()
                .publish(new BotSpawnPrepareEvent(
                        plugin.getBotEventDispatcher().nextSequence(botUUID),
                        registryOwnerUUID,
                        botUUID,
                        spawnSource,
                        spawnLocation));
        if (prepareEvent.isCancelled()) {
            plugin.getBotEventDispatcher().forget(botUUID);
            return false;
        }
        spawnLocation = prepareEvent.getSpawnLocation();
        if (registry.isBotSpawned(registryOwnerUUID)) {
            despawnByOwnerUUID(registryOwnerUUID, BotDespawnReason.REPLACED);
        }
        botOptions.setSpawnLocation(BotLocation.of(spawnLocation));
        ITrainingBot bot = NMSBridgeManager.get()
                .createTrainingBot(
                        spawnLocation,
                        profile,
                        resolvedTarget,
                        follow,
                        plugin,
                        plugin.getLangString("messages.dead-bot-msg", "You have killed the bot!"),
                        plugin.getLangString("messages.dead-bot-event-msg"),
                        botOptions);

        bot.setTotemCount(totem);
        NMSBridgeManager.get().registerBotEntity(bot);
        NMSBridgeManager.get().addToProfileCache(bot);
        plugin.markCompatibilityBot(bot.asBukkitPlayer());
        bot.getBotAI().manageTotem();
        BotEquipmentUtils.applyEquipment(bot, armorMap, blastProtectionMap);
        applyCustomEquipment(bot, botOptions);

        BotBroadcaster.broadcastSpawn(bot, armorMap, blastProtectionMap);
        registry.registerBot(registryOwnerUUID, bot);

        if (botOptions == null || botOptions.isEnderPearls()) {
            bot.getBotAI().getInventoryController().addEnderpearls(16);
        } else {
            bot.getBotAI()
                    .getInventoryController()
                    .setItem(
                            com.monkey.ultimatebot.bot.ai.controllers.inventory.BotInventoryController.ENDERPEARL_SLOT,
                            ItemStackAccess.empty());
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
        BotEquipmentPolicy.enforce(bot, botOptions);
        BotSnapshot snapshot = BotSnapshotMapper.toSnapshot(registryOwnerUUID, bot);
        if (snapshot != null) {
            plugin.getBotEventDispatcher()
                    .publish(new BotSpawnEvent(
                            plugin.getBotEventDispatcher().nextSequence(botUUID),
                            snapshot,
                            BotEventSourceContext.currentOr(
                                    botOptions != null && botOptions.getCreationSource() == BotCreationSource.API
                                            ? BotEventSource.API
                                            : BotEventSource.GUI),
                            bot.asBukkitPlayer()));
        }
        return true;
    }

    private boolean isBotUUIDAvailable(UUID botUUID) {
        return botUUID != null
                && !botUUID.equals(new UUID(0L, 0L))
                && registry.getOwnerUUIDByBotUUID(botUUID) == null
                && org.bukkit.Bukkit.getPlayer(botUUID) == null
                && EntityLookupAccess.get(botUUID) == null;
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
        bot.getBotAI()
                .getInventoryController()
                .setItem(
                        com.monkey.ultimatebot.bot.ai.controllers.inventory.BotInventoryController.CRYSTAL_SLOT,
                        ItemStackAccess.empty());
        bot.getBotAI()
                .getInventoryController()
                .setItem(
                        com.monkey.ultimatebot.bot.ai.controllers.inventory.BotInventoryController.ANCHOR_SLOT,
                        ItemStackAccess.empty());
        bot.getBotAI()
                .getInventoryController()
                .setItem(
                        com.monkey.ultimatebot.bot.ai.controllers.inventory.BotInventoryController.GLOW_SLOT,
                        ItemStackAccess.empty());
    }

    private Location resolveSpawnLocation(Player registryOwner, BotOptions botOptions) {
        if (botOptions != null && botOptions.getSpawnLocation() != null) {
            com.monkey.ultimatebot.api.model.runtime.BotLocation apiLocation = botOptions.getSpawnLocation();
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
                        apiLocation.pitch());
            }
        }

        Location loc = Objects.requireNonNull(registryOwner.getLocation(), "registry owner location").clone();
        World world = Objects.requireNonNull(loc.getWorld(), "registry owner world");
        // Always spawn at the owner. Only nudge up if the feet block is a full solid (never use
        // getHighestBlockAt — that put bots on roofs/trees far above the player).
        Block feet = loc.getBlock();
        if (isBlockingSpawnBlock(feet.getType())) {
            Block above = feet.getRelative(0, 1, 0);
            if (!isBlockingSpawnBlock(above.getType())) {
                loc.setY(feet.getY() + 1.0D);
            }
        }
        return loc;
    }

    private static boolean isBlockingSpawnBlock(Material material) {
        if (material == null || material == Material.AIR || !material.isSolid()) {
            return false;
        }
        // Cobwebs / passable solids must not force a Y bump or roof teleport.
        String name = material.name();
        return !"COBWEB".equals(name)
                && !"STRING".equals(name)
                && !name.endsWith("_CARPET")
                && !name.endsWith("_SIGN")
                && !name.contains("PRESSURE_PLATE");
    }

    private void applyCustomEquipment(ITrainingBot bot, BotOptions botOptions) {
        if (botOptions == null || botOptions.getEquipmentContents().isEmpty()) {
            return;
        }

        for (Map.Entry<Integer, ItemStack> entry :
                botOptions.getEquipmentContents().entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) {
                continue;
            }
            bot.getBotAI().getInventoryController().setItem(entry.getKey(), entry.getValue());
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
        prepareBotDespawn(ownerUUID);

        boolean removed = EntityUtils.removeEntity(owner.getWorld(), botUUID);
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
        prepareBotDespawn(ownerUUID);

        ITrainingBot bot = registry.getBot(ownerUUID);
        if (bot != null && bot.asBukkitPlayer().getWorld() != null) {
            boolean removed = EntityUtils.removeEntity(bot.asBukkitPlayer().getWorld(), botUUID);
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
                UUID botUUID = bot != null && bot.asBukkitPlayer() != null
                        ? bot.asBukkitPlayer().getUniqueId()
                        : registry.getBotUUID(ownerUUID);
                if (!allowDespawn(ownerUUID, reason)) return;
                prepareBotDespawn(ownerUUID);
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
        prepareBotDespawn(ownerUUID);

        boolean removed = EntityUtils.removeEntity(fromWorld, botUUID);
        cleanupDespawnState(ownerUUID, botUUID, removed, reason);
    }

    private void prepareBotDespawn(UUID ownerUUID) {
        ITrainingBot bot = registry.getBot(ownerUUID);
        if (bot != null) {
            BotBroadcaster.broadcastDespawn(bot);
            if (bot.getBotAI() != null) {
                bot.getBotAI().close();
            }
        }
    }

    private void cleanupDespawnState(UUID ownerUUID, @Nullable UUID botUUID, boolean removed, BotDespawnReason reason) {
        ITrainingBot bot = registry.getBot(ownerUUID);
        BotSnapshot snapshot = BotSnapshotMapper.toSnapshot(ownerUUID, bot);
        if (!removed && botUUID != null) {
            UltimateBotLogging.warn(
                    plugin.getLogger(),
                    "Bot",
                    "Despawn cleanup could not find entity " + botUUID + " for owner " + ownerUUID);
        }

        if (botUUID != null) {
            NMSBridgeManager.get().removeFromProfileCache(botUUID);
            plugin.forgetCompatibilityBot(botUUID);
        }

        registry.removeBot(ownerUUID);
        if (reason != BotDespawnReason.REPLACED) {
            clearCachedOptions(ownerUUID, bot);
        }
        if (snapshot != null) {
            BotEventSource source = BotEventSourceContext.currentOr(BotEventSource.GUI);
            plugin.getBotEventDispatcher()
                    .publish(new BotDespawnEvent(
                            plugin.getBotEventDispatcher().nextSequence(snapshot.requireBotUUID()),
                            snapshot,
                            source,
                            reason == BotDespawnReason.MANUAL
                                            && (source == BotEventSource.API || source == BotEventSource.REMOTE_API)
                                    ? BotDespawnReason.API_REQUEST
                                    : reason));
            plugin.getBotEventDispatcher().forget(snapshot.requireBotUUID());
        }
    }

    private void clearCachedOptions(UUID ownerUUID, @Nullable ITrainingBot bot) {
        plugin.getPlayerOptions().remove(ownerUUID);
        if (bot == null || bot.getBrainController() == null) {
            return;
        }
        BotOptions options = bot.getBrainController().getBotOptions();
        if (options == null) {
            return;
        }
        if (options.getOwnerUUID() != null) {
            plugin.getPlayerOptions().remove(options.getOwnerUUID());
        }
        for (UUID teamOwnerUUID : options.getTeamOwnerUUIDs()) {
            plugin.getPlayerOptions().remove(teamOwnerUUID);
        }
    }

    private boolean allowDespawn(UUID ownerUUID, BotDespawnReason requestedReason) {
        BotEventSource source = BotEventSourceContext.currentOr(BotEventSource.GUI);
        BotDespawnReason reason = requestedReason == BotDespawnReason.MANUAL
                        && (source == BotEventSource.API || source == BotEventSource.REMOTE_API)
                ? BotDespawnReason.API_REQUEST
                : requestedReason;
        if (reason != BotDespawnReason.MANUAL && reason != BotDespawnReason.API_REQUEST) return true;
        ITrainingBot bot = registry.getBot(ownerUUID);
        BotSnapshot snapshot = BotSnapshotMapper.toSnapshot(ownerUUID, bot);
        if (snapshot == null) return true;
        BotDespawnPrepareEvent event = plugin.getBotEventDispatcher()
                .publish(new BotDespawnPrepareEvent(
                        plugin.getBotEventDispatcher().nextSequence(snapshot.requireBotUUID()),
                        snapshot,
                        source,
                        reason));
        return !event.isCancelled();
    }
}
