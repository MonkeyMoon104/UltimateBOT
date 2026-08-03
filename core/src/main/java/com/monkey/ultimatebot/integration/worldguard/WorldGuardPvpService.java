package com.monkey.ultimatebot.integration.worldguard;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.logging.UltimateBotLogging;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public final class WorldGuardPvpService {

    private final UltimateBot plugin;
    private final boolean available;

    public WorldGuardPvpService(UltimateBot plugin) {
        this.plugin = plugin;
        this.available = Bukkit.getPluginManager().isPluginEnabled("WorldGuard");
        if (available) {
            UltimateBotLogging.info(plugin.getLogger(), "WorldGuard", "WorldGuard PvP checks enabled");
        }
    }

    public boolean isAvailable() {
        return available;
    }

    public boolean isPvpAllowed(Location location) {
        if (!available || location == null || location.getWorld() == null) {
            return true;
        }

        try {
            RegionQuery query =
                    WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
            return query.testState(BukkitAdapter.adapt(location), null, Flags.PVP);
        } catch (Throwable error) {
            UltimateBotLogging.warn(
                    plugin.getLogger(), "WorldGuard", "Failed to check PvP flag: " + error.getMessage());
            return true;
        }
    }
}
