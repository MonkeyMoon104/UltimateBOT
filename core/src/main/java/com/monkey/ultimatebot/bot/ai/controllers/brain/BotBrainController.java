package com.monkey.ultimatebot.bot.ai.controllers.brain;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.api.event.state.BotTargetChangeEvent;
import com.monkey.ultimatebot.api.model.runtime.BotSnapshot;
import com.monkey.ultimatebot.bot.BotOptions;
import com.monkey.ultimatebot.bot.BotType;
import com.monkey.ultimatebot.bot.ai.BotAI;
import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.bot.ai.services.TargetingService;
import com.monkey.ultimatebot.common.model.BotTargetMode;
import com.monkey.ultimatebot.utils.ChatColorUtils;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.jspecify.annotations.Nullable;

public class BotBrainController {

    private enum AllyAlertState {
        NONE,
        PRE_RANGE,
        RANGE
    }

    private static class ThreatSelection {
        private final org.bukkit.entity.Player threat;
        private final double distanceSq;

        private ThreatSelection(org.bukkit.entity.Player threat, double distanceSq) {
            this.threat = threat;
            this.distanceSq = distanceSq;
        }
    }

    private final ITrainingBot bot;
    private final BotAI botAI;
    private final UltimateBot plugin;
    private org.bukkit.entity.@Nullable Player targetPlayer;
    private @Nullable LivingEntity activeTarget;
    private final org.bukkit.entity.@Nullable Player ownerPlayer;
    private final BotOptions botOptions;
    private boolean follow;
    private boolean combat;
    private final TargetingService targetingService;

    private final double eventTargetRange;
    private final double allyRange;
    private final double allyPreRange;
    private final double allyReturnTeleportDistance;
    private final long allyReturnTeleportCooldownMs;
    private final String allyPreRangeAlertMessage;
    private final String allyRangeAlertMessage;

    private final double teamAllyRange;
    private final double teamAllyPreRange;
    private final double teamAllyReturnTeleportDistance;
    private final long teamAllyReturnTeleportCooldownMs;
    private final String teamAllyPreRangeAlertMessage;
    private final String teamAllyRangeAlertMessage;

    private long lastReturnTeleport = 0;

    private boolean watchOnlyMode = false;
    private AllyAlertState lastAlertState = AllyAlertState.NONE;
    private @Nullable UUID lastAlertTarget;

    public BotBrainController(
            ITrainingBot bot,
            UltimateBot plugin,
            org.bukkit.entity.@Nullable Player targetPlayer,
            boolean follow,
            BotOptions botOptions) {
        java.util.Objects.requireNonNull(bot, "bot");
        java.util.Objects.requireNonNull(plugin, "plugin");
        java.util.Objects.requireNonNull(botOptions, "botOptions");
        this.targetingService = plugin.getTargetingService();
        this.bot = bot;
        this.plugin = plugin;
        this.targetPlayer = targetPlayer;
        this.ownerPlayer = targetPlayer;
        this.follow = follow;
        this.botAI = new BotAI(bot, plugin, botOptions);
        this.botOptions = botOptions;
        this.combat = botOptions.isCombat();

        this.eventTargetRange = plugin.getConfig().getDouble("bot.event.range");

        this.allyRange = plugin.getConfig().getDouble("bot.ally.range");
        this.allyPreRange = plugin.getConfig().getDouble("bot.ally.pre-range");
        this.allyReturnTeleportDistance = plugin.getConfig().getDouble("bot.ally.return-teleport-distance");
        this.allyReturnTeleportCooldownMs = plugin.getConfig().getLong("bot.ally.return-teleport-cooldown-ms");
        this.allyPreRangeAlertMessage = plugin.getLangString("messages.ally.pre-range-alert");
        this.allyRangeAlertMessage = plugin.getLangString("messages.ally.range-alert");

        this.teamAllyRange = plugin.getConfig().getDouble("bot.team-ally.range");
        this.teamAllyPreRange = plugin.getConfig().getDouble("bot.team-ally.pre-range");
        this.teamAllyReturnTeleportDistance = plugin.getConfig().getDouble("bot.team-ally.return-teleport-distance");
        this.teamAllyReturnTeleportCooldownMs = plugin.getConfig().getLong("bot.team-ally.return-teleport-cooldown-ms");
        this.teamAllyPreRangeAlertMessage = plugin.getLangString("messages.team-ally.pre-range-alert");
        this.teamAllyRangeAlertMessage = plugin.getLangString("messages.team-ally.range-alert");

        configureBotAI();
    }

