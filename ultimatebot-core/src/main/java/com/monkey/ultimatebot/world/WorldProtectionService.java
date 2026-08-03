package com.monkey.ultimatebot.world;

import com.monkey.ultimatebot.config.RuntimeSettings.WorldProtectionSettings;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BooleanSupplier;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.Nullable;

public final class WorldProtectionService implements AutoCloseable {
    private final Plugin plugin;
    private final Map<WorldBlockKey, TrackedWorldBlock> placements = new ConcurrentHashMap<>();
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
        boolean tracked = isTracked(block);
        BlockData originalData =
                tracked ? placements.get(WorldBlockKey.from(block)).originalData() : null;
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

    private boolean placementAllowed(
            Block block, BlockState replacedState, Material placementItem, org.bukkit.entity.Player placer) {
        if (!settings.respectProtectionPlugins()) {
            return true;
        }
        return WorldBlockEventDispatcher.placementAllowed(block, replacedState, placementItem, placer);
    }

    boolean isAntiDupeEnabled() {
        return settings.antiDupe();
    }

    boolean isTracked(Block block) {
        return placements.containsKey(WorldBlockKey.from(Objects.requireNonNull(block, "block")));
    }

    void forget(Block block) {
        TrackedWorldBlock placement = placements.remove(WorldBlockKey.from(block));
        if (placement != null) {
            placement.cancelExpiry();
        }
    }

    private @Nullable ScheduledTask scheduleExpiry(WorldBlockKey key, Location location) {
        int lifetimeSeconds = settings.combatBlockLifetimeSeconds();
        if (lifetimeSeconds == 0) {
            return null;
        }
        return Bukkit.getRegionScheduler()
                .runDelayed(plugin, location, ignored -> expire(key), Math.max(1L, lifetimeSeconds * 20L));
    }

    private void expire(WorldBlockKey key) {
        TrackedWorldBlock placement = placements.remove(key);
        Block block = key.block();
        if (placement != null && block != null && block.getType() == placement.material()) {
            block.setBlockData(placement.originalData(), false);
        }
    }

    @Override
    public void close() {
        for (Map.Entry<WorldBlockKey, TrackedWorldBlock> entry :
                Map.copyOf(placements).entrySet()) {
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
    }
}
