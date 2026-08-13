package com.monkey.ultimatebot.world;

import org.bukkit.Material;

final class WorldProtectionPolicy {
    private WorldProtectionPolicy() {}

    static boolean shouldSuppressDrops(boolean antiDupe, boolean tracked) {
        return antiDupe && tracked;
    }

    static boolean shouldRestoreOnShutdown(boolean antiDupe, boolean tracked, boolean materialMatches) {
        return antiDupe && tracked && materialMatches;
    }

    static boolean shouldRemoveEntityOnShutdown(boolean antiDupe, boolean tracked) {
        return antiDupe && tracked;
    }

    static boolean hasPlacementSupport(Material material, boolean supportedBelow) {
        if (material == Material.COBWEB || material == Material.WATER) {
            return supportedBelow;
        }
        return true;
    }
}
