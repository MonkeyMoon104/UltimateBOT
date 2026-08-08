package com.monkey.ultimatebot.bot.ai.controllers.enderpearl;

import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.bot.ai.controllers.enderpearl.helper.DamageTracker;
import com.monkey.ultimatebot.bot.ai.controllers.enderpearl.helper.PearlStrategyCalculator;
import com.monkey.ultimatebot.bot.ai.controllers.enderpearl.helper.PearlThrower;
import com.monkey.ultimatebot.bot.ai.controllers.enderpearl.helper.PositionCalculator;
import com.monkey.ultimatebot.bot.ai.controllers.enderpearl.helper.SafetyValidator;
import com.monkey.ultimatebot.bot.ai.controllers.enderpearl.helper.TargetTracker;
import com.monkey.ultimatebot.bot.ai.controllers.enderpearl.helper.inter.IDamageTracker;
import com.monkey.ultimatebot.bot.ai.controllers.enderpearl.helper.inter.IPearlStrategyCalculator;
import com.monkey.ultimatebot.bot.ai.controllers.enderpearl.helper.inter.IPearlThrower;
import com.monkey.ultimatebot.bot.ai.controllers.enderpearl.helper.inter.IPearlStrategyCalculator.PearlStrategy;
import com.monkey.ultimatebot.bot.ai.controllers.enderpearl.helper.inter.ISafetyValidator;
import com.monkey.ultimatebot.bot.ai.controllers.enderpearl.helper.inter.ITargetTracker;
import com.monkey.ultimatebot.bot.ai.controllers.inventory.BotInventoryController;
import com.monkey.ultimatebot.bot.ai.controllers.rotation.BotRotationController;
import com.monkey.ultimatebot.bot.ai.controllers.teleport.BotTeleportController;
import java.util.Objects;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockVector;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jspecify.annotations.Nullable;

@SuppressWarnings("NullAway")
public class BotEnderpearlController {

    private final ITrainingBot bot;
    private final Player bukkitBot;
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

    private @Nullable Player currentTarget;

    private boolean isPreparingPearl = false;
    private @Nullable Vector pendingThrowTarget;
    private int preparationTicks = 0;
    private static final int PREPARATION_TIME = 3;
    private PearlStrategy currentStrategy = PearlStrategy.ESCAPE;

    private int aggressivePearlCooldown = 0;
    private int repositionPearlCooldown = 0;
    private long lastEmergencyPearl = 0;

    private long lastPearlUseTime = 0;
    private static final long MIN_TIME_BETWEEN_PEARLS_MS = 700;
    private static final double MIN_USE_DISTANCE = 3.2;
    private static final double MAX_USE_DISTANCE = 14.0;

    private long lastAutoTeleportTime = 0;
    private static final long AUTO_TELEPORT_COOLDOWN_MS = 10000;
    private static final double AUTO_TELEPORT_HORIZONTAL_DISTANCE = 25.0;
    private static final double AUTO_TELEPORT_VERTICAL_DISTANCE = 15.0;
    private static final double EMERGENCY_TELEPORT_DISTANCE = 25.0;
    private static final double EXTREME_EMERGENCY_TELEPORT_DISTANCE = 34.0;
    private boolean enabled = true;

    public BotEnderpearlController(
            ITrainingBot bot, BotInventoryController inventoryController, BotRotationController rotationController) {
        this.bot = bot;
        this.bukkitBot = bot.asBukkitPlayer();
        this.inventoryController = inventoryController;
        this.rotationController = rotationController;
        this.teleportController = new BotTeleportController(bot);

        this.safetyValidator = new SafetyValidator(bot.getWorld());
        this.pearlThrower = new PearlThrower();
        this.damageTracker = new DamageTracker(bot);
        this.targetTracker = new TargetTracker();
        this.strategyCalculator = new PearlStrategyCalculator(new PositionCalculator(safetyValidator));
    }

    public boolean tryUseEnderpearl(Player target) {
        return tryUseEnderpearl(target, null);
    }

