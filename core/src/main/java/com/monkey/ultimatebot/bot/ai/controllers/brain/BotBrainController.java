package com.monkey.ultimatebot.bot.ai.controllers.brain;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.bot.BotOptions;
import com.monkey.ultimatebot.bot.BotType;
import com.monkey.ultimatebot.bot.ai.BotAI;
import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.bot.ai.services.TargetingService;
import com.monkey.ultimatebot.bot.ai.controllers.brain.steps.NoTargetTickStep;
import com.monkey.ultimatebot.bot.ai.controllers.brain.steps.TargetChangeMediationStep;
import com.monkey.ultimatebot.bot.ai.controllers.brain.steps.TargetResolutionStep;
import com.monkey.ultimatebot.bot.ai.controllers.brain.steps.TickExecutionStep;
import com.monkey.ultimatebot.bot.ai.controllers.brain.steps.WatchOnlyTickStep;
import com.monkey.ultimatebot.access.entity.EntityCoordsAccess;
import com.monkey.ultimatebot.access.player.GameModeAccess;
import com.monkey.ultimatebot.utils.ChatColorUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.LivingEntity;
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
            botAI.getRotationController()
                    .lookAt(
                            EntityCoordsAccess.getX(targetPlayer),
                            targetPlayer.getEyeLocation().getY(),
                            EntityCoordsAccess.getZ(targetPlayer));
        }
    }

    public void onTick() {
        updateTargetByType();
        LivingEntity selectedTarget = resolveTargetForTickStep();
        activeTarget = selectedTarget;
        executeTickStep(selectedTarget);
    }

    private boolean sameTarget(@Nullable LivingEntity first, @Nullable LivingEntity second) {
        if (first == null || second == null) return first == null && second == null;
        return first.getUniqueId().equals(second.getUniqueId());
    }

    private boolean shouldFollowPlayerAnchor() {
        if (!follow || !isTargetAvailable(targetPlayer)) {
            return false;
        }
        BotType type = botOptions.getBotType();
        return type == BotType.SINGLE || type == BotType.ALLY || type == BotType.TEAM_ALLY;
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
            handleEventTargetingStep();
            return;
        }

        if (botType == BotType.ALLY) {
            handleAllyTargetingStep(targetFilters);
            return;
        }

        if (botType == BotType.TEAM_ALLY) {
            handleTeamAllyTargetingStep(targetFilters);
            return;
        }

        if (botType == BotType.SINGLE && botOptions.isAutoTarget()) {
            handleSingleAutoTargetingStep();
            return;
        }

        resetWatchAndAlerts();
    }

    private @Nullable LivingEntity resolveTargetForTickStep() {
        LivingEntity selectedTarget = new TargetResolutionStep(
                        bot,
                        botOptions,
                        targetingService,
                        eventTargetRange,
                        allyRange,
                        teamAllyRange,
                        follow,
                        combat,
                        targetPlayer,
                        this::isTargetAvailable)
                .resolve();
        if (!sameTarget(activeTarget, selectedTarget)) {
            LivingEntity previousTarget = activeTarget;
            selectedTarget = new TargetChangeMediationStep(plugin, bot).mediate(previousTarget, selectedTarget);
            botAI.customBrainTargetChanged(previousTarget, selectedTarget);
        }
        return selectedTarget;
    }

    private void executeTickStep(@Nullable LivingEntity selectedTarget) {
        NoTargetTickStep noTargetTickStep = new NoTargetTickStep(botAI, targetPlayer, this::shouldFollowPlayerAnchor);
        WatchOnlyTickStep watchOnlyTickStep = new WatchOnlyTickStep(botAI);
        new TickExecutionStep(
                        botAI,
                        follow,
                        combat,
                        watchOnlyMode,
                        this::shouldUseCombatOnCurrentTarget,
                        noTargetTickStep,
                        watchOnlyTickStep)
                .execute(selectedTarget);
    }

    private void handleEventTargetingStep() {
        resetWatchAndAlerts();
        targetingService.invalidateCache(bot.getUniqueId());
        setTargetIfChanged(targetingService.findClosestPlayer(bot, eventTargetRange, this::isValidPvpTarget));
    }

    private void handleAllyTargetingStep(Set<UUID> targetFilters) {
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
    }

    private void handleTeamAllyTargetingStep(Set<UUID> targetFilters) {
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
    }

    private void handleSingleAutoTargetingStep() {
        resetWatchAndAlerts();
        setTargetIfChanged(targetingService.findClosestPlayer(bot, botOptions.getAutoTargetRange(), this::isValidPvpTarget));
    }

    private void resetWatchAndAlerts() {
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
            ownerUUIDs = Collections.singleton(ownerPlayer.getUniqueId());
        } else {
            ownerUUIDs = Collections.emptySet();
        }

        double rangeSq = range * range;
        ThreatSelection best = null;
        UUID botUUID = bot.getUniqueId();

        for (org.bukkit.entity.Player candidate : collectThreatCandidates()) {
            if (candidate == null || candidate.isDead()) {
                continue;
            }

            GameMode gameMode = candidate.getGameMode();
            if (gameMode != null && GameModeAccess.isInvulnerable(gameMode)) {
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

                double dx = EntityCoordsAccess.getX(candidate) - EntityCoordsAccess.getX(owner);
                double dy = EntityCoordsAccess.getY(candidate) - EntityCoordsAccess.getY(owner);
                double dz = EntityCoordsAccess.getZ(candidate) - EntityCoordsAccess.getZ(owner);
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

            double distanceSq = java.util.Objects.requireNonNull(
                            bot.asBukkitPlayer().getLocation(), "bot location")
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
        if (template != null && !template.trim().isEmpty()) {
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
        botOptions.setFollow(follow);
    }

    public boolean isFollow() {
        return follow;
    }

    public void setCombat(boolean combat) {
        this.combat = combat;
        botOptions.setCombat(combat);
    }

    public boolean isCombat() {
        return combat;
    }

    public BotOptions getBotOptions() {
        return botOptions;
    }
}
