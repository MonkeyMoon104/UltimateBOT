package com.monkey.ultimatebot.bot.ai.services;


import java.util.Collections;
import com.monkey.ultimatebot.bot.BotOptions;
import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.bot.ai.services.cache.TargetCacheStats;
import com.monkey.ultimatebot.bot.ai.services.cache.UuidCache;
import com.monkey.ultimatebot.bot.ai.services.cache.UuidCaches;
import com.monkey.ultimatebot.config.RuntimeSettings;
import com.monkey.ultimatebot.compat.EntityCoordsAccess;
import com.monkey.ultimatebot.compat.EntityInvulnerableAccess;
import com.monkey.ultimatebot.compat.GameModeAccess;
import com.monkey.ultimatebot.compat.WorldAccess;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

public class TargetingService {

    private static final long MOB_CACHE_TTL_MS = 250L;

    private volatile UuidCache<TargetCache> targetCache;
    private TargetCacheStats retiredCacheStats = TargetCacheStats.empty();
    private final Map<UUID, MobCache> mobTargetCache = new java.util.concurrent.ConcurrentHashMap<>();

    public TargetingService(RuntimeSettings.CacheSettings settings) {
        this.targetCache = UuidCaches.create(settings);
    }

    public synchronized void reconfigure(RuntimeSettings.CacheSettings settings) {
        UuidCache<TargetCache> previousCache = targetCache;
        retiredCacheStats = retiredCacheStats.plus(previousCache.stats());
        targetCache = UuidCaches.create(settings);
        previousCache.invalidateAll();
    }

    public synchronized TargetCacheStats cacheStats() {
        return retiredCacheStats.plus(targetCache.stats());
    }

    public long estimatedCacheSize() {
        return targetCache.estimatedSize();
    }

    /**
     * Closest hostile/neutral living non-player near the bot.
     *
     * <p>Uses {@link LivingEntity} instead of {@code org.bukkit.entity.Mob} — that interface does not
     * exist before 1.13/1.14 and hard links crash 1.12 with {@code NoClassDefFoundError}.
     */
    public @Nullable LivingEntity findClosestMob(@Nullable ITrainingBot bot, double maxRange) {
        if (bot == null || bot.asBukkitPlayer() == null || maxRange <= 0.0D) {
            return null;
        }

        UUID botUUID = bot.getUniqueId();
        MobCache cached = mobTargetCache.get(botUUID);
        long now = System.currentTimeMillis();
        if (cached != null
                && now - cached.cachedAtMs < MOB_CACHE_TTL_MS
                && Double.compare(cached.maxRange, maxRange) == 0
                && cached.mob != null
                && cached.mob.isValid()
                && !cached.mob.isDead()
                && cached.mob.getWorld() != null
                && cached.mob.getWorld().equals(bot.asBukkitPlayer().getWorld())
                && cached.mob.getLocation().distanceSquared(
                        java.util.Objects.requireNonNull(bot.asBukkitPlayer().getLocation(), "bot location"))
                        <= maxRange * maxRange) {
            return cached.mob;
        }

        org.bukkit.World world = bot.asBukkitPlayer().getWorld();
        if (world == null) {
            return null;
        }

        org.bukkit.Location center = java.util.Objects.requireNonNull(bot.asBukkitPlayer().getLocation(), "bot location");
        double closestDistanceSq = maxRange * maxRange;
        LivingEntity closest = null;
        for (org.bukkit.entity.Entity entity :
                WorldAccess.nearbyEntities(world, center, maxRange, maxRange, maxRange, null)) {
            if (!(entity instanceof LivingEntity) || entity instanceof Player || entity instanceof ArmorStand) {
                continue;
            }
            LivingEntity mob = (LivingEntity) entity;
            if (!mob.isValid() || mob.isDead() || EntityInvulnerableAccess.isInvulnerable(mob)) {
                continue;
            }
            double distanceSq = mob.getLocation().distanceSquared(center);
            if (distanceSq < closestDistanceSq) {
                closestDistanceSq = distanceSq;
                closest = mob;
            }
        }
        mobTargetCache.put(botUUID, new MobCache(closest, maxRange, now));
        return closest;
    }

    private static final class MobCache {
        private final @Nullable LivingEntity mob;
        private final double maxRange;
        private final long cachedAtMs;

        private MobCache(@Nullable LivingEntity mob, double maxRange, long cachedAtMs) {
            this.mob = mob;
            this.maxRange = maxRange;
            this.cachedAtMs = cachedAtMs;
        }
    }

