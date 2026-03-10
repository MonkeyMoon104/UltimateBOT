package com.monkey.mcbot.bot.ai.controllers.enderpearl;

import com.monkey.mcbot.bot.ai.controllers.enderpearl.helper.*;
import com.monkey.mcbot.bot.ai.controllers.enderpearl.helper.inter.*;
import com.monkey.mcbot.bot.ai.controllers.inventory.BotInventoryController;
import com.monkey.mcbot.bot.ai.controllers.rotation.BotRotationController;
import com.monkey.mcbot.bot.ai.controllers.teleport.BotTeleportController;
import com.monkey.mcbot.bot.ai.controllers.enderpearl.helper.*;
import com.monkey.mcbot.bot.ai.controllers.enderpearl.helper.inter.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.bukkit.craftbukkit.entity.CraftPlayer;

public class BotEnderpearlController {

    private final Player bot;
    private final Level level;
    private final BotInventoryController inventoryController;
    private final BotRotationController rotationController;
    private final BotTeleportController teleportController;

    private final IPearlStrategyCalculator strategyCalculator;
    private final IPearlThrower pearlThrower;
    private final IDamageTracker damageTracker;
    private final ITargetTracker targetTracker;
    private final ISafetyValidator safetyValidator;

    private int enderpearlCooldown = 0;
    private static final int ENDERPEARL_COOLDOWN_TICKS = 30;
    private static final double MIN_ENDERPEARL_DISTANCE = 4.0;
    private static final double MAX_ENDERPEARL_DISTANCE = 10.0;

    private Player currentTarget;

    private boolean isPreparingPearl = false;
    private Vec3 pendingThrowTarget = null;
    private int preparationTicks = 0;
    private static final int PREPARATION_TIME = 3;
    private IPearlStrategyCalculator.PearlStrategy currentStrategy = IPearlStrategyCalculator.PearlStrategy.ESCAPE;

    private int aggressivePearlCooldown = 0;
    private int repositionPearlCooldown = 0;
    private long lastEmergencyPearl = 0;

    private long lastPearlUseTime = 0;
    private static final long MIN_TIME_BETWEEN_PEARLS_MS = 700;
    private static final double MIN_USE_DISTANCE = 3.5;
    private static final double MAX_USE_DISTANCE = 16.0;

    private long lastAutoTeleportTime = 0;
    private static final long AUTO_TELEPORT_COOLDOWN_MS = 10000;
    private static final double AUTO_TELEPORT_HORIZONTAL_DISTANCE = 25.0;
    private static final double AUTO_TELEPORT_VERTICAL_DISTANCE = 15.0;
    private static final double EMERGENCY_TELEPORT_DISTANCE = 25.0;

    public BotEnderpearlController(Player bot, BotInventoryController inventoryController, BotRotationController rotationController) {
        this.bot = bot;
        this.level = bot.level();
        this.inventoryController = inventoryController;
        this.rotationController = rotationController;
        this.teleportController = new BotTeleportController(bot);

        this.safetyValidator = new SafetyValidator(level);
        this.pearlThrower = new PearlThrower(level);
        this.damageTracker = new DamageTracker(bot);
        this.targetTracker = new TargetTracker();
        this.strategyCalculator = new PearlStrategyCalculator(new PositionCalculator(safetyValidator));
    }

    public boolean tryUseEnderpearl(Player target) {
        return tryUseEnderpearl(target, null);
    }

    public boolean tryUseEnderpearl(Player target, IPearlStrategyCalculator.PearlStrategy forcedStrategy) {
        if (isPreparingPearl) return false;
        if (!inventoryController.hasEnderpearls()) return false;

        this.currentTarget = target;
        targetTracker.updateTargetTracking(target);

        double dist = bot.distanceTo(target);
        if (dist < MIN_USE_DISTANCE && forcedStrategy != IPearlStrategyCalculator.PearlStrategy.COMBO_ESCAPE) return false;
        if (dist > MAX_USE_DISTANCE && forcedStrategy != IPearlStrategyCalculator.PearlStrategy.AGGRESSIVE_CLOSE) return false;

        try {
            if (!bot.onGround() && forcedStrategy == null) return false;
        } catch (Exception ignored) {}

        if (!canUseEnderpearl()) return false;

        IPearlStrategyCalculator.PearlStrategy strategy = forcedStrategy != null ? forcedStrategy :
                strategyCalculator.determineOptimalStrategy(bot, target, damageTracker.wasRecentlyDamaged(),
                        damageTracker.getDamageComboCount(), damageTracker.getComboStartTime(),
                        repositionPearlCooldown, aggressivePearlCooldown);

        if (!strategyCalculator.shouldUsePearlForStrategy(strategy, bot, target, damageTracker.wasRecentlyDamaged(),
                lastEmergencyPearl, repositionPearlCooldown, aggressivePearlCooldown)) return false;

        if (!inventoryController.isHoldingEnderpearl()) {
            inventoryController.switchToEnderpearl();
        }

        Vec3 targetPos = strategyCalculator.calculateTargetForStrategy(strategy, bot, target,
                targetTracker.getPredictedTargetMovement());
        if (targetPos == null) return false;

        startPearlPreparation(targetPos, strategy);
        return true;
    }