    public boolean tryUseEnderpearl(Player target, IPearlStrategyCalculator.@Nullable PearlStrategy forcedStrategy) {
        if (!enabled) return false;
        if (isPreparingPearl) return false;
        if (!inventoryController.hasEnderpearls()) return false;

        this.currentTarget = target;
        targetTracker.updateTargetTracking(target);

        double dist = bot.distanceTo(target);
        if (dist < MIN_USE_DISTANCE && forcedStrategy != PearlStrategy.COMBO_ESCAPE) return false;
        if (dist > MAX_USE_DISTANCE && forcedStrategy != PearlStrategy.AGGRESSIVE_CLOSE) return false;

        if (!isOnGround(bukkitBot) && forcedStrategy == null) return false;
        if (!canUseEnderpearl()) return false;

        PearlStrategy strategy = forcedStrategy != null
                ? forcedStrategy
                : strategyCalculator.determineOptimalStrategy(
                        bukkitBot,
                        target,
                        damageTracker.wasRecentlyDamaged(),
                        damageTracker.getDamageComboCount(),
                        damageTracker.getComboStartTime(),
                        repositionPearlCooldown,
                        aggressivePearlCooldown);

        if (!strategyCalculator.shouldUsePearlForStrategy(
                strategy,
                bukkitBot,
                target,
                damageTracker.wasRecentlyDamaged(),
                lastEmergencyPearl,
                repositionPearlCooldown,
                aggressivePearlCooldown)) return false;

        if (!inventoryController.isHoldingEnderpearl()) {
            inventoryController.switchToEnderpearl();
        }

        Vector targetPos = strategyCalculator.calculateTargetForStrategy(
                strategy, bukkitBot, target, targetTracker.getPredictedTargetMovement());
        if (targetPos == null) return false;

        startPearlPreparation(targetPos, strategy);
        return true;
    }

    public boolean tryUseEnderpearlToPosition(Vector targetPos) {
        if (!enabled) return false;
        if (!canUseEnderpearl() || isPreparingPearl) return false;
        if (!inventoryController.hasEnderpearls()) return false;
        if (targetPos == null) return false;

        Vector validatedTarget = validatePearlTarget(targetPos);
        if (validatedTarget == null) {
            return false;
        }

        if (!inventoryController.isHoldingEnderpearl()) {
            inventoryController.switchToEnderpearl();
        }

        startPearlPreparation(validatedTarget, PearlStrategy.ESCAPE);
        return true;
    }

    private void startPearlPreparation(Vector targetPos, PearlStrategy strategy) {
        isPreparingPearl = true;
        pendingThrowTarget = targetPos;
        preparationTicks = 0;
        currentStrategy = strategy;

        rotationController.lookAt(targetPos.getX(), targetPos.getY(), targetPos.getZ());
        lastPearlUseTime = System.currentTimeMillis();

        switch (strategy) {
            case COMBO_ESCAPE -> lastEmergencyPearl = System.currentTimeMillis();
            case REPOSITION_LOW -> repositionPearlCooldown = 100;
            case AGGRESSIVE_CLOSE -> aggressivePearlCooldown = 80;
            case ESCAPE, MELEE_DISENGAGE, ANCHOR_POSITION -> {}
        }
    }

    public boolean tryPearlToObsidianSide(BlockVector obsidianPos, Player target) {
        if (!enabled) return false;
        if (!canUseEnderpearl() || isPreparingPearl) return false;
        if (!inventoryController.hasEnderpearls()) return false;

        Vector targetPos = target.getLocation().toVector();
        Vector predictedTargetPos = targetPos.clone().add(target.getVelocity().multiply(8 / 20.0D));

        BlockVector bestSide = null;
        double bestScore = Double.NEGATIVE_INFINITY;

        int[][] horizontal = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        for (int[] offset : horizontal) {
            BlockVector sidePos = new BlockVector(
                    obsidianPos.getBlockX() + offset[0], obsidianPos.getBlockY(), obsidianPos.getBlockZ() + offset[1]);

            if (!safetyValidator.isSafeLandingSpot(sidePos)) continue;

            Vector sideCenter = centerOf(sidePos);
            double distToTarget = sideCenter.distance(predictedTargetPos);
            double distToBot = sideCenter.distance(bot.bukkitPosition());

            double score = 0;
            if (distToTarget < 8.0) {
                score += (8.0 - distToTarget) * 10;
            }
            if (distToBot > 6.0) {
                score += 20;
            }
            if (sidePos.getBlockY() <= target.getLocation().getBlockY()) {
                score += 15;
            }

            if (score > bestScore) {
                bestScore = score;
                bestSide = sidePos;
            }
        }

        if (bestSide == null) return false;

        if (!inventoryController.isHoldingEnderpearl()) {
            inventoryController.switchToEnderpearl();
        }

        this.currentTarget = target;
        startPearlPreparation(centerOf(bestSide), PearlStrategy.ANCHOR_POSITION);
        return true;
    }

