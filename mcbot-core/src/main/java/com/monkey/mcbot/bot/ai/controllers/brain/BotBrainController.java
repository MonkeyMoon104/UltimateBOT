package com.monkey.mcbot.bot.ai.controllers.brain;

import com.monkey.mcbot.MinecraftBot;
import com.monkey.mcbot.bot.BotOptions;
import com.monkey.mcbot.bot.BotType;
import com.monkey.mcbot.bot.ai.BotAI;
import com.monkey.mcbot.bot.ai.ITrainingBot;
import com.monkey.mcbot.bot.ai.services.TargetingService;
import com.monkey.mcbot.utils.ChatColorUtils;
import net.minecraft.world.entity.player.Player;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.craftbukkit.entity.CraftPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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
    private final MinecraftBot plugin;
    private org.bukkit.entity.Player targetPlayer;
    private final org.bukkit.entity.Player ownerPlayer;
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

    private Player cachedNmsTarget = null;
    private long lastNmsTargetUpdate = 0;
    private static final long NMS_CACHE_TIME = 100;
    private long lastReturnTeleport = 0;

    private boolean watchOnlyMode = false;
    private AllyAlertState lastAlertState = AllyAlertState.NONE;
    private UUID lastAlertTarget = null;

    public BotBrainController(ITrainingBot bot, MinecraftBot plugin,
                              org.bukkit.entity.Player targetPlayer, boolean follow, BotOptions botOptions) {
        this.targetingService = plugin.getTargetingService();
        this.bot = bot;
        this.plugin = plugin;
        this.targetPlayer = targetPlayer;
        this.ownerPlayer = targetPlayer;
        this.follow = follow;
        this.botAI = new BotAI(bot.asPlayer(), plugin, botOptions);
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
        if (targetPlayer != null && follow && targetPlayer instanceof CraftPlayer) {
            Player target = ((CraftPlayer) targetPlayer).getHandle();
            botAI.getRotationController().setInstantRotation(target);
        }
    }

    public void onTick() {
        if (!follow) {
            return;
        }

        updateTargetByType();

        if (targetPlayer == null || targetPlayer.isDead() || !targetPlayer.isOnline()) {
            return;
        }

        Player target = getNMSTarget();
        if (target == null) {
            return;
        }

        boolean allowCombat = shouldUseCombatOnCurrentTarget();

        botAI.getRotationController().updateRotation(target);

        if (watchOnlyMode) {
            botAI.getMovementController().clearPath();
            return;
        }

        botAI.tick(targetPlayer, allowCombat);
    }

    private boolean shouldUseCombatOnCurrentTarget() {
        if (!combat) {
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

    private Player getNMSTarget() {
        long currentTime = System.currentTimeMillis();

        if (targetPlayer == null || !targetPlayer.isOnline() || targetPlayer.isDead() || !(targetPlayer instanceof CraftPlayer)) {
            cachedNmsTarget = null;
            return null;
        }

        if (cachedNmsTarget != null && (currentTime - lastNmsTargetUpdate) < NMS_CACHE_TIME) {
            return cachedNmsTarget;
        }

        lastNmsTargetUpdate = currentTime;
        cachedNmsTarget = ((CraftPlayer) targetPlayer).getHandle();

        return cachedNmsTarget;
    }

    private void updateTargetByType() {
        BotType botType = botOptions.getBotType();
        Set<UUID> targetFilters = botOptions.getTargetUUIDs();

        if (botType == BotType.EVENT) {
            watchOnlyMode = false;
            clearAlertState();

            targetingService.invalidateCache(bot.asPlayer().getUUID());
            setTargetIfChanged(targetingService.findClosestPlayer(bot, eventTargetRange));
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
                    targetFilters
            );
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
                    targetFilters
            );
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

    private void handleOwnerGroupTargeting(List<org.bukkit.entity.Player> owners,
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
                notifyThreatIfChanged(AllyAlertState.RANGE, closestInRange.threat, owners, preRangeMessage, rangeMessage);
                return;
            }

            double effectivePreRange = Math.max(preRange, range);
            ThreatSelection closestInPreRange = findClosestThreatNearOwners(owners, effectivePreRange, targetFilters);
            if (closestInPreRange != null) {
                watchOnlyMode = true;
                setTargetIfChanged(closestInPreRange.threat);
                notifyThreatIfChanged(AllyAlertState.PRE_RANGE, closestInPreRange.threat, owners, preRangeMessage, rangeMessage);
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

    private ThreatSelection findClosestThreatNearOwners(List<org.bukkit.entity.Player> owners,
                                                        double range,
                                                        Set<UUID> targetFilters) {
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
        UUID botUUID = bot.asPlayer().getUUID();

        for (org.bukkit.entity.Player candidate : Bukkit.getOnlinePlayers()) {
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

            if (ownerUUIDs.contains(candidate.getUniqueId())) {
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

    private org.bukkit.entity.Player getClosestOwnerToBot(List<org.bukkit.entity.Player> owners) {
        org.bukkit.entity.Player bestOwner = null;
        double bestDistanceSq = Double.MAX_VALUE;

        for (org.bukkit.entity.Player owner : owners) {
            if (owner == null || !owner.isOnline() || owner.isDead()) {
                continue;
            }

            if (!owner.getWorld().getUID().equals(bot.asPlayer().level().getWorld().getUID())) {
                continue;
            }

            double distanceSq = bot.asPlayer().distanceToSqr(((CraftPlayer) owner).getHandle());
            if (distanceSq < bestDistanceSq) {
                bestDistanceSq = distanceSq;
                bestOwner = owner;
            }
        }

        return bestOwner;
    }

    private void notifyThreatIfChanged(AllyAlertState state,
                                       org.bukkit.entity.Player threat,
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

    private void tryTeleportBackToOwnerIfFar(org.bukkit.entity.Player owner,
                                             double returnTeleportDistance,
                                             long returnTeleportCooldownMs) {
        if (owner == null) {
            return;
        }

        double distance = bot.asPlayer().distanceTo(((CraftPlayer) owner).getHandle());
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

    private void setTargetIfChanged(org.bukkit.entity.Player newTarget) {
        if (newTarget == this.targetPlayer) {
            return;
        }

        this.targetPlayer = newTarget;
        cachedNmsTarget = null;
        lastNmsTargetUpdate = 0;
        bot.getBotAI().getTeleportController().setTarget(newTarget);
    }

    public BotAI getBotAI() {
        return botAI;
    }

    public org.bukkit.entity.Player getTargetPlayer() {
        return targetPlayer;
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