    private void configureBotAI() {
        if (targetPlayer != null && follow) {
            botAI.getRotationController().lookAt(targetPlayer.getX(), targetPlayer.getEyeLocation().getY(), targetPlayer.getZ());
        }
    }

    public void onTick() {
        updateTargetByType();

        LivingEntity selectedTarget = selectActiveTarget();
        if (!sameTarget(activeTarget, selectedTarget)) {
            UUID ownerUUID =
                    plugin.getBotRegistry().getOwnerUUIDByBotUUID(bot.getUniqueId());
            BotSnapshot snapshot =
                    ownerUUID == null ? null : plugin.getBotEventDispatcher().snapshot(ownerUUID, bot);
            if (snapshot != null) {
                BotTargetChangeEvent event = plugin.getBotEventDispatcher()
                        .publish(new BotTargetChangeEvent(
                                plugin.getBotEventDispatcher()
                                        .nextSequence(bot.getUniqueId()),
                                snapshot,
                                activeTarget,
                                selectedTarget));
                selectedTarget = event.isCancelled() ? activeTarget : event.getNewTarget();
            }
            botAI.customBrainTargetChanged(activeTarget, selectedTarget);
        }
        activeTarget = selectedTarget;
        if (selectedTarget == null) {
            if (shouldFollowPlayerAnchor()) {
                botAI.tick(java.util.Objects.requireNonNull(targetPlayer, "follow target"), false);
                return;
            }
            botAI.tickIdle();
            return;
        }

        boolean playerTarget = selectedTarget instanceof org.bukkit.entity.Player;
        boolean allowCombat = playerTarget ? shouldUseCombatOnCurrentTarget() : combat;

        if (playerTarget && watchOnlyMode) {
            botAI.getMovementController().clearPath();
            return;
        }

        if (follow || allowCombat) {
            botAI.tick(selectedTarget, allowCombat);
        } else {
            botAI.tickIdle();
        }
    }

    private boolean sameTarget(@Nullable LivingEntity first, @Nullable LivingEntity second) {
        if (first == null || second == null) return first == null && second == null;
        return first.getUniqueId().equals(second.getUniqueId());
    }

    private @Nullable LivingEntity selectActiveTarget() {
        BotTargetMode mode = botOptions.getTargetMode();
        boolean playerSelectable = mode.allowsPlayers()
                && isTargetAvailable(targetPlayer)
                && (!combat || shouldUseCombatOnCurrentTarget());
        org.bukkit.entity.Player player = playerSelectable ? targetPlayer : null;
        Mob mob = mode.allowsMobs() && combat ? targetingService.findClosestMob(bot, getMobTargetRange()) : null;

        if (player == null) {
            return mob;
        }
        if (mob == null) {
            return player;
        }
        return distanceSquaredFromBot(mob) < distanceSquaredFromBot(player) ? mob : player;
    }

    private boolean shouldFollowPlayerAnchor() {
        if (!follow || !isTargetAvailable(targetPlayer)) {
            return false;
        }
        BotType type = botOptions.getBotType();
        return type == BotType.SINGLE || type == BotType.ALLY || type == BotType.TEAM_ALLY;
    }

    private double getMobTargetRange() {
        return switch (botOptions.getBotType()) {
            case EVENT -> eventTargetRange;
            case ALLY -> allyRange;
            case TEAM_ALLY -> teamAllyRange;
            default -> botOptions.getAutoTargetRange();
        };
    }

    private double distanceSquaredFromBot(LivingEntity entity) {
        double dx = entity.getX() - bot.asBukkitPlayer().getX();
        double dy = entity.getY() - bot.asBukkitPlayer().getY();
        double dz = entity.getZ() - bot.asBukkitPlayer().getZ();
        return dx * dx + dy * dy + dz * dz;
    }

    private boolean shouldUseCombatOnCurrentTarget() {
        if (!combat) {
            return false;
        }

        if (!isWorldGuardPvpAllowedForCurrentFight()) {
            return false;
        }

        if ((botOptions.getBotType() == BotType.ALLY || botOptions.getBotType() == BotType.TEAM_ALLY)
                && targetPlayer != null
                && isProtectedOwner(targetPlayer.getUniqueId())) {
            return false;
        }

        return true;
    }

