package com.monkey.mcbot.bot.ai.services;

import com.monkey.mcbot.bot.ai.ITrainingBot;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TargetingService {

    private final Map<UUID, TargetCache> targetCache = new ConcurrentHashMap<>();
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
        return findClosestPlayerNearBot(bot, maxRange, null, Set.of());
    }

    public Player findClosestPlayerFromList(ITrainingBot bot, double maxRange, Set<UUID> allowedTargets) {
        return findClosestPlayerNearBot(bot, maxRange, null, allowedTargets);
    }

    public Player findClosestPlayerExcept(ITrainingBot bot, double maxRange, UUID excludedPlayer) {
        return findClosestPlayerNearBot(bot, maxRange, excludedPlayer, Set.of());
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
            if (!isValidCandidate(player, centerWorld)) {
                continue;
            }

            if (botUUID.equals(player.getUniqueId())) {
                continue;
            }

            if (excludedPlayer != null && excludedPlayer.equals(player.getUniqueId())) {
                continue;
            }

            if (useAllowedTargetsFilter && !allowedTargets.contains(player.getUniqueId())) {
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

    private Player findClosestPlayerNearBot(ITrainingBot bot,
                                            double maxRange,
                                            UUID excludedPlayer,
                                            Set<UUID> allowedTargets) {
        if (bot == null || bot.asPlayer() == null || bot.asPlayer().level() == null) {
            return null;
        }

        UUID botUUID = bot.asPlayer().getUUID();
        long currentTime = System.currentTimeMillis();
        boolean useAllowedTargetsFilter = allowedTargets != null && !allowedTargets.isEmpty();

        TargetCache cached = targetCache.get(botUUID);
        if (!useAllowedTargetsFilter
                && cached != null
                && (currentTime - cached.time) < GLOBAL_CACHE_TIME
                && cached.isValid(excludedPlayer, botUUID, maxRange)) {
            return cached.player;
        }

        org.bukkit.World centerWorld = bot.asPlayer().level().getWorld();
        if (centerWorld == null) {
            targetCache.remove(botUUID);
            return null;
        }

        double centerX = bot.asPlayer().getX();
        double centerY = bot.asPlayer().getY();
        double centerZ = bot.asPlayer().getZ();
        double closestDistanceSq = maxRange * maxRange;
        Player closestPlayer = null;

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!isValidCandidate(player, centerWorld)) {
                continue;
            }

            if (botUUID.equals(player.getUniqueId())) {
                continue;
            }

            if (excludedPlayer != null && excludedPlayer.equals(player.getUniqueId())) {
                continue;
            }

            if (useAllowedTargetsFilter && !allowedTargets.contains(player.getUniqueId())) {
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
                targetCache.put(botUUID, new TargetCache(closestPlayer, currentTime, excludedPlayer, botUUID, maxRange));
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

    private boolean isValidCandidate(Player player, org.bukkit.World centerWorld) {
        if (player == null || centerWorld == null) {
            return false;
        }

        org.bukkit.World playerWorld = player.getWorld();
        if (playerWorld == null || !playerWorld.getUID().equals(centerWorld.getUID())) {
            return false;
        }

        if (!player.isOnline() || player.isDead()) {
            return false;
        }

        GameMode gameMode = player.getGameMode();
        return gameMode == null || !gameMode.isInvulnerable();
    }
}
