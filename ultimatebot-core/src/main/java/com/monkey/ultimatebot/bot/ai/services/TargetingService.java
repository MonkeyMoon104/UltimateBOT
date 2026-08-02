package com.monkey.ultimatebot.bot.ai.services;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import com.monkey.ultimatebot.bot.BotOptions;
import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.config.RuntimeSettings;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;

public class TargetingService {

    private volatile Cache<UUID, TargetCache> targetCache;
    private CacheStats retiredCacheStats = CacheStats.empty();

    public TargetingService(RuntimeSettings.CacheSettings settings) {
        this.targetCache = createCache(settings);
    }

    public synchronized void reconfigure(RuntimeSettings.CacheSettings settings) {
        Cache<UUID, TargetCache> previousCache = targetCache;
        retiredCacheStats = retiredCacheStats.plus(previousCache.stats());
        targetCache = createCache(settings);
        previousCache.invalidateAll();
    }

    public synchronized CacheStats cacheStats() {
        return retiredCacheStats.plus(targetCache.stats());
    }

    public long estimatedCacheSize() {
        return targetCache.estimatedSize();
    }

    private static Cache<UUID, TargetCache> createCache(RuntimeSettings.CacheSettings settings) {
        return Caffeine.newBuilder()
                .maximumSize(settings.maximumSize())
                .expireAfterWrite(settings.expireAfterWrite())
                .recordStats()
                .build();
    }

    public Mob findClosestMob(ITrainingBot bot, double maxRange) {
        if (bot == null || bot.asPlayer() == null || bot.asPlayer().level() == null || maxRange <= 0.0D) {
            return null;
        }

        org.bukkit.World world = bot.asPlayer().level().getWorld();
        if (world == null) {
            return null;
        }

        org.bukkit.Location center = bot.asPlayer().getBukkitEntity().getLocation();
        double closestDistanceSq = maxRange * maxRange;
        Mob closest = null;
        for (org.bukkit.entity.Entity entity : world.getNearbyEntities(center, maxRange, maxRange, maxRange)) {
            if (!(entity instanceof Mob mob) || !mob.isValid() || mob.isDead() || mob.isInvulnerable()) {
                continue;
            }
            double distanceSq = mob.getLocation().distanceSquared(center);
            if (distanceSq < closestDistanceSq) {
                closestDistanceSq = distanceSq;
                closest = mob;
            }
        }
        return closest;
    }

    private static class TargetCache {
        Player player;
        UUID excluded;
        UUID center;
        double maxRange;

        TargetCache(Player player, UUID excluded, UUID center, double maxRange) {
            this.player = player;
            this.excluded = excluded;
            this.center = center;
            this.maxRange = maxRange;
        }

        boolean isValid(ITrainingBot bot, UUID expectedExcluded, UUID expectedCenter, double expectedMaxRange) {
            boolean excludedMatches = (expectedExcluded == null && excluded == null)
                    || (expectedExcluded != null && expectedExcluded.equals(excluded));
            boolean centerMatches = (expectedCenter == null && center == null)
                    || (expectedCenter != null && expectedCenter.equals(center));

            return player != null
                    && isCandidateOnline(bot, player)
                    && !player.isDead()
                    && excludedMatches
                    && centerMatches
                    && Double.compare(maxRange, expectedMaxRange) == 0;
        }
    }

    public Player findClosestPlayer(ITrainingBot bot, double maxRange) {
        return findClosestPlayerNearBot(bot, maxRange, null, Set.of());
    }

    public Player findClosestPlayer(ITrainingBot bot, double maxRange, Predicate<Player> candidateFilter) {
        return findClosestPlayerNearBot(bot, maxRange, null, Set.of(), candidateFilter);
    }

    public Player findClosestPlayerFromList(ITrainingBot bot, double maxRange, Set<UUID> allowedTargets) {
        return findClosestPlayerNearBot(bot, maxRange, null, allowedTargets);
    }

    public Player findClosestPlayerExcept(ITrainingBot bot, double maxRange, UUID excludedPlayer) {
        return findClosestPlayerNearBot(bot, maxRange, excludedPlayer, Set.of());
    }

    public Player findClosestPlayerNearPlayer(
            ITrainingBot bot, Player centerPlayer, double maxRange, UUID excludedPlayer) {
        return findClosestPlayerNearPlayer(bot, centerPlayer, maxRange, excludedPlayer, Set.of());
    }

    public Player findClosestPlayerNearPlayer(
            ITrainingBot bot, Player centerPlayer, double maxRange, UUID excludedPlayer, Set<UUID> allowedTargets) {
        if (centerPlayer == null || !centerPlayer.isOnline() || centerPlayer.isDead()) {
            targetCache.invalidate(bot.asPlayer().getUUID());
            return null;
        }

        UUID botUUID = bot.asPlayer().getUUID();
        UUID centerUUID = centerPlayer.getUniqueId();
        boolean useAllowedTargetsFilter = allowedTargets != null && !allowedTargets.isEmpty();

        TargetCache cached = targetCache.getIfPresent(botUUID);
        if (!useAllowedTargetsFilter && cached != null && cached.isValid(bot, excludedPlayer, centerUUID, maxRange)) {
            if (canTargetManagedBot(bot, cached.player.getUniqueId())) {
                return cached.player;
            }
            targetCache.invalidate(botUUID);
        }

        double closestDistanceSq = maxRange * maxRange;
        Player closestPlayer = null;

        org.bukkit.World centerWorld = centerPlayer.getWorld();
        double centerX = centerPlayer.getX();
        double centerY = centerPlayer.getY();
        double centerZ = centerPlayer.getZ();

        for (Player player : collectTargetCandidates(bot)) {
            if (!isValidCandidate(bot, player, centerWorld)) {
                continue;
            }

            if (botUUID.equals(player.getUniqueId())) {
                continue;
            }

            if (!canTargetManagedBot(bot, player.getUniqueId())) {
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
                targetCache.put(botUUID, new TargetCache(closestPlayer, excludedPlayer, centerUUID, maxRange));
            }
        } else {
            targetCache.invalidate(botUUID);
        }

        return closestPlayer;
    }