    private boolean isProtectedOwner(UUID uuid) {
        if (uuid == null) {
            return false;
        }

        if (botOptions.getBotType() == BotType.ALLY) {
            return ownerPlayer != null && uuid.equals(ownerPlayer.getUniqueId());
        }

        if (botOptions.getBotType() == BotType.TEAM_ALLY) {
            Set<UUID> owners = botOptions.getTeamOwnerUUIDs();
            return owners.contains(uuid);
        }

        return false;
    }

    private void updateTargetByType() {
        BotType botType = botOptions.getBotType();
        Set<UUID> targetFilters = botOptions.getTargetUUIDs();

        if (botType == BotType.EVENT) {
            watchOnlyMode = false;
            clearAlertState();

            targetingService.invalidateCache(bot.getUniqueId());
            setTargetIfChanged(targetingService.findClosestPlayer(bot, eventTargetRange, this::isValidPvpTarget));
            return;
        }

        if (botType == BotType.ALLY) {
            List<org.bukkit.entity.Player> owners = getOwnersForAlly();
            handleOwnerGroupTargeting(
                    owners,
                    allyRange,
                    allyPreRange,
                    allyReturnTeleportDistance,
                    allyReturnTeleportCooldownMs,
                    allyPreRangeAlertMessage,
                    allyRangeAlertMessage,
                    targetFilters);
            return;
        }

        if (botType == BotType.TEAM_ALLY) {
            List<org.bukkit.entity.Player> owners = getOwnersForTeamAlly();
            handleOwnerGroupTargeting(
                    owners,
                    teamAllyRange,
                    teamAllyPreRange,
                    teamAllyReturnTeleportDistance,
                    teamAllyReturnTeleportCooldownMs,
                    teamAllyPreRangeAlertMessage,
                    teamAllyRangeAlertMessage,
                    targetFilters);
            return;
        }

        if (botType == BotType.SINGLE && botOptions.isAutoTarget()) {
            watchOnlyMode = false;
            clearAlertState();
            setTargetIfChanged(
                    targetingService.findClosestPlayer(bot, botOptions.getAutoTargetRange(), this::isValidPvpTarget));
            return;
        }

        watchOnlyMode = false;
        clearAlertState();
    }

    private List<org.bukkit.entity.Player> getOwnersForAlly() {
        List<org.bukkit.entity.Player> owners = new ArrayList<>();
        if (ownerPlayer != null && ownerPlayer.isOnline() && !ownerPlayer.isDead()) {
            owners.add(ownerPlayer);
        }
        return owners;
    }

    private List<org.bukkit.entity.Player> getOwnersForTeamAlly() {
        List<org.bukkit.entity.Player> owners = new ArrayList<>();

        for (UUID ownerUUID : botOptions.getTeamOwnerUUIDs()) {
            org.bukkit.entity.Player owner = Bukkit.getPlayer(ownerUUID);
            if (owner != null && owner.isOnline() && !owner.isDead()) {
                owners.add(owner);
            }
        }

        if (owners.isEmpty() && ownerPlayer != null && ownerPlayer.isOnline() && !ownerPlayer.isDead()) {
            owners.add(ownerPlayer);
        }

        return owners;
    }

    private void handleOwnerGroupTargeting(
            List<org.bukkit.entity.Player> owners,
            double range,
            double preRange,
            double returnTeleportDistance,
            long returnTeleportCooldownMs,
            String preRangeMessage,
            String rangeMessage,
            Set<UUID> targetFilters) {
        if (owners.isEmpty()) {
            watchOnlyMode = false;
            clearAlertState();
            setTargetIfChanged(null);
            return;
        }

        org.bukkit.entity.Player closestOwnerToBot = getClosestOwnerToBot(owners);

        if (follow && combat) {
            ThreatSelection closestInRange = findClosestThreatNearOwners(owners, range, targetFilters);
            if (closestInRange != null) {
                watchOnlyMode = false;
                setTargetIfChanged(closestInRange.threat);
                notifyThreatIfChanged(
                        AllyAlertState.RANGE, closestInRange.threat, owners, preRangeMessage, rangeMessage);
                return;
            }

            double effectivePreRange = Math.max(preRange, range);
            ThreatSelection closestInPreRange = findClosestThreatNearOwners(owners, effectivePreRange, targetFilters);
            if (closestInPreRange != null) {
                watchOnlyMode = true;
                setTargetIfChanged(closestInPreRange.threat);
                notifyThreatIfChanged(
                        AllyAlertState.PRE_RANGE, closestInPreRange.threat, owners, preRangeMessage, rangeMessage);
                tryTeleportBackToOwnerIfFar(closestOwnerToBot, returnTeleportDistance, returnTeleportCooldownMs);
                return;
            }
        }

        watchOnlyMode = false;
        clearAlertState();

        if (closestOwnerToBot != null) {
            setTargetIfChanged(closestOwnerToBot);
            tryTeleportBackToOwnerIfFar(closestOwnerToBot, returnTeleportDistance, returnTeleportCooldownMs);
            return;
        }

        setTargetIfChanged(null);
    }

