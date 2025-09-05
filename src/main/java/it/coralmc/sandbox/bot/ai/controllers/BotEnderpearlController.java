package it.coralmc.sandbox.bot.ai.controllers;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class BotEnderpearlController {

    public enum PearlStrategy {
        ESCAPE,
        REPOSITION_LOW,
        MELEE_DISENGAGE,
        ANCHOR_POSITION,
        AGGRESSIVE_CLOSE,
        COMBO_ESCAPE
    }

    private final Player bot;
    private final Level level;
    private final BotInventoryController inventoryController;
    private final BotRotationController rotationController;

    private int enderpearlCooldown = 0;
    private static final int ENDERPEARL_COOLDOWN_TICKS = 30;
    private static final double MIN_ENDERPEARL_DISTANCE = 4.0;
    private static final double MAX_ENDERPEARL_DISTANCE = 10.0;
    private static final int PREDICT_TICKS = 8;

    private float lastHealth;
    private long lastDamageTime = 0;
    private static final long DAMAGE_REACTION_WINDOW = 2000;
    private static final float CRITICAL_HEALTH_THRESHOLD = 6.0f;
    private static final float LOW_HEALTH_THRESHOLD = 8.0f;
    private int damageComboCount = 0;
    private long comboStartTime = 0;

    private boolean wasRecentlyDamaged = false;
    private Player currentTarget;
    private Vec3 lastTargetPosition;
    private Vec3 predictedTargetMovement = Vec3.ZERO;

    private boolean isPreparingPearl = false;
    private Vec3 pendingThrowTarget = null;
    private int preparationTicks = 0;
    private static final int PREPARATION_TIME = 3;
    private PearlStrategy currentStrategy = PearlStrategy.ESCAPE;

    private int aggressivePearlCooldown = 0;
    private int repositionPearlCooldown = 0;
    private long lastEmergencyPearl = 0;
    private static final long EMERGENCY_PEARL_COOLDOWN = 3000;

    private long lastPearlUseTime = 0;
    private static final long MIN_TIME_BETWEEN_PEARLS_MS = 700;
    private static final double MIN_USE_DISTANCE = 3.5;
    private static final double MAX_USE_DISTANCE = 16.0;

    public BotEnderpearlController(Player bot, BotInventoryController inventoryController, BotRotationController rotationController) {
        this.bot = bot;
        this.level = bot.level();
        this.inventoryController = inventoryController;
        this.lastHealth = bot.getHealth();
        this.rotationController = rotationController;
    }

    public boolean tryUseEnderpearl(Player target) {
        return tryUseEnderpearl(target, null);
    }

    public boolean tryUseEnderpearl(Player target, PearlStrategy forcedStrategy) {
        if (isPreparingPearl) return false;
        if (!inventoryController.hasEnderpearls()) return false;

        this.currentTarget = target;
        updateTargetTracking(target);

        double dist = bot.distanceTo(target);
        if (dist < MIN_USE_DISTANCE && forcedStrategy != PearlStrategy.COMBO_ESCAPE) return false;
        if (dist > MAX_USE_DISTANCE && forcedStrategy != PearlStrategy.AGGRESSIVE_CLOSE) return false;

        try {
            if (!bot.onGround() && forcedStrategy == null) return false;
        } catch (Exception ignored) {}

        if (!canUseEnderpearl()) return false;

        PearlStrategy strategy = forcedStrategy != null ? forcedStrategy : determineOptimalStrategy(target);

        if (!shouldUsePearlForStrategy(strategy, target)) return false;

        if (!inventoryController.isHoldingEnderpearl()) {
            inventoryController.switchToEnderpearl();
        }

        Vec3 targetPos = calculateTargetForStrategy(strategy, target);
        if (targetPos == null) return false;

        startPearlPreparation(targetPos, strategy);
        return true;
    }

    private PearlStrategy determineOptimalStrategy(Player target) {
        double distance = bot.distanceTo(target);
        float healthPercent = bot.getHealth() / bot.getMaxHealth();
        Vec3 botPos = bot.position();
        Vec3 targetPos = target.position();
        double yDiff = botPos.y - targetPos.y;

        long currentTime = System.currentTimeMillis();

        if (bot.position().y > target.position().y &&
                distance > 4.0 && distance < 15.0) {
            return PearlStrategy.REPOSITION_LOW;
        }

        if (healthPercent < 0.3f ||
                (damageComboCount >= 2 && currentTime - comboStartTime < 2000)) {
            return PearlStrategy.COMBO_ESCAPE;
        }


        if (wasRecentlyDamaged && distance < 4.0) {
            return PearlStrategy.ESCAPE;
        }

        if (distance < 2.5 && healthPercent < 0.6f) {
            return PearlStrategy.MELEE_DISENGAGE;
        }

        if (yDiff > 1.5 && distance > 4.0 && distance < 15.0 && repositionPearlCooldown <= 0) {
            return PearlStrategy.REPOSITION_LOW;
        }

        if (target.onGround() && distance > 5.0 && distance < 12.0 && yDiff > 0) {
            return PearlStrategy.ANCHOR_POSITION;
        }

        if (distance > 8.0 && healthPercent > 0.6f && aggressivePearlCooldown <= 0) {
            return PearlStrategy.AGGRESSIVE_CLOSE;
        }

        return PearlStrategy.ESCAPE;
    }

    public boolean tryUseEnderpearlToPosition(Vec3 targetPos) {
        if (!canUseEnderpearl() || isPreparingPearl) return false;
        if (!inventoryController.hasEnderpearls()) return false;

        if (!inventoryController.isHoldingEnderpearl()) {
            inventoryController.switchToEnderpearl();
        }

        startPearlPreparation(targetPos, PearlStrategy.ESCAPE);
        return true;
    }

    private boolean shouldUsePearlForStrategy(PearlStrategy strategy, Player target) {
        double distance = bot.distanceTo(target);
        long currentTime = System.currentTimeMillis();

        switch (strategy) {
            case COMBO_ESCAPE:
                return currentTime - lastEmergencyPearl > EMERGENCY_PEARL_COOLDOWN;

            case ESCAPE:
                return wasRecentlyDamaged || bot.getHealth() < LOW_HEALTH_THRESHOLD;

            case MELEE_DISENGAGE:
                return distance < 3.0 && (wasRecentlyDamaged || bot.getHealth() < 10.0f);

            case REPOSITION_LOW:
                return repositionPearlCooldown <= 0 && distance > 4.0;

            case ANCHOR_POSITION:
                return target.onGround() && distance > 4.0;

            case AGGRESSIVE_CLOSE:
                return aggressivePearlCooldown <= 0 && distance > 6.0 && bot.getHealth() > 8.0f;

            default:
                return Math.random() < 0.4;
        }
    }

    private Vec3 calculateTargetForStrategy(PearlStrategy strategy, Player target) {
        Vec3 botPos = bot.position();
        Vec3 targetPos = target.position();
        Vec3 predictedTargetPos = targetPos.add(predictedTargetMovement.scale(PREDICT_TICKS / 20.0));

        switch (strategy) {
            case COMBO_ESCAPE:
            case ESCAPE:
                return calculateEmergencyEscape(target);

            case MELEE_DISENGAGE:
                return calculateMeleeDisengage(target);

            case REPOSITION_LOW:
                return calculateLowGroundPosition(target);

            case ANCHOR_POSITION:
                return calculateAnchorPosition(target);

            case AGGRESSIVE_CLOSE:
                return calculateAggressiveApproach(target);

            default:
                return calculateStandardEscape(target);
        }
    }

    private Vec3 calculateEmergencyEscape(Player target) {
        Vec3 botPos = bot.position();
        Vec3 targetPos = target.position();
        Vec3 awayDirection = botPos.subtract(targetPos).normalize();

        double distance = 6.0 + Math.random() * 3.0;
        double yOffset = Math.random() * 2.0;

        Vec3 escapePos = botPos.add(awayDirection.scale(distance)).add(0, yOffset, 0);
        BlockPos escapeBlock = BlockPos.containing(escapePos);

        if (isSafeLandingSpot(escapeBlock)) {
            return escapePos;
        }

        return findSafeLandingSpot(BlockPos.containing(botPos.add(awayDirection.scale(7))));
    }


    private Vec3 calculateMeleeDisengage(Player target) {
        Vec3 botPos = bot.position();
        Vec3 targetPos = target.position();
        Vec3 awayDirection = botPos.subtract(targetPos).normalize();

        double distance = 6.0 + Math.random() * 3.0;
        double yBoost = 1.5 + Math.random() * 2.0;

        Vec3 disengagePos = botPos.add(awayDirection.scale(distance)).add(0, yBoost, 0);
        BlockPos disengageBlock = BlockPos.containing(disengagePos);

        if (isSafeLandingSpot(disengageBlock)) {
            return disengagePos;
        }

        return findSafeLandingSpot(disengageBlock);
    }

    private Vec3 calculateLowGroundPosition(Player target) {
        Vec3 targetPos = target.position();
        Vec3 botPos = bot.position();

        int targetY = target.blockPosition().getY();
        int desiredY = targetY - 2 - (int)(Math.random() * 2);

        double radius = 4.0 + Math.random() * 2.0;
        double angle = Math.random() * 2 * Math.PI;

        Vec3 lowGroundPos = targetPos.add(
                Math.cos(angle) * radius,
                desiredY - targetPos.y,
                Math.sin(angle) * radius
        );

        BlockPos checkPos = BlockPos.containing(lowGroundPos);
        if (isSafeLandingSpot(checkPos)) {
            repositionPearlCooldown = 60;
            return lowGroundPos;
        }

        return findSafeLandingSpot(checkPos);
    }

    private Vec3 calculateAnchorPosition(Player target) {
        Vec3 targetPos = target.position();

        double distance = 5.0 + Math.random() * 3.0;
        double angle = Math.random() * 2 * Math.PI;

        Vec3 anchorPos = targetPos.add(
                Math.cos(angle) * distance,
                -1.0,
                Math.sin(angle) * distance
        );

        BlockPos checkPos = BlockPos.containing(anchorPos);
        if (isSafeLandingSpot(checkPos)) {
            return anchorPos;
        }

        return findSafeLandingSpot(checkPos);
    }

    private Vec3 calculateAggressiveApproach(Player target) {
        Vec3 targetPos = target.position();
        Vec3 predictedPos = targetPos.add(predictedTargetMovement.scale(PREDICT_TICKS / 20.0));

        Vec3 botPos = bot.position();
        Vec3 toTarget = predictedPos.subtract(botPos).normalize();

        double approachDistance = 2.5 + Math.random() * 1.5;

        Vec3 approachPos = predictedPos.subtract(toTarget.scale(approachDistance));
        approachPos = approachPos.add(0, Math.random(), 0);

        BlockPos checkPos = BlockPos.containing(approachPos);
        if (isSafeLandingSpot(checkPos)) {
            aggressivePearlCooldown = 40;
            return approachPos;
        }

        return findSafeLandingSpot(checkPos);
    }


    private Vec3 calculateStandardEscape(Player target) {
        Vec3 botPos = bot.position();
        Vec3 targetPos = target.position();
        Vec3 baseDirection = botPos.subtract(targetPos).normalize();

        double angle = (Math.random() - 0.5) * Math.PI / 2;
        Vec3 escapeDirection = rotateVector(baseDirection, angle);

        double escapeDistance = 8.0 + Math.random() * 6.0;
        double yOffset = (Math.random() - 0.3) * 3.0;

        Vec3 escapePos = botPos.add(escapeDirection.scale(escapeDistance)).add(0, yOffset, 0);
        BlockPos checkPos = BlockPos.containing(escapePos);

        if (isSafeLandingSpot(checkPos)) {
            return escapePos;
        }

        return findSafeLandingSpot(checkPos);
    }

    private Vec3 rotateVector(Vec3 vector, double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        return new Vec3(
                vector.x * cos - vector.z * sin,
                vector.y,
                vector.x * sin + vector.z * cos
        );
    }

    private void updateTargetTracking(Player target) {
        Vec3 currentPos = target.position();
        if (lastTargetPosition != null) {
            Vec3 movement = currentPos.subtract(lastTargetPosition);
            predictedTargetMovement = movement.scale(0.8).add(predictedTargetMovement.scale(0.2));
        }
        lastTargetPosition = currentPos;
    }

    private void startPearlPreparation(Vec3 targetPos, PearlStrategy strategy) {
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


    private boolean isSafeLandingSpot(BlockPos pos) {
        if (level.getBlockState(pos.below()).isAir()) {
            return false;
        }

        if (!level.getBlockState(pos).isAir() || !level.getBlockState(pos.above()).isAir()) {
            return false;
        }

        if (level.getBlockState(pos.below()).getBlock().toString().contains("lava") ||
                level.getBlockState(pos.below()).getBlock().toString().contains("cactus")) {
            return false;
        }

        return true;
    }

    private Vec3 findSafeLandingSpot(BlockPos center) {
        for (int radius = 1; radius <= 3; radius++) {
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    for (int y = -1; y <= 2; y++) {
                        BlockPos checkPos = center.offset(x, y, z);
                        if (isSafeLandingSpot(checkPos)) {
                            return Vec3.atCenterOf(checkPos);
                        }
                    }
                }
            }
        }
        return null;
    }

    private void throwEnderpearl(Vec3 targetPos) {
        ThrownEnderpearl enderpearl = new ThrownEnderpearl(net.minecraft.world.entity.EntityType.ENDER_PEARL, level);
        enderpearl.setOwner(bot);

        Vec3 botPos = bot.position().add(0, bot.getEyeHeight(), 0);
        Vec3 direction = targetPos.subtract(botPos);
        double distance = direction.length();

        double velocityScale;
        if (distance < 15) {
            velocityScale = Math.min(distance * 0.08, 1.2);
        } else {
            velocityScale = Math.min(distance * 0.06, 1.8);
        }

        Vec3 velocity = direction.normalize().scale(velocityScale);

        double gravityCompensation = distance * 0.02;
        velocity = velocity.add(0, 0.2 + gravityCompensation, 0);

        enderpearl.setPos(botPos.x, botPos.y, botPos.z);
        enderpearl.setDeltaMovement(velocity);
        level.addFreshEntity(enderpearl);

        bot.swing(InteractionHand.MAIN_HAND);
        bot.playSound(net.minecraft.sounds.SoundEvents.ENDER_PEARL_THROW, 0.5f, 0.4f / (level.getRandom().nextFloat() * 0.4f + 0.8f));

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
                wasRecentlyDamaged = false;
                damageComboCount = 0;
            }
            case AGGRESSIVE_CLOSE -> {
            }
            case REPOSITION_LOW -> {
                repositionPearlCooldown = 100;
            }
        }
    }

    public boolean tryPearlToObsidianSide(BlockPos obsidianPos, Player target) {
        if (!canUseEnderpearl() || isPreparingPearl) return false;
        if (!inventoryController.hasEnderpearls()) return false;

        Vec3 targetPos = target.position();
        Vec3 velocity = target.getDeltaMovement();
        Vec3 predictedTargetPos = targetPos.add(velocity.scale(PREDICT_TICKS / 20.0));

        BlockPos bestSide = null;
        double bestScore = Double.NEGATIVE_INFINITY;

        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos sidePos = obsidianPos.relative(dir);

            if (!isSafeLandingSpot(sidePos)) continue;

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
        startPearlPreparation(pearlTarget, PearlStrategy.ANCHOR_POSITION);
        return true;
    }

    public void onDamageReceived() {
        float currentHealth = bot.getHealth();
        long currentTime = System.currentTimeMillis();

        if (currentHealth < lastHealth) {
            wasRecentlyDamaged = true;
            lastDamageTime = currentTime;

            if (currentTime - comboStartTime < DAMAGE_REACTION_WINDOW) {
                damageComboCount++;
            } else {
                damageComboCount = 1;
                comboStartTime = currentTime;
            }
        }

        lastHealth = currentHealth;
    }

    public void tick() {
        if (enderpearlCooldown > 0) enderpearlCooldown--;
        if (aggressivePearlCooldown > 0) aggressivePearlCooldown--;
        if (repositionPearlCooldown > 0) repositionPearlCooldown--;

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastDamageTime > DAMAGE_REACTION_WINDOW) {
            wasRecentlyDamaged = false;
        }

        if (currentTime - comboStartTime > DAMAGE_REACTION_WINDOW * 2) {
            damageComboCount = 0;
        }

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
        PearlStrategy strategy = determineOptimalStrategy(target);
        return shouldUsePearlForStrategy(strategy, target);
    }

    public boolean wasRecentlyDamaged() {
        return wasRecentlyDamaged;
    }

    public boolean isThrowingPearl() {
        return isPreparingPearl;
    }

    public int getDamageComboCount() {
        return damageComboCount;
    }

    public PearlStrategy getCurrentStrategy() {
        return currentStrategy;
    }
}