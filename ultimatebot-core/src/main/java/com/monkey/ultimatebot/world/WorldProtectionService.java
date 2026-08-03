package com.monkey.ultimatebot.world;

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
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.Nullable;

public final class WorldProtectionService implements AutoCloseable {
    private final Plugin plugin;
    private final Map<BlockKey, Placement> placements = new ConcurrentHashMap<>();
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
        return placements.size() < settings.maxActiveCombatBlocks()
                && !placements.containsKey(BlockKey.from(block))
                && block.isPassable()
                && !block.isLiquid();
    }

    public synchronized boolean placeCombatBlock(
            Location location, Material material, org.bukkit.entity.Player placer, BooleanSupplier inventoryCommit) {
        Objects.requireNonNull(placer, "placer");
        Objects.requireNonNull(inventoryCommit, "inventoryCommit");
        if (!canPlaceCombatBlock(location, material)) {
            return false;
        }
        Block block = location.getBlock();
        BlockKey key = BlockKey.from(block);
        BlockData originalData = block.getBlockData();
        BlockState replacedState = block.getState();
        block.setType(material, true);
        if (!placementAllowed(block, replacedState, material, placer)) {
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
        Placement placement = new Placement(originalData, material, expiryTask);
        placements.put(key, placement);
        return true;
    }

    private boolean placementAllowed(
            Block block, BlockState replacedState, Material material, org.bukkit.entity.Player placer) {
        if (!settings.respectProtectionPlugins()) {
            return true;
        }
        BlockPlaceEvent event = new BlockPlaceEvent(
                block,
                replacedState,
                block.getRelative(0, -1, 0),
                new ItemStack(material),
                placer,
                true,
                EquipmentSlot.HAND);
        Bukkit.getPluginManager().callEvent(event);
        return !event.isCancelled() && event.canBuild();
    }

    boolean isAntiDupeEnabled() {
        return settings.antiDupe();
    }

    boolean isTracked(Block block) {
        return placements.containsKey(BlockKey.from(Objects.requireNonNull(block, "block")));
    }

    void forget(Block block) {
        Placement placement = placements.remove(BlockKey.from(block));
        if (placement != null) {
            placement.cancelExpiry();
        }
    }

    private @Nullable ScheduledTask scheduleExpiry(BlockKey key, Location location) {
        int lifetimeSeconds = settings.combatBlockLifetimeSeconds();
        if (lifetimeSeconds == 0) {
            return null;
        }
        return Bukkit.getRegionScheduler()
                .runDelayed(plugin, location, ignored -> expire(key), Math.max(1L, lifetimeSeconds * 20L));
    }

    private void expire(BlockKey key) {
        Placement placement = placements.remove(key);
        Block block = key.block();
        if (placement != null && block != null && block.getType() == placement.material()) {
            block.setBlockData(placement.originalData(), false);
        }
    }

    @Override
    public void close() {
        for (Map.Entry<BlockKey, Placement> entry : Map.copyOf(placements).entrySet()) {
            Placement placement = entry.getValue();
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

    private record Placement(
            BlockData originalData,
            Material material,
            @Nullable ScheduledTask expiryTask) {
        private void cancelExpiry() {
            if (expiryTask != null) {
                expiryTask.cancel();
            }
        }
    }

    private record BlockKey(UUID worldUUID, int x, int y, int z) {
        private static BlockKey from(Block block) {
            return new BlockKey(block.getWorld().getUID(), block.getX(), block.getY(), block.getZ());
        }

        private @Nullable Block block() {
            World world = Bukkit.getWorld(worldUUID);
            return world == null ? null : world.getBlockAt(x, y, z);
        }
    }
}