    private @Nullable ThreatSelection findClosestThreatNearOwners(
            List<org.bukkit.entity.Player> owners, double range, Set<UUID> targetFilters) {
        if (owners.isEmpty()) {
            return null;
        }

        Set<UUID> ownerUUIDs;
        if (botOptions.getBotType() == BotType.TEAM_ALLY) {
            ownerUUIDs = botOptions.getTeamOwnerUUIDs();
        } else if (ownerPlayer != null) {
            ownerUUIDs = Set.of(ownerPlayer.getUniqueId());
        } else {
            ownerUUIDs = Set.of();
        }

        double rangeSq = range * range;
        ThreatSelection best = null;
        UUID botUUID = bot.getUniqueId();

        for (org.bukkit.entity.Player candidate : collectThreatCandidates()) {
            if (candidate == null || candidate.isDead()) {
                continue;
            }

            GameMode gameMode = candidate.getGameMode();
            if (gameMode != null && gameMode.isInvulnerable()) {
                continue;
            }

            if (!targetFilters.isEmpty() && !targetFilters.contains(candidate.getUniqueId())) {
                continue;
            }

            if (botUUID.equals(candidate.getUniqueId())) {
                continue;
            }

            if (!botOptions.isAttackBots()
                    && plugin.getBotRegistry().getOwnerUUIDByBotUUID(candidate.getUniqueId()) != null) {
                continue;
            }

            if (ownerUUIDs.contains(candidate.getUniqueId())) {
                continue;
            }

            if (!isValidPvpTarget(candidate)) {
                continue;
            }

            double bestOwnerDistanceSq = Double.MAX_VALUE;
            for (org.bukkit.entity.Player owner : owners) {
                if (!candidate.getWorld().getUID().equals(owner.getWorld().getUID())) {
                    continue;
                }

                double dx = candidate.getX() - owner.getX();
                double dy = candidate.getY() - owner.getY();
                double dz = candidate.getZ() - owner.getZ();
                double distanceSq = dx * dx + dy * dy + dz * dz;

                if (distanceSq <= rangeSq && distanceSq < bestOwnerDistanceSq) {
                    bestOwnerDistanceSq = distanceSq;
                }
            }

            if (bestOwnerDistanceSq == Double.MAX_VALUE) {
                continue;
            }

            if (best == null || bestOwnerDistanceSq < best.distanceSq) {
                best = new ThreatSelection(candidate, bestOwnerDistanceSq);
            }
        }

        return best;
    }

    private List<org.bukkit.entity.Player> collectThreatCandidates() {
        Map<UUID, org.bukkit.entity.Player> candidates = new LinkedHashMap<>();
        for (org.bukkit.entity.Player online : Bukkit.getOnlinePlayers()) {
            if (online != null) {
                candidates.put(online.getUniqueId(), online);
            }
        }

        if (!botOptions.isAttackBots()) {
            return new ArrayList<>(candidates.values());
        }

        for (ITrainingBot managedBot : plugin.getBotRegistry().getAllBots().values()) {
            if (managedBot == null || managedBot.asBukkitPlayer() == null) {
                continue;
            }
            org.bukkit.entity.Player managedPlayer = managedBot.asBukkitPlayer();
            candidates.put(managedPlayer.getUniqueId(), managedPlayer);
        }

        return new ArrayList<>(candidates.values());
    }

    private boolean isValidPvpTarget(org.bukkit.entity.@Nullable Player candidate) {
        if (candidate == null) {
            return false;
        }
        if (!botOptions.isRespectWorldGuardPvp()) {
            return true;
        }
        return plugin.getWorldGuardPvpService()
                .isPvpAllowed(java.util.Objects.requireNonNull(candidate.getLocation(), "candidate location"));
    }