    public boolean checkAndPerformAutoTeleport(Player target) {
        if (!enabled) return false;
        if (target == null || !target.isOnline()) return false;
        if (!canAutoTeleport()) return false;

        Vector botPos = bot.bukkitPosition();
        Vector targetPos = Objects.requireNonNull(target.getLocation(), "target location").toVector();

        double horizontalDistance =
                Math.sqrt(Math.pow(botPos.getX() - targetPos.getX(), 2) + Math.pow(botPos.getZ() - targetPos.getZ(), 2));
        double verticalDistance = Math.abs(botPos.getY() - targetPos.getY());
        double totalDistance = botPos.distance(targetPos);

        boolean needsTeleport = false;
        boolean requiresObstacleCheck = false;

        if (totalDistance > EMERGENCY_TELEPORT_DISTANCE) {
            needsTeleport = true;
            requiresObstacleCheck = totalDistance < EXTREME_EMERGENCY_TELEPORT_DISTANCE;
        } else if (horizontalDistance > AUTO_TELEPORT_HORIZONTAL_DISTANCE) {
            needsTeleport = true;
            requiresObstacleCheck = true;
        } else if (verticalDistance > AUTO_TELEPORT_VERTICAL_DISTANCE) {
            needsTeleport = true;
            requiresObstacleCheck = true;
        }

        if (needsTeleport && requiresObstacleCheck && !hasSolidObstacleBetween(botPos, targetPos)) {
            return false;
        }

        return needsTeleport && performAutoTeleport(target);
    }