    public boolean tryUseEnderpearlToPosition(Vec3 targetPos) {
        if (!canUseEnderpearl() || isPreparingPearl) return false;
        if (!inventoryController.hasEnderpearls()) return false;

        if (!inventoryController.isHoldingEnderpearl()) {
            inventoryController.switchToEnderpearl();
        }

        startPearlPreparation(targetPos, IPearlStrategyCalculator.PearlStrategy.ESCAPE);
        return true;
    }

    private void startPearlPreparation(Vec3 targetPos, IPearlStrategyCalculator.PearlStrategy strategy) {
        isPreparingPearl = true;
        pendingThrowTarget = targetPos;
        preparationTicks = 0;
        currentStrategy = strategy;

        rotationController.lookAt(targetPos.x, targetPos.y, targetPos.z);

        lastPearlUseTime = System.currentTimeMillis();

        switch (strategy) {
            case COMBO_ESCAPE -> lastEmergencyPearl = System.currentTimeMillis();
            case REPOSITION_LOW -> repositionPearlCooldown = 100;
            case AGGRESSIVE_CLOSE -> aggressivePearlCooldown = 80;
        }
    }

    public boolean tryPearlToObsidianSide(BlockPos obsidianPos, Player target) {
        if (!canUseEnderpearl() || isPreparingPearl) return false;
        if (!inventoryController.hasEnderpearls()) return false;

        Vec3 targetPos = target.position();
        Vec3 velocity = target.getDeltaMovement();
        Vec3 predictedTargetPos = targetPos.add(velocity.scale(8 / 20.0));

        BlockPos bestSide = null;
        double bestScore = Double.NEGATIVE_INFINITY;

        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos sidePos = obsidianPos.relative(dir);

            if (!safetyValidator.isSafeLandingSpot(sidePos)) continue;

            Vec3 sideCenter = Vec3.atCenterOf(sidePos);
            double distToTarget = sideCenter.distanceTo(predictedTargetPos);
            double distToBot = sideCenter.distanceTo(bot.position());

            double score = 0;

            if (distToTarget < 8.0) {
                score += (8.0 - distToTarget) * 10;
            }

            if (distToBot > 6.0) {
                score += 20;
            }

            if (sidePos.getY() <= target.blockPosition().getY()) {
                score += 15;
            }

            if (score > bestScore) {
                bestScore = score;
                bestSide = sidePos;
            }
        }

        if (bestSide == null) return false;

        Vec3 pearlTarget = Vec3.atCenterOf(bestSide);

        if (!inventoryController.isHoldingEnderpearl()) {
            inventoryController.switchToEnderpearl();
        }

