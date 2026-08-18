package com.monkey.ultimatebot.world;

import com.monkey.ultimatebot.access.block.BlockBreakAccess;
import com.monkey.ultimatebot.access.block.BlockPlaceAccess;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;

final class WorldBlockEventDispatcher {
    private WorldBlockEventDispatcher() {}

    static boolean placementAllowed(Block block, BlockState replacedState, Material placementItem, Player placer) {
        return BlockPlaceAccess.placementAllowed(block, replacedState, placementItem, placer);
    }

    static BreakResult requestBreak(Block block, Player breaker) {
        BlockBreakEvent event = new BlockBreakEvent(block, breaker);
        Bukkit.getPluginManager().callEvent(event);
        return new BreakResult(event.isCancelled(), BlockBreakAccess.isDropItems(event));
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
