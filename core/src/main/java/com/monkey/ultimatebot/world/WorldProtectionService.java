package com.monkey.ultimatebot.world;


import java.util.Collections;
import com.monkey.ultimatebot.config.RuntimeSettings.WorldProtectionSettings;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BooleanSupplier;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.Nullable;

public final class WorldProtectionService implements AutoCloseable {
    private final Plugin plugin;
    private final Map<WorldBlockKey, TrackedWorldBlock> placements = new ConcurrentHashMap<>();
    private final Map<UUID, TrackedWorldEntity> combatEntities = new ConcurrentHashMap<>();
    private volatile WorldProtectionSettings settings;

    public WorldProtectionService(Plugin plugin, WorldProtectionSettings settings) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.settings = Objects.requireNonNull(settings, "settings");
    }

    public void reconfigure(WorldProtectionSettings settings) {
        this.settings = Objects.requireNonNull(settings, "settings");
    }

    public synchronized boolean canPlaceCombatBlock(Location location, Material material) {
        Objects.requireNonNull(location, "location");
        Objects.requireNonNull(material, "material");
        Block block = location.getBlock();
        Material materialBelow =
                block.getRelative(org.bukkit.block.BlockFace.DOWN).getType();
        return placements.size() < settings.maxActiveCombatBlocks()
                && !placements.containsKey(WorldBlockKey.from(block))
                && block.isPassable()
                && !block.isLiquid()
                && WorldProtectionPolicy.hasPlacementSupport(
                        material, materialBelow == Material.COBWEB || materialBelow.isSolid());
    }

    public synchronized boolean placeCombatBlock(
            Location location, Material material, org.bukkit.entity.Player placer, BooleanSupplier inventoryCommit) {
        return placeCombatBlock(location, material, material, placer, inventoryCommit);
    }

    public synchronized boolean placeCombatBlock(
            Location location,
            Material material,
            Material placementItem,
            org.bukkit.entity.Player placer,
            BooleanSupplier inventoryCommit) {
        Objects.requireNonNull(location, "location");
        Objects.requireNonNull(material, "material");
        Objects.requireNonNull(placementItem, "placementItem");
        Objects.requireNonNull(placer, "placer");
        Objects.requireNonNull(inventoryCommit, "inventoryCommit");
        if (!canPlaceCombatBlock(location, material)) {
            return false;
        }
        Block block = location.getBlock();
        WorldBlockKey key = WorldBlockKey.from(block);
        BlockData originalData = block.getBlockData();
        BlockState replacedState = block.getState();
        block.setType(material, false);
        if (!placementAllowed(block, replacedState, placementItem, placer)) {
            block.setBlockData(originalData, false);
            return false;
        }
        ScheduledTask expiryTask;
        try {
            expiryTask = scheduleExpiry(key, location);
        } catch (RuntimeException schedulingError) {
            block.setBlockData(originalData, false);
            throw new IllegalStateException("Could not schedule combat block expiry", schedulingError);
        }
        if (!inventoryCommit.getAsBoolean()) {
            if (expiryTask != null) {
                expiryTask.cancel();
            }
            block.setBlockData(originalData, false);
            return false;
        }
        TrackedWorldBlock placement = new TrackedWorldBlock(originalData, material, expiryTask);
        placements.put(key, placement);
        return true;
    }

    public synchronized boolean breakCombatBlock(Location location, org.bukkit.entity.Player breaker, ItemStack tool) {
        Objects.requireNonNull(location, "location");
        Objects.requireNonNull(breaker, "breaker");
        Objects.requireNonNull(tool, "tool");
        Block block = location.getBlock();
        if (block.getType().isAir()) {
            return false;
        }
        TrackedWorldBlock placement = placements.get(WorldBlockKey.from(block));
        boolean tracked = placement != null;
        BlockData originalData = placement == null ? null : placement.originalData();
        WorldBlockEventDispatcher.BreakResult result = WorldBlockEventDispatcher.requestBreak(block, breaker);
        if (result.cancelled()) {
            return false;
        }
        if (WorldProtectionPolicy.shouldSuppressDrops(settings.antiDupe(), tracked)) {
            block.setBlockData(Objects.requireNonNull(originalData, "tracked block data"), false);
        } else if (result.dropItems()) {
            block.breakNaturally(tool, true);
        } else {
            block.setType(Material.AIR, false);
        }
        forget(block);
        return true;
    }

    public synchronized void restoreCombatBlock(Location location) {
        Objects.requireNonNull(location, "location");
        Block block = location.getBlock();
        TrackedWorldBlock placement = placements.remove(WorldBlockKey.from(block));
        if (placement == null) {
            return;
        }
        placement.cancelExpiry();
        if (block.getType() == placement.material()) {
            block.setBlockData(placement.originalData(), false);
        }
    }

    public synchronized boolean trackCombatEntity(
            Entity entity, boolean explosionBlockDamageAllowed, BooleanSupplier inventoryCommit) {
        Objects.requireNonNull(entity, "entity");
        Objects.requireNonNull(inventoryCommit, "inventoryCommit");
        pruneCombatEntities();
        UUID entityId = entity.getUniqueId();
        if (!entity.isValid()
                || combatEntities.containsKey(entityId)
                || combatEntities.size() >= settings.maxActiveCombatEntities()) {
            entity.remove();
            return false;
        }
        ScheduledTask expiryTask;
        try {
            expiryTask = scheduleEntityExpiry(entity);
        } catch (RuntimeException schedulingError) {
            entity.remove();
            throw new IllegalStateException("Could not schedule combat entity expiry", schedulingError);
        }
        if (!inventoryCommit.getAsBoolean()) {
            if (expiryTask != null) {
                expiryTask.cancel();
            }
            entity.remove();
            return false;
        }
        combatEntities.put(entityId, new TrackedWorldEntity(explosionBlockDamageAllowed, expiryTask));
        return true;
    }

    public synchronized void removeCombatEntity(Entity entity) {
        Objects.requireNonNull(entity, "entity");
        forgetCombatEntity(entity);
        entity.remove();
    }

    private boolean placementAllowed(
            Block block, BlockState replacedState, Material placementItem, org.bukkit.entity.Player placer) {
        if (!settings.respectProtectionPlugins()) {
            return true;
        }
        // Fake-player bots are not in the PlayerList; protection plugins often cancel their
        // BlockPlaceEvent. Combat rails/webs still need to place for Cart/UHC modes.
        if (plugin instanceof com.monkey.ultimatebot.UltimateBot) {
            com.monkey.ultimatebot.UltimateBot ultimateBot = (com.monkey.ultimatebot.UltimateBot) plugin;
            if (ultimateBot.getBotRegistry().getOwnerUUIDByBotUUID(placer.getUniqueId()) != null) {
                return true;
            }
        }
        return WorldBlockEventDispatcher.placementAllowed(block, replacedState, placementItem, placer);
    }

    boolean isAntiDupeEnabled() {
        return settings.antiDupe();
    }

    boolean isTracked(Block block) {
        return placements.containsKey(WorldBlockKey.from(Objects.requireNonNull(block, "block")));
    }

    boolean isTracked(Entity entity) {
        return combatEntities.containsKey(
                Objects.requireNonNull(entity, "entity").getUniqueId());
    }

    boolean isExplosionBlockDamageAllowed(Entity entity) {
        TrackedWorldEntity tracked =
                combatEntities.get(Objects.requireNonNull(entity, "entity").getUniqueId());
        return tracked == null || tracked.explosionBlockDamageAllowed();
    }

    void forget(Block block) {
        TrackedWorldBlock placement = placements.remove(WorldBlockKey.from(block));
        if (placement != null) {
            placement.cancelExpiry();
        }
    }

    void forgetCombatEntity(Entity entity) {
        forgetCombatEntity(entity.getUniqueId());
    }

    private @Nullable ScheduledTask scheduleExpiry(WorldBlockKey key, Location location) {
        int lifetimeSeconds = settings.combatBlockLifetimeSeconds();
        if (lifetimeSeconds == 0) {
            return null;
        }
        return Bukkit.getRegionScheduler()
                .runDelayed(plugin, location, ignored -> expire(key), Math.max(1L, lifetimeSeconds * 20L));
    }

    private @Nullable ScheduledTask scheduleEntityExpiry(Entity entity) {
        int lifetimeSeconds = settings.combatEntityLifetimeSeconds();
        if (lifetimeSeconds == 0) {
            return null;
        }
        UUID entityId = entity.getUniqueId();
        return entity.getScheduler()
                .runDelayed(
                        plugin,
                        ignored -> expireCombatEntity(entityId),
                        () -> forgetCombatEntity(entityId),
                        Math.max(1L, lifetimeSeconds * 20L));
    }

    private void expire(WorldBlockKey key) {
        TrackedWorldBlock placement = placements.remove(key);
        Block block = key.block();
        if (placement != null && block != null && block.getType() == placement.material()) {
            block.setBlockData(placement.originalData(), false);
        }
    }

    private synchronized void expireCombatEntity(UUID entityId) {
        TrackedWorldEntity tracked = combatEntities.remove(entityId);
        Entity entity = Bukkit.getEntity(entityId);
        if (tracked != null && entity != null) {
            entity.remove();
        }
    }

    private synchronized void forgetCombatEntity(UUID entityId) {
        TrackedWorldEntity tracked = combatEntities.remove(entityId);
        if (tracked != null) {
            tracked.cancelExpiry();
        }
    }

    private void pruneCombatEntities() {
        combatEntities.entrySet().removeIf(entry -> {
            Entity entity = Bukkit.getEntity(entry.getKey());
            if (entity != null && entity.isValid()) {
                return false;
            }
            entry.getValue().cancelExpiry();
            return true;
        });
    }

    @Override
    public void close() {
        for (Map.Entry<WorldBlockKey, TrackedWorldBlock> entry :
                com.monkey.ultimatebot.common.util.ImmutableCollections.copyOf(placements).entrySet()) {
            TrackedWorldBlock placement = entry.getValue();
            placement.cancelExpiry();
            Block block = entry.getKey().block();
            if (block != null
                    && WorldProtectionPolicy.shouldRestoreOnShutdown(
                            settings.antiDupe(), true, block.getType() == placement.material())) {
                block.setBlockData(placement.originalData(), false);
            }
        }
        placements.clear();
        for (Map.Entry<UUID, TrackedWorldEntity> entry :
                com.monkey.ultimatebot.common.util.ImmutableCollections.copyOf(combatEntities).entrySet()) {
            entry.getValue().cancelExpiry();
            Entity entity = Bukkit.getEntity(entry.getKey());
            if (entity != null && WorldProtectionPolicy.shouldRemoveEntityOnShutdown(settings.antiDupe(), true)) {
                entity.remove();
            }
        }
        combatEntities.clear();
    }
}
