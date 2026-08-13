package com.monkey.ultimatebot.world;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

final class WorldBlockEventDispatcher {
    private WorldBlockEventDispatcher() {}

    static boolean placementAllowed(Block block, BlockState replacedState, Material placementItem, Player placer) {
        BlockPlaceEvent event = new BlockPlaceEvent(
                block,
                replacedState,
                block.getRelative(0, -1, 0),
                new ItemStack(placementItem),
                placer,
                true,
                EquipmentSlot.HAND);
        Bukkit.getPluginManager().callEvent(event);
        return !event.isCancelled() && event.canBuild();
    }

    static BreakResult requestBreak(Block block, Player breaker) {
        BlockBreakEvent event = new BlockBreakEvent(block, breaker);
        Bukkit.getPluginManager().callEvent(event);
        return new BreakResult(event.isCancelled(), event.isDropItems());
    }

    static final class BreakResult {
        private final boolean cancelled;
        private final boolean dropItems;

        BreakResult(boolean cancelled, boolean dropItems) {
            this.cancelled = cancelled;
            this.dropItems = dropItems;
        }

        public boolean cancelled() {
            return cancelled;
        }
        public boolean dropItems() {
            return dropItems;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof BreakResult)) {
                return false;
            }
            BreakResult other = (BreakResult) obj;
            return cancelled == other.cancelled && dropItems == other.dropItems;
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(cancelled, dropItems);
        }

        @Override
        public String toString() {
            return "BreakResult[cancelled=" + cancelled + ", dropItems=" + dropItems + "]";
        }
    }
}