    private boolean isTargetAvailable(org.bukkit.entity.@Nullable Player candidate) {
        if (candidate == null || candidate.isDead()) {
            return false;
        }
        return candidate.isOnline() || plugin.getBotRegistry().getOwnerUUIDByBotUUID(candidate.getUniqueId()) != null;
    }

    private boolean isWorldGuardPvpAllowedForCurrentFight() {
        if (!botOptions.isRespectWorldGuardPvp()) {
            return true;
        }
        if (targetPlayer != null
                && !plugin.getWorldGuardPvpService()
                        .isPvpAllowed(
                                java.util.Objects.requireNonNull(targetPlayer.getLocation(), "target location"))) {
            return false;
        }
        org.bukkit.entity.Entity bukkitBot = bot.asBukkitPlayer();
        return bukkitBot == null || plugin.getWorldGuardPvpService().isPvpAllowed(bukkitBot.getLocation());
    }

    private org.bukkit.entity.@Nullable Player getClosestOwnerToBot(List<org.bukkit.entity.Player> owners) {
        org.bukkit.entity.Player bestOwner = null;
        double bestDistanceSq = Double.MAX_VALUE;

        for (org.bukkit.entity.Player owner : owners) {
            if (owner == null || !owner.isOnline() || owner.isDead()) {
                continue;
            }

            if (!owner.getWorld()
                    .getUID()
                    .equals(bot.asBukkitPlayer().getWorld().getUID())) {
                continue;
            }

            double distanceSq = java.util.Objects.requireNonNull(bot.asBukkitPlayer().getLocation(), "bot location")
                    .distanceSquared(java.util.Objects.requireNonNull(owner.getLocation(), "owner location"));
            if (distanceSq < bestDistanceSq) {
                bestDistanceSq = distanceSq;
                bestOwner = owner;
            }
        }

        return bestOwner;
    }

    private void notifyThreatIfChanged(
            AllyAlertState state,
            org.bukkit.entity.@Nullable Player threat,
            List<org.bukkit.entity.Player> owners,
            String preRangeMessage,
            String rangeMessage) {
        if (threat == null) {
            return;
        }

        UUID threatUUID = threat.getUniqueId();
        if (lastAlertState == state && threatUUID.equals(lastAlertTarget)) {
            return;
        }

        String template = state == AllyAlertState.RANGE ? rangeMessage : preRangeMessage;
        if (template != null && !template.isBlank()) {
            String finalMessage = ChatColorUtils.translate(template.replace("%player%", threat.getName()));
            for (org.bukkit.entity.Player owner : owners) {
                if (owner != null && owner.isOnline()) {
                    owner.sendMessage(finalMessage);
                }
            }
        }

        lastAlertState = state;
        lastAlertTarget = threatUUID;
    }

    private void clearAlertState() {
        lastAlertState = AllyAlertState.NONE;
        lastAlertTarget = null;
    }

    private void tryTeleportBackToOwnerIfFar(
            org.bukkit.entity.@Nullable Player owner, double returnTeleportDistance, long returnTeleportCooldownMs) {
        if (owner == null) {
            return;
        }

        double distance = java.util.Objects.requireNonNull(bot.asBukkitPlayer().getLocation(), "bot location")
                .distance(java.util.Objects.requireNonNull(owner.getLocation(), "owner location"));
        if (distance <= returnTeleportDistance) {
            return;
        }

        long now = System.currentTimeMillis();
        if (now - lastReturnTeleport < returnTeleportCooldownMs) {
            return;
        }

        if (botAI.getTeleportController().teleportSafeNear(owner)) {
            lastReturnTeleport = now;
        }
    }

    private void setTargetIfChanged(org.bukkit.entity.@Nullable Player newTarget) {
        if (sameTarget(newTarget, this.targetPlayer)) {
            return;
        }

        this.targetPlayer = newTarget;
        bot.getBotAI().getTeleportController().setTarget(newTarget);
    }

    public BotAI getBotAI() {
        return botAI;
    }

    public org.bukkit.entity.@Nullable Player getTargetPlayer() {
        return targetPlayer;
    }

    public @Nullable LivingEntity getActiveTarget() {
        return activeTarget;
    }

    public void setFollow(boolean follow) {
        this.follow = follow;
    }

    public boolean isFollow() {
        return follow;
    }

    public void setCombat(boolean combat) {
        this.combat = combat;
    }

    public boolean isCombat() {
        return combat;
    }

    public BotOptions getBotOptions() {
        return botOptions;
    }
}
