package com.monkey.ultimatebot.world;

final class WorldProtectionPolicy {
    private WorldProtectionPolicy() {}

    static boolean shouldSuppressDrops(boolean antiDupe, boolean tracked) {
        return antiDupe && tracked;
    }

    static boolean shouldRestoreOnShutdown(boolean antiDupe, boolean tracked, boolean materialMatches) {
        return antiDupe && tracked && materialMatches;
    }
}