    private static class TargetCache {
        Player player;

        @Nullable UUID excluded;

        @Nullable UUID center;

        double maxRange;

        TargetCache(Player player, @Nullable UUID excluded, @Nullable UUID center, double maxRange) {
            this.player = player;
            this.excluded = excluded;
            this.center = center;
            this.maxRange = maxRange;
        }

        boolean isValid(
                ITrainingBot bot,
                @Nullable UUID expectedExcluded,
                @Nullable UUID expectedCenter,
                double expectedMaxRange) {
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

    public @Nullable Player findClosestPlayer(ITrainingBot bot, double maxRange) {
        return findClosestPlayerNearBot(bot, maxRange, null, Collections.emptySet());
    }

    public @Nullable Player findClosestPlayer(ITrainingBot bot, double maxRange, Predicate<Player> candidateFilter) {
        return findClosestPlayerNearBot(bot, maxRange, null, Collections.emptySet(), candidateFilter);
    }

    public @Nullable Player findClosestPlayerFromList(ITrainingBot bot, double maxRange, Set<UUID> allowedTargets) {
        return findClosestPlayerNearBot(bot, maxRange, null, allowedTargets);
    }

    public @Nullable Player findClosestPlayerExcept(ITrainingBot bot, double maxRange, @Nullable UUID excludedPlayer) {
        return findClosestPlayerNearBot(bot, maxRange, excludedPlayer, Collections.emptySet());
    }

    public @Nullable Player findClosestPlayerNearPlayer(
            ITrainingBot bot, @Nullable Player centerPlayer, double maxRange, @Nullable UUID excludedPlayer) {
        return findClosestPlayerNearPlayer(bot, centerPlayer, maxRange, excludedPlayer, Collections.emptySet());
    }

    public @Nullable Player findClosestPlayerNearPlayer(
            ITrainingBot bot,
            @Nullable Player centerPlayer,
            double maxRange,
            @Nullable UUID excludedPlayer,
            Set<UUID> allowedTargets) {
        if (centerPlayer == null || !centerPlayer.isOnline() || centerPlayer.isDead()) {
            targetCache.invalidate(bot.getUniqueId());
            return null;
        }

        UUID botUUID = bot.getUniqueId();
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
        double centerX = EntityCoordsAccess.getX(centerPlayer);
        double centerY = EntityCoordsAccess.getY(centerPlayer);
        double centerZ = EntityCoordsAccess.getZ(centerPlayer);

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

            double dx = EntityCoordsAccess.getX(player) - centerX;
            double dy = EntityCoordsAccess.getY(player) - centerY;
            double dz = EntityCoordsAccess.getZ(player) - centerZ;
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

    private @Nullable Player findClosestPlayerNearBot(
            ITrainingBot bot, double maxRange, @Nullable UUID excludedPlayer, Set<UUID> allowedTargets) {
        return findClosestPlayerNearBot(bot, maxRange, excludedPlayer, allowedTargets, null);
    }

    private @Nullable Player findClosestPlayerNearBot(
            ITrainingBot bot,
            double maxRange,
            @Nullable UUID excludedPlayer,
            Set<UUID> allowedTargets,
            @Nullable Predicate<Player> candidateFilter) {
        if (bot == null || bot.asBukkitPlayer() == null) {
            return null;
        }

        UUID botUUID = bot.getUniqueId();
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

        org.bukkit.World centerWorld = bot.asBukkitPlayer().getWorld();
        if (centerWorld == null) {
            targetCache.invalidate(botUUID);
            return null;
        }

        double centerX = EntityCoordsAccess.getX(bot.asBukkitPlayer());
        double centerY = EntityCoordsAccess.getY(bot.asBukkitPlayer());
        double centerZ = EntityCoordsAccess.getZ(bot.asBukkitPlayer());
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

            double dx = EntityCoordsAccess.getX(player) - centerX;
            double dy = EntityCoordsAccess.getY(player) - centerY;
            double dz = EntityCoordsAccess.getZ(player) - centerZ;
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
            if (managedBot == null || managedBot.asBukkitPlayer() == null) {
                continue;
            }
            Player managedPlayer = managedBot.asBukkitPlayer();
            candidates.put(managedPlayer.getUniqueId(), managedPlayer);
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
        return gameMode == null || !GameModeAccess.isInvulnerable(gameMode);
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
