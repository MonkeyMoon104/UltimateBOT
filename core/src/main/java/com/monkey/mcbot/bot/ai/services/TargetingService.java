package com.monkey.mcbot.bot.ai.services;

import com.monkey.mcbot.bot.ai.ITrainingBot;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TargetingService {

    private final Map<UUID, TargetCache> targetCache = new HashMap<>();
    private static final long GLOBAL_CACHE_TIME = 250;
    private static final double MAX_RANGE = 64.0;
    private static final double MAX_RANGE_SQUARED = MAX_RANGE * MAX_RANGE;

    private static class TargetCache {
        Player player;
        long time;

        TargetCache(Player player, long time) {
            this.player = player;
            this.time = time;
        }

        boolean isValid() {
            return player != null && player.isOnline() && !player.isDead();
        }
    }

    public Player findClosestPlayer(ITrainingBot bot, double maxRange) {
        UUID botUUID = bot.asPlayer().getUUID();
        long currentTime = System.currentTimeMillis();

        TargetCache cached = targetCache.get(botUUID);
        if (cached != null && (currentTime - cached.time) < GLOBAL_CACHE_TIME && cached.isValid()) {
            return cached.player;
        }

        double closestDistanceSq = maxRange * maxRange;
        Player closestPlayer = null;

        org.bukkit.World botWorld = bot.asPlayer().level().getWorld();
        double botX = bot.asPlayer().getX();
        double botY = bot.asPlayer().getY();
        double botZ = bot.asPlayer().getZ();

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.isDead() || player.getGameMode().isInvulnerable()) {
                continue;
            }

            if (player.getWorld() != botWorld) {
                continue;
            }

            double dx = player.getX() - botX;
            double dy = player.getY() - botY;
            double dz = player.getZ() - botZ;
            double distanceSq = dx * dx + dy * dy + dz * dz;

            if (distanceSq < closestDistanceSq) {
                closestDistanceSq = distanceSq;
                closestPlayer = player;
            }
        }

        if (closestPlayer != null) {
            targetCache.put(botUUID, new TargetCache(closestPlayer, currentTime));
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