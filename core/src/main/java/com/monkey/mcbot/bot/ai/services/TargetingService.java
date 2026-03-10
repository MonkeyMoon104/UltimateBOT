package com.monkey.mcbot.bot.ai.services;

import com.monkey.mcbot.bot.ai.ITrainingBot;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class TargetingService {

    private final Map<UUID, TargetCache> targetCache = new HashMap<>();
    private static final long GLOBAL_CACHE_TIME = 250;

    private static class TargetCache {
        Player player;
        long time;
        UUID excluded;
        UUID center;
        double maxRange;

        TargetCache(Player player, long time, UUID excluded, UUID center, double maxRange) {
            this.player = player;
            this.time = time;
            this.excluded = excluded;
            this.center = center;
            this.maxRange = maxRange;
        }

        boolean isValid(UUID expectedExcluded, UUID expectedCenter, double expectedMaxRange) {
            boolean excludedMatches = (expectedExcluded == null && excluded == null)
                    || (expectedExcluded != null && expectedExcluded.equals(excluded));
            boolean centerMatches = (expectedCenter == null && center == null)
                    || (expectedCenter != null && expectedCenter.equals(center));

            return player != null
                    && player.isOnline()
                    && !player.isDead()
                    && excludedMatches
                    && centerMatches
                    && Double.compare(maxRange, expectedMaxRange) == 0;
        }
    }

    public Player findClosestPlayer(ITrainingBot bot, double maxRange) {
        return findClosestPlayerExcept(bot, maxRange, null);
    }

    public Player findClosestPlayerFromList(ITrainingBot bot, double maxRange, Set<UUID> allowedTargets) {
        return findClosestPlayerNearPlayer(
                bot,
                (Player) bot.asPlayer().getBukkitEntity(),
                maxRange,
                null,
                allowedTargets
        );
    }

    public Player findClosestPlayerExcept(ITrainingBot bot, double maxRange, UUID excludedPlayer) {
        return findClosestPlayerNearPlayer(
                bot,
                (Player) bot.asPlayer().getBukkitEntity(),
                maxRange,
                excludedPlayer,
                Set.of()
        );
    }

    public Player findClosestPlayerNearPlayer(ITrainingBot bot,
                                              Player centerPlayer,
                                              double maxRange,
                                              UUID excludedPlayer) {
        return findClosestPlayerNearPlayer(bot, centerPlayer, maxRange, excludedPlayer, Set.of());
    }

    public Player findClosestPlayerNearPlayer(ITrainingBot bot,
                                              Player centerPlayer,
                                              double maxRange,
                                              UUID excludedPlayer,
                                              Set<UUID> allowedTargets) {
        if (centerPlayer == null || !centerPlayer.isOnline() || centerPlayer.isDead()) {
            targetCache.remove(bot.asPlayer().getUUID());
            return null;
        }

        UUID botUUID = bot.asPlayer().getUUID();
        UUID centerUUID = centerPlayer.getUniqueId();
        long currentTime = System.currentTimeMillis();
        boolean useAllowedTargetsFilter = allowedTargets != null && !allowedTargets.isEmpty();

        TargetCache cached = targetCache.get(botUUID);
        if (!useAllowedTargetsFilter
                && cached != null
                && (currentTime - cached.time) < GLOBAL_CACHE_TIME
                && cached.isValid(excludedPlayer, centerUUID, maxRange)) {
            return cached.player;
        }

        double closestDistanceSq = maxRange * maxRange;
        Player closestPlayer = null;

        org.bukkit.World centerWorld = centerPlayer.getWorld();
        double centerX = centerPlayer.getX();
        double centerY = centerPlayer.getY();
        double centerZ = centerPlayer.getZ();

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.isDead() || player.getGameMode().isInvulnerable()) {
                continue;
            }

            if (excludedPlayer != null && excludedPlayer.equals(player.getUniqueId())) {
                continue;
            }

            if (useAllowedTargetsFilter && !allowedTargets.contains(player.getUniqueId())) {
                continue;
            }

            if (!player.getWorld().getUID().equals(centerWorld.getUID())) {
                continue;
            }

            double dx = player.getX() - centerX;
            double dy = player.getY() - centerY;
            double dz = player.getZ() - centerZ;
            double distanceSq = dx * dx + dy * dy + dz * dz;

            if (distanceSq < closestDistanceSq) {
                closestDistanceSq = distanceSq;
                closestPlayer = player;
            }
        }

        if (closestPlayer != null) {
            if (!useAllowedTargetsFilter) {
                targetCache.put(botUUID, new TargetCache(closestPlayer, currentTime, excludedPlayer, centerUUID, maxRange));
            }
        } else {
            targetCache.remove(botUUID);
        }

        return closestPlayer;
    }

    public void invalidateCache(UUID botUUID) {
        targetCache.remove(botUUID);
    }

    public void clearCache() {
        targetCache.clear();
    }
}