    private Player findClosestPlayerNearBot(
            ITrainingBot bot, double maxRange, UUID excludedPlayer, Set<UUID> allowedTargets) {
        return findClosestPlayerNearBot(bot, maxRange, excludedPlayer, allowedTargets, null);
    }

    private Player findClosestPlayerNearBot(
            ITrainingBot bot,
            double maxRange,
            UUID excludedPlayer,
            Set<UUID> allowedTargets,
            Predicate<Player> candidateFilter) {
        if (bot == null || bot.asPlayer() == null || bot.asPlayer().level() == null) {
            return null;
        }

        UUID botUUID = bot.asPlayer().getUUID();
        boolean useAllowedTargetsFilter = allowedTargets != null && !allowedTargets.isEmpty();
        boolean useDynamicFilter = candidateFilter != null;

        TargetCache cached = targetCache.getIfPresent(botUUID);
        if (!useAllowedTargetsFilter
                && !useDynamicFilter
                && cached != null
                && cached.isValid(bot, excludedPlayer, botUUID, maxRange)) {
            if (canTargetManagedBot(bot, cached.player.getUniqueId())) {
                return cached.player;
            }
            targetCache.invalidate(botUUID);
        }

        org.bukkit.World centerWorld = bot.asPlayer().level().getWorld();
        if (centerWorld == null) {
            targetCache.invalidate(botUUID);
            return null;
        }

        double centerX = bot.asPlayer().getX();
        double centerY = bot.asPlayer().getY();
        double centerZ = bot.asPlayer().getZ();
        double closestDistanceSq = maxRange * maxRange;
        Player closestPlayer = null;

        for (Player player : collectTargetCandidates(bot)) {
            if (!isValidCandidate(bot, player, centerWorld)) {
                continue;
            }

            if (botUUID.equals(player.getUniqueId())) {
                continue;
            }

            if (!canTargetManagedBot(bot, player.getUniqueId())) {
                continue;
            }

            if (excludedPlayer != null && excludedPlayer.equals(player.getUniqueId())) {
                continue;
            }

            if (useAllowedTargetsFilter && !allowedTargets.contains(player.getUniqueId())) {
                continue;
            }

            if (candidateFilter != null && !candidateFilter.test(player)) {
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
            if (!useAllowedTargetsFilter && !useDynamicFilter) {
                targetCache.put(botUUID, new TargetCache(closestPlayer, excludedPlayer, botUUID, maxRange));
            }
        } else {
            targetCache.invalidate(botUUID);
        }

        return closestPlayer;
    }

    public void invalidateCache(UUID botUUID) {
        targetCache.invalidate(botUUID);
    }

    public void clearCache() {
        targetCache.invalidateAll();
    }

    private boolean canTargetManagedBot(ITrainingBot bot, UUID candidateUUID) {
        if (bot == null || candidateUUID == null || bot.getBrainController() == null) {
            return true;
        }
        BotOptions options = bot.getBrainController().getBotOptions();
        if (options == null || options.isAttackBots()) {
            return true;
        }
        return options.getTraining().getBotRegistry().getOwnerUUIDByBotUUID(candidateUUID) == null;
    }

    private List<Player> collectTargetCandidates(ITrainingBot bot) {
        Map<UUID, Player> candidates = new LinkedHashMap<>();
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online != null) {
                candidates.put(online.getUniqueId(), online);
            }
        }

        BotOptions options = bot == null || bot.getBrainController() == null
                ? null
                : bot.getBrainController().getBotOptions();
        if (options == null || !options.isAttackBots()) {
            return new ArrayList<>(candidates.values());
        }

        for (ITrainingBot managedBot :
                options.getTraining().getBotRegistry().getAllBots().values()) {
            if (managedBot == null || managedBot.asPlayer() == null) {
                continue;
            }
            if (managedBot.asPlayer().getBukkitEntity() instanceof Player managedPlayer) {
                candidates.put(managedPlayer.getUniqueId(), managedPlayer);
            }
        }

        return new ArrayList<>(candidates.values());
    }

    private boolean isValidCandidate(ITrainingBot bot, Player player, org.bukkit.World centerWorld) {
        if (player == null || centerWorld == null) {
            return false;
        }

        org.bukkit.World playerWorld = player.getWorld();
        if (playerWorld == null || !playerWorld.getUID().equals(centerWorld.getUID())) {
            return false;
        }

        if (!isCandidateOnline(bot, player) || player.isDead()) {
            return false;
        }

        GameMode gameMode = player.getGameMode();
        return gameMode == null || !gameMode.isInvulnerable();
    }

    private static boolean isCandidateOnline(ITrainingBot bot, Player player) {
        if (player == null) {
            return false;
        }
        if (player.isOnline()) {
            return true;
        }
        BotOptions options = bot == null || bot.getBrainController() == null
                ? null
                : bot.getBrainController().getBotOptions();
        return options != null
                && options.isAttackBots()
                && options.getTraining().getBotRegistry().getOwnerUUIDByBotUUID(player.getUniqueId()) != null;
    }
}