    private boolean performAutoTeleport(Player target) {
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
                    rotationController.updateRotation(target);
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
        return currentTime - lastAutoTeleportTime >= AUTO_TELEPORT_COOLDOWN_MS && !teleportController.isTeleporting();
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

    private void throwEnderpearl(Vector targetPos) {
        pearlThrower.throwEnderpearl(bukkitBot, targetPos);

        enderpearlCooldown = ENDERPEARL_COOLDOWN_TICKS * 2;
        lastPearlUseTime = System.currentTimeMillis();

        handlePostPearlStrategy();

        if (currentTarget != null && currentTarget.isOnline()) {
            rotationController.updateRotation(currentTarget);
        }

        inventoryController.switchToSword();
    }

    private void handlePostPearlStrategy() {
        switch (currentStrategy) {
            case COMBO_ESCAPE, ESCAPE -> damageTracker.resetDamageState();
            case AGGRESSIVE_CLOSE, MELEE_DISENGAGE, ANCHOR_POSITION -> {}
            case REPOSITION_LOW -> repositionPearlCooldown = 100;
        }
    }

    public void tick() {
        if (!enabled) {
            isPreparingPearl = false;
            pendingThrowTarget = null;
            preparationTicks = 0;
            return;
        }
        if (enderpearlCooldown > 0) enderpearlCooldown--;
        if (aggressivePearlCooldown > 0) aggressivePearlCooldown--;
        if (repositionPearlCooldown > 0) repositionPearlCooldown--;

        damageTracker.tick();

        if (isPreparingPearl && pendingThrowTarget != null) {
            preparationTicks++;

            rotationController.lookAt(pendingThrowTarget.getX(), pendingThrowTarget.getY(), pendingThrowTarget.getZ());

            if (preparationTicks >= PREPARATION_TIME) {
                throwEnderpearl(pendingThrowTarget);

                isPreparingPearl = false;
                pendingThrowTarget = null;
                preparationTicks = 0;
            }
        }
    }

    public boolean canUseEnderpearl() {
        if (!enabled) return false;
        if (isPreparingPearl) return false;
        if (enderpearlCooldown > 0) return false;
        if (!inventoryController.hasEnderpearls()) return false;
        if (!bot.isAlive()) return false;
        if (System.currentTimeMillis() - lastPearlUseTime < MIN_TIME_BETWEEN_PEARLS_MS) return false;
        return bukkitBot.getVelocity().lengthSquared() <= 1.2D * 1.2D;
    }

    public int getCooldown() {
        return enderpearlCooldown;
    }

    public void setCooldown(int ticks) {
        this.enderpearlCooldown = ticks;
    }

    public boolean shouldUseEnderpearl(Player target) {
        if (!enabled) return false;
        PearlStrategy strategy = strategyCalculator.determineOptimalStrategy(
                bukkitBot,
                target,
                damageTracker.wasRecentlyDamaged(),
                damageTracker.getDamageComboCount(),
                damageTracker.getComboStartTime(),
                repositionPearlCooldown,
                aggressivePearlCooldown);
        return strategyCalculator.shouldUsePearlForStrategy(
                strategy,
                bukkitBot,
                target,
                damageTracker.wasRecentlyDamaged(),
                lastEmergencyPearl,
                repositionPearlCooldown,
                aggressivePearlCooldown);
    }

    public boolean wasRecentlyDamaged() {
        return damageTracker.wasRecentlyDamaged();
    }

    public boolean isThrowingPearl() {
        return isPreparingPearl;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (!enabled) {
            isPreparingPearl = false;
            pendingThrowTarget = null;
            preparationTicks = 0;
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getDamageComboCount() {
        return damageTracker.getDamageComboCount();
    }

    public PearlStrategy getCurrentStrategy() {
        return currentStrategy;
    }

    private boolean hasSolidObstacleBetween(Vector start, Vector end) {
        Vector delta = end.clone().subtract(start);
        double distance = delta.length();
        if (distance < 2.0D) {
            return false;
        }

        Vector dir = delta.normalize();
        int solidSamples = 0;
        double sampleStep = 1.25D;

        for (double t = 1.0D; t < distance; t += sampleStep) {
            Vector p = start.clone().add(dir.clone().multiply(t));
            Block block = blockAt(p);
            if (isSolid(block) || isSolid(block.getRelative(0, 1, 0))) {
                solidSamples++;
                if (solidSamples >= 2) {
                    return true;
                }
            }
        }

        return false;
    }

    private @Nullable Vector validatePearlTarget(Vector requestedTarget) {
        Vector botPos = bot.bukkitPosition();
        Vector candidate = requestedTarget.clone();
        double distance = botPos.distance(candidate);

        if (distance < 2.2D) {
            return null;
        }
        if (distance > 15.0D) {
            Vector direction = candidate.clone().subtract(botPos);
            if (direction.lengthSquared() < 1.0E-5D) {
                return null;
            }
            candidate = botPos.clone().add(direction.normalize().multiply(13.5D));
        }

        BlockVector candidateBlock = toBlockVector(candidate);
        Vector safeTarget = safetyValidator.isSafeLandingSpot(candidateBlock)
                ? centerOf(candidateBlock)
                : safetyValidator.findSafeLandingSpot(candidateBlock);

        if (safeTarget == null) {
            return null;
        }

        double safeDistance = botPos.distance(safeTarget);
        if (safeDistance < 2.2D || safeDistance > 15.0D) {
            return null;
        }

        return hasThrowPath(safeTarget) ? safeTarget : null;
    }

    private boolean hasThrowPath(Vector destination) {
        Vector eyes = bukkitBot.getEyeLocation().toVector();
        Vector delta = destination.clone().subtract(eyes);
        double distance = delta.length();
        if (distance < 1.0E-6D) {
            return true;
        }

        RayTraceResult result = bukkitBot.getWorld().rayTraceBlocks(
                bukkitBot.getEyeLocation(), delta.normalize(), distance, FluidCollisionMode.NEVER, true);
        if (result == null || result.getHitBlock() == null) {
            return true;
        }

        BlockVector destinationBlock = toBlockVector(destination);
        Block hitBlock = result.getHitBlock();
        return blockEquals(hitBlock, destinationBlock)
                || blockEquals(hitBlock.getRelative(0, -1, 0), destinationBlock)
                || blockEquals(hitBlock.getRelative(0, 1, 0), destinationBlock);
    }

    private Block blockAt(Vector value) {
        return bot.getWorld().getBlockAt(value.getBlockX(), value.getBlockY(), value.getBlockZ());
    }

    private static BlockVector toBlockVector(Vector value) {
        return new BlockVector(value.getBlockX(), value.getBlockY(), value.getBlockZ());
    }

    private static Vector centerOf(BlockVector value) {
        return new Vector(value.getBlockX() + 0.5D, value.getBlockY() + 0.5D, value.getBlockZ() + 0.5D);
    }

    private static boolean blockEquals(Block block, BlockVector vector) {
        return block.getX() == vector.getBlockX()
                && block.getY() == vector.getBlockY()
                && block.getZ() == vector.getBlockZ();
    }

    private static boolean isSolid(Block block) {
        Material type = block.getType();
        return type.isBlock() && type.isSolid() && !block.isPassable();
    }

    private static boolean isOnGround(Player player) {
        return Math.abs(player.getVelocity().getY()) < 1.0E-3D;
    }
}
