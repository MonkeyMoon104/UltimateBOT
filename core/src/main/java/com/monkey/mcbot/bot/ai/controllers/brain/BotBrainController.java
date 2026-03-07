package com.monkey.mcbot.bot.ai.controllers.brain;

import com.monkey.mcbot.MinecraftBot;
import com.monkey.mcbot.bot.BotOptions;
import com.monkey.mcbot.bot.BotType;
import com.monkey.mcbot.bot.ai.BotAI;
import com.monkey.mcbot.bot.ai.ITrainingBot;
import com.monkey.mcbot.bot.ai.services.TargetingService;
import com.monkey.mcbot.utils.ChatColorUtils;
import net.minecraft.world.entity.player.Player;
import org.bukkit.craftbukkit.entity.CraftPlayer;

import java.util.UUID;

public class BotBrainController {

    private enum AllyAlertState {
        NONE,
        PRE_RANGE,
        RANGE
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

    private Player cachedNmsTarget = null;
    private long lastNmsTargetUpdate = 0;
    private static final long NMS_CACHE_TIME = 100;
    private long lastAllyReturnTeleport = 0;

    private boolean allyWatchOnlyMode = false;
    private AllyAlertState lastAllyAlertState = AllyAlertState.NONE;
    private UUID lastAllyAlertTarget = null;

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
        this.allyPreRangeAlertMessage = plugin.getConfig().getString("messages.ally.pre-range-alert");
        this.allyRangeAlertMessage = plugin.getConfig().getString("messages.ally.range-alert");

        configureBotAI();
    }

    private void configureBotAI() {
        if (targetPlayer != null && follow) {
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

        if (allyWatchOnlyMode) {
            botAI.getMovementController().clearPath();
            return;
        }

        botAI.tick(targetPlayer, allowCombat);
    }

    private boolean shouldUseCombatOnCurrentTarget() {
        if (!combat) {
            return false;
        }

        if (botOptions.getBotType() == BotType.ALLY
                && ownerPlayer != null
                && targetPlayer != null
                && ownerPlayer.getUniqueId().equals(targetPlayer.getUniqueId())) {
            return false;
        }

        return true;
    }

    private Player getNMSTarget() {
        long currentTime = System.currentTimeMillis();

        if (cachedNmsTarget != null && (currentTime - lastNmsTargetUpdate) < NMS_CACHE_TIME) {
            return cachedNmsTarget;
        }

        lastNmsTargetUpdate = currentTime;
        if (targetPlayer != null) {
            cachedNmsTarget = ((CraftPlayer) targetPlayer).getHandle();
        } else {
            cachedNmsTarget = null;
        }

        return cachedNmsTarget;
    }

    private void updateTargetByType() {
        BotType botType = botOptions.getBotType();

        if (botType == BotType.EVENT) {
            allyWatchOnlyMode = false;
            setTargetIfChanged(targetingService.findClosestPlayer(bot, eventTargetRange));
            return;
        }

        if (botType == BotType.ALLY) {
            handleAllyTargeting();
            return;
        }

        allyWatchOnlyMode = false;
        clearAllyAlertState();
    }

    private void handleAllyTargeting() {
        if (ownerPlayer == null || !ownerPlayer.isOnline() || ownerPlayer.isDead()) {
            allyWatchOnlyMode = false;
            clearAllyAlertState();
            return;
        }

        if (follow && combat) {
            org.bukkit.entity.Player closestInRange = targetingService.findClosestPlayerNearPlayer(
                    bot,
                    ownerPlayer,
                    allyRange,
                    ownerPlayer.getUniqueId()
            );

            if (closestInRange != null) {
                allyWatchOnlyMode = false;
                setTargetIfChanged(closestInRange);
                notifyAllyThreatIfChanged(AllyAlertState.RANGE, closestInRange);
                return;
            }

            org.bukkit.entity.Player closestInPreRange = targetingService.findClosestPlayerNearPlayer(
                    bot,
                    ownerPlayer,
                    allyPreRange,
                    ownerPlayer.getUniqueId()
            );

            if (closestInPreRange != null) {
                allyWatchOnlyMode = true;
                setTargetIfChanged(closestInPreRange);
                notifyAllyThreatIfChanged(AllyAlertState.PRE_RANGE, closestInPreRange);
                tryTeleportBackToOwnerIfFar();
                return;
            }
        }

        allyWatchOnlyMode = false;
        setTargetIfChanged(ownerPlayer);
        clearAllyAlertState();
        tryTeleportBackToOwnerIfFar();
    }

    private void notifyAllyThreatIfChanged(AllyAlertState state, org.bukkit.entity.Player threat) {
        if (threat == null || ownerPlayer == null || !ownerPlayer.isOnline()) {
            return;
        }

        UUID threatUUID = threat.getUniqueId();
        if (lastAllyAlertState == state && threatUUID.equals(lastAllyAlertTarget)) {
            return;
        }

        String template = state == AllyAlertState.RANGE ? allyRangeAlertMessage : allyPreRangeAlertMessage;
        if (template != null && !template.isBlank()) {
            ownerPlayer.sendMessage(ChatColorUtils.translate(template.replace("%player%", threat.getName())));
        }

        lastAllyAlertState = state;
        lastAllyAlertTarget = threatUUID;
    }

    private void clearAllyAlertState() {
        lastAllyAlertState = AllyAlertState.NONE;
        lastAllyAlertTarget = null;
    }

    private void tryTeleportBackToOwnerIfFar() {
        if (ownerPlayer == null) {
            return;
        }

        double distance = bot.asPlayer().distanceTo(((CraftPlayer) ownerPlayer).getHandle());
        if (distance <= allyReturnTeleportDistance) {
            return;
        }

        long now = System.currentTimeMillis();
        if (now - lastAllyReturnTeleport < allyReturnTeleportCooldownMs) {
            return;
        }

        if (botAI.getTeleportController().teleportSafeNear(ownerPlayer)) {
            lastAllyReturnTeleport = now;
        }
    }

    private void setTargetIfChanged(org.bukkit.entity.Player newTarget) {
        if (newTarget != null && newTarget != this.targetPlayer) {
            this.targetPlayer = newTarget;

            cachedNmsTarget = null;
            lastNmsTargetUpdate = 0;

            bot.getBotAI().getTeleportController().setTarget(newTarget);
        }
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
