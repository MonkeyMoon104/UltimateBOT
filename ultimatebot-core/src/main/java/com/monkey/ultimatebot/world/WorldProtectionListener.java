package com.monkey.ultimatebot.world;

import java.util.List;
import java.util.Objects;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public final class WorldProtectionListener implements Listener {
    private final WorldProtectionService protection;

    public WorldProtectionListener(WorldProtectionService protection) {
        this.protection = Objects.requireNonNull(protection, "protection");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void suppressTrackedBlockDrops(BlockBreakEvent event) {
        if (WorldProtectionPolicy.shouldSuppressDrops(
                protection.isAntiDupeEnabled(), protection.isTracked(event.getBlock()))) {
            event.setDropItems(false);
            event.setExpToDrop(0);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void forgetBrokenBlock(BlockBreakEvent event) {
        protection.forget(event.getBlock());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void suppressExplosionDrops(EntityExplodeEvent event) {
        destroyTrackedBlocksWithoutDrops(event.blockList());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void forgetExplodedBlocks(EntityExplodeEvent event) {
        event.blockList().forEach(protection::forget);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void suppressExplosionDrops(BlockExplodeEvent event) {
        destroyTrackedBlocksWithoutDrops(event.blockList());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void forgetExplodedBlocks(BlockExplodeEvent event) {
        event.blockList().forEach(protection::forget);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void preventFluidDrops(BlockFromToEvent event) {
        if (protection.isTracked(event.getBlock())) {
            event.setCancelled(true);
            return;
        }
        Block destination = event.getToBlock();
        if (WorldProtectionPolicy.shouldSuppressDrops(
                protection.isAntiDupeEnabled(), protection.isTracked(destination))) {
            protection.forget(destination);
            destination.setType(Material.AIR, false);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void forgetBurnedBlock(BlockBurnEvent event) {
        protection.forget(event.getBlock());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void forgetFadedBlock(BlockFadeEvent event) {
        protection.forget(event.getBlock());
    }

    private void destroyTrackedBlocksWithoutDrops(List<Block> blocks) {
        blocks.removeIf(block -> {
            if (!WorldProtectionPolicy.shouldSuppressDrops(
                    protection.isAntiDupeEnabled(), protection.isTracked(block))) {
                return false;
            }
            protection.forget(block);
            block.setType(Material.AIR, false);
            return true;
        });
    }
}