        this.currentTarget = target;
        startPearlPreparation(pearlTarget, IPearlStrategyCalculator.PearlStrategy.ANCHOR_POSITION);
        return true;
    }

    public boolean checkAndPerformAutoTeleport(org.bukkit.entity.Player target) {
        if (target == null || !target.isOnline()) return false;
        if (!canAutoTeleport()) return false;

        Vec3 botPos = bot.position();
        org.bukkit.Location targetLoc = target.getLocation();
        Vec3 targetPos = new Vec3(targetLoc.getX(), targetLoc.getY(), targetLoc.getZ());

        double horizontalDistance = Math.sqrt(
                Math.pow(botPos.x - targetPos.x, 2) +
                        Math.pow(botPos.z - targetPos.z, 2)
        );
        double verticalDistance = Math.abs(botPos.y - targetPos.y);
        double totalDistance = botPos.distanceTo(targetPos);

        boolean needsTeleport = false;
        String reason = "";

        if (totalDistance > EMERGENCY_TELEPORT_DISTANCE) {
            needsTeleport = true;
            reason = "emergency distance";
        } else if (horizontalDistance > AUTO_TELEPORT_HORIZONTAL_DISTANCE) {
            needsTeleport = true;
            reason = "horizontal distance";
        } else if (verticalDistance > AUTO_TELEPORT_VERTICAL_DISTANCE) {
            needsTeleport = true;
            reason = "vertical distance";
        }

        if (needsTeleport) {
            return performAutoTeleport(target, reason);
        }

        return false;
    }

    private boolean performAutoTeleport(org.bukkit.entity.Player target, String reason) {
        try {
            teleportController.setTarget(target);

            boolean success = teleportController.teleportSafeNear(target);
            if (!success) {
                success = teleportController.teleportBeside(target);
            }

            if (success) {
                lastAutoTeleportTime = System.currentTimeMillis();

                if (isPreparingPearl) {
                    isPreparingPearl = false;
                    pendingThrowTarget = null;
                    preparationTicks = 0;
                }

                if (target.isOnline()) {
                    net.minecraft.world.entity.player.Player nmsTarget = ((CraftPlayer) target).getHandle();
                    rotationController.updateRotation(nmsTarget);
                }

                return true;
            }
        } catch (Exception e) {
            return false;
        }

        return false;
    }

    private boolean canAutoTeleport() {
        long currentTime = System.currentTimeMillis();
        return currentTime - lastAutoTeleportTime >= AUTO_TELEPORT_COOLDOWN_MS &&
                !teleportController.isTeleporting();
    }

    public long getAutoTeleportCooldownRemaining() {
        long currentTime = System.currentTimeMillis();
        long timeSinceLastTeleport = currentTime - lastAutoTeleportTime;
        return Math.max(0, AUTO_TELEPORT_COOLDOWN_MS - timeSinceLastTeleport);
    }

    public boolean isAutoTeleportReady() {
        return canAutoTeleport();
    }

    public void onDamageReceived() {
        damageTracker.onDamageReceived(bot);
    }

    private void throwEnderpearl(Vec3 targetPos) {
        pearlThrower.throwEnderpearl(bot, targetPos);

        enderpearlCooldown = ENDERPEARL_COOLDOWN_TICKS * 2;
        lastPearlUseTime = System.currentTimeMillis();

        handlePostPearlStrategy();

        if (currentTarget != null && currentTarget.isAlive()) {
            rotationController.updateRotation(currentTarget);
        }

        inventoryController.switchToSword();
    }

    private void handlePostPearlStrategy() {
        switch (currentStrategy) {
            case COMBO_ESCAPE, ESCAPE -> {
                damageTracker.resetDamageState();
            }
            case AGGRESSIVE_CLOSE -> {
            }
            case REPOSITION_LOW -> {
                repositionPearlCooldown = 100;
            }
        }
    }

    public void tick() {
        if (enderpearlCooldown > 0) enderpearlCooldown--;
        if (aggressivePearlCooldown > 0) aggressivePearlCooldown--;
        if (repositionPearlCooldown > 0) repositionPearlCooldown--;

        damageTracker.tick();

        if (isPreparingPearl && pendingThrowTarget != null) {
            preparationTicks++;

            rotationController.lookAt(pendingThrowTarget.x, pendingThrowTarget.y, pendingThrowTarget.z);

            if (preparationTicks >= PREPARATION_TIME) {
                throwEnderpearl(pendingThrowTarget);

                isPreparingPearl = false;
                pendingThrowTarget = null;
                preparationTicks = 0;
            }
        }
    }

    public boolean canUseEnderpearl() {
        if (isPreparingPearl) return false;
        if (enderpearlCooldown > 0) return false;
        if (!inventoryController.hasEnderpearls()) return false;
        if (!bot.isAlive()) return false;

        if (System.currentTimeMillis() - lastPearlUseTime < MIN_TIME_BETWEEN_PEARLS_MS) return false;

        Vec3 vel = bot.getDeltaMovement();
        if (vel.lengthSqr() > 1.2 * 1.2) return false;

        return true;
    }

    public int getCooldown() {
        return enderpearlCooldown;
    }

    public void setCooldown(int ticks) {
        this.enderpearlCooldown = ticks;
    }

    public boolean shouldUseEnderpearl(Player target) {
        IPearlStrategyCalculator.PearlStrategy strategy = strategyCalculator.determineOptimalStrategy(bot, target,
                damageTracker.wasRecentlyDamaged(), damageTracker.getDamageComboCount(),
                damageTracker.getComboStartTime(), repositionPearlCooldown, aggressivePearlCooldown);
        return strategyCalculator.shouldUsePearlForStrategy(strategy, bot, target, damageTracker.wasRecentlyDamaged(),
                lastEmergencyPearl, repositionPearlCooldown, aggressivePearlCooldown);
    }

    public boolean wasRecentlyDamaged() {
        return damageTracker.wasRecentlyDamaged();
    }

    public boolean isThrowingPearl() {
        return isPreparingPearl;
    }

    public int getDamageComboCount() {
        return damageTracker.getDamageComboCount();
    }

    public IPearlStrategyCalculator.PearlStrategy getCurrentStrategy() {
        return currentStrategy;
    }
}