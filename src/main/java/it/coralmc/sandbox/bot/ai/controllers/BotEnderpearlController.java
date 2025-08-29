package it.coralmc.sandbox.bot.ai.controllers;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class BotEnderpearlController {
    private final Player bot;
    private final Level level;
    private final BotInventoryController inventoryController;
    private final BotRotationController rotationController;

    private int enderpearlCooldown = 0;
    private static final int ENDERPEARL_COOLDOWN_TICKS = 30;
    private static final double MIN_ENDERPEARL_DISTANCE = 4.0;
    private static final double MAX_ENDERPEARL_DISTANCE = 50.0;
    private static final int PREDICT_TICKS = 10;
    private static final double ESCAPE_DISTANCE = 3.5;
    private static final double CLOSE_TARGET_DISTANCE = 5.0;

    private float lastHealth;
    private long lastDamageTime = 0;
    private static final long DAMAGE_REACTION_WINDOW = 1000;
    private static final float HEALTH_THRESHOLD = 4.0f;

    private int lowHealthPearlCooldown = 0;
    private static final int LOW_HEALTH_PEARL_COOLDOWN = 100;

    private boolean wasRecentlyDamaged = false;
    private Player currentTarget;

    private boolean isPreparingPearl = false;
    private Vec3 pendingThrowTarget = null;
    private int preparationTicks = 0;
    private static final int PREPARATION_TIME = 3;

    public BotEnderpearlController(Player bot, BotInventoryController inventoryController, BotRotationController rotationController) {
        this.bot = bot;
        this.level = bot.level();
        this.inventoryController = inventoryController;
        this.lastHealth = bot.getHealth();
        this.rotationController = rotationController;
    }

    public boolean tryUseEnderpearl(Player target) {
        if (!canUseEnderpearl() || isPreparingPearl) {
            return false;
        }

        double distance = bot.distanceTo(target);

        if (!inventoryController.hasEnderpearls()) {
            return false;
        }

        if (!shouldUseEnderpearlIntelligent(target, distance)) {
            return false;
        }

        if (!inventoryController.isHoldingEnderpearl()) {
            inventoryController.switchToEnderpearl();
        }

        Vec3 targetPos;
        if (distance < 2.5 || (bot.getHealth() <= HEALTH_THRESHOLD && distance < 5.0)) {
            targetPos = calculateEscapeTarget(target);
        } else if (distance < 6.0 && wasRecentlyDamaged) {
            targetPos = calculateStrafeTarget(target);
        } else if (distance > 8.0) {
            targetPos = calculateApproachTarget(target);
        } else if (distance >= MIN_ENDERPEARL_DISTANCE && distance <= MAX_ENDERPEARL_DISTANCE) {
            targetPos = calculateThrowTarget(target);
        } else {
            return false;
        }

        if (targetPos == null) {
            return false;
        }

        this.currentTarget = target;

        startPearlPreparation(targetPos);

        wasRecentlyDamaged = false;
        return true;
    }

    private void startPearlPreparation(Vec3 targetPos) {
        isPreparingPearl = true;
        pendingThrowTarget = targetPos;
        preparationTicks = 0;

        rotationController.lookAt(targetPos.x, targetPos.y, targetPos.z);
    }

    private boolean shouldUseEnderpearlIntelligent(Player target, double distance) {
        float currentHealth = bot.getHealth();
        long currentTime = System.currentTimeMillis();

        if (currentHealth < lastHealth) {
            lastDamageTime = currentTime;
            wasRecentlyDamaged = true;
        }

        if (currentTime - lastDamageTime > DAMAGE_REACTION_WINDOW) {
            wasRecentlyDamaged = false;
        }

        lastHealth = currentHealth;

        if (currentHealth <= HEALTH_THRESHOLD) {
            if (lowHealthPearlCooldown <= 0) {
                lowHealthPearlCooldown = LOW_HEALTH_PEARL_COOLDOWN;
                return true;
            }
            return false;
        }

        if (wasRecentlyDamaged && distance < 4.0) {
            return true;
        }

        if (distance < 2.5) {
            return true;
        }

        if (distance >= MIN_ENDERPEARL_DISTANCE && distance <= MAX_ENDERPEARL_DISTANCE) {
            return enderpearlCooldown <= 0 && Math.random() < 0.3;
        }

        return false;
    }

    private Vec3 calculateEscapeTarget(Player target) {
        Vec3 botPos = bot.position();
        Vec3 targetPos = target.position();
        Vec3 baseDir;

        int choice = (int)(Math.random() * 4);
        baseDir = switch (choice) {
            case 0 -> botPos.subtract(targetPos).normalize();
            case 1 -> targetPos.subtract(botPos).normalize();
            case 2 -> new Vec3(-(targetPos.z - botPos.z), 0, targetPos.x - botPos.x).normalize();
            default -> new Vec3(targetPos.z - botPos.z, 0, -(targetPos.x - botPos.x)).normalize();
        };

        double escapeMultiplier = bot.getHealth() <= HEALTH_THRESHOLD ? 1.8 : 1.2;

        double angle = (Math.random() - 0.5) * Math.PI / 3;
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);

        Vec3 dir = new Vec3(
                baseDir.x * cos - baseDir.z * sin,
                0,
                baseDir.x * sin + baseDir.z * cos
        ).normalize();

        double randomY = (Math.random() - 0.5) * 2.5;

        Vec3 escapePos = botPos.add(dir.scale(ESCAPE_DISTANCE * escapeMultiplier)).add(0, randomY, 0);
        BlockPos escapeBlock = BlockPos.containing(escapePos);

        if (isSafeLandingSpot(escapeBlock)) {
            return escapePos;
        }

        return findSafeLandingSpot(escapeBlock);
    }

    private Vec3 calculateStrafeTarget(Player target) {
        Vec3 targetPos = target.position();
        double angle = Math.random() * 2 * Math.PI;
        double radius = ESCAPE_DISTANCE + (Math.random() * 3.0);

        double offsetX = Math.cos(angle) * radius;
        double offsetZ = Math.sin(angle) * radius;
        double offsetY = (Math.random() - 0.5) * 3.0;

        Vec3 strafePos = targetPos.add(offsetX, offsetY, offsetZ);
        BlockPos strafeBlock = BlockPos.containing(strafePos);

        if (isSafeLandingSpot(strafeBlock)) {
            return strafePos;
        }

        return findSafeLandingSpot(strafeBlock);
    }

    private Vec3 calculateThrowTarget(Player target) {
        Vec3 botPos = bot.position();
        double angle = Math.random() * 2 * Math.PI;
        double distance = 5 + Math.random() * 10;

        double offsetX = Math.cos(angle) * distance;
        double offsetZ = Math.sin(angle) * distance;
        double offsetY = (Math.random() - 0.5) * 5.0;

        Vec3 throwTarget = botPos.add(offsetX, offsetY, offsetZ);
        BlockPos targetBlock = BlockPos.containing(throwTarget);

        if (isSafeLandingSpot(targetBlock)) {
            return throwTarget;
        }

        return findSafeLandingSpot(targetBlock);
    }

    private Vec3 calculateApproachTarget(Player target) {
        Vec3 botPos = bot.position();
        Vec3 targetPos = target.position();
        Vec3 velocity = target.getDeltaMovement();

        double distance = bot.distanceTo(target);
        int ticksAhead = Math.min(PREDICT_TICKS + (int)(distance / 3), 20);

        Vec3 predictedPos = targetPos.add(velocity.scale(ticksAhead));
        Vec3 directionToTarget = predictedPos.subtract(botPos).normalize();
        Vec3 approachPos = predictedPos.subtract(directionToTarget.scale(1.5));

        double offsetY = (Math.random() - 0.3) * 2.0;
        approachPos = approachPos.add(0, offsetY, 0);

        BlockPos blockPos = BlockPos.containing(approachPos);

        if (isSafeLandingSpot(blockPos)) {
            return approachPos;
        }

        return findSafeLandingSpot(blockPos);
    }

    private boolean isSafeLandingSpot(BlockPos pos) {
        if (level.getBlockState(pos.below()).isAir()) {
            return false;
        }

        if (!level.getBlockState(pos).isAir() || !level.getBlockState(pos.above()).isAir()) {
            return false;
        }

        return true;
    }

    private Vec3 findSafeLandingSpot(BlockPos center) {
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                BlockPos checkPos = center.offset(x, 0, z);
                if (isSafeLandingSpot(checkPos)) {
                    return Vec3.atCenterOf(checkPos);
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

        Vec3 velocity = direction.normalize().scale(Math.min(distance * 0.1, 1.5));
        velocity = velocity.add(0, 0.2, 0);

        enderpearl.setPos(botPos.x, botPos.y, botPos.z);
        enderpearl.setDeltaMovement(velocity);
        level.addFreshEntity(enderpearl);

        bot.swing(InteractionHand.MAIN_HAND);
        bot.playSound(net.minecraft.sounds.SoundEvents.ENDER_PEARL_THROW, 0.5f, 0.4f / (level.getRandom().nextFloat() * 0.4f + 0.8f));

        enderpearlCooldown = ENDERPEARL_COOLDOWN_TICKS;

        if (currentTarget != null && currentTarget.isAlive()) {
            rotationController.updateRotation(currentTarget);
        }

        inventoryController.switchToSword();
    }

    public boolean canUseEnderpearl() {
        return enderpearlCooldown <= 0 && inventoryController.hasEnderpearls() && bot.isAlive() && !isPreparingPearl;
    }

    public void tick() {
        if (enderpearlCooldown > 0) {
            enderpearlCooldown--;
        }

        if (lowHealthPearlCooldown > 0) {
            lowHealthPearlCooldown--;
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

    public int getCooldown() {
        return enderpearlCooldown;
    }

    public void setCooldown(int ticks) {
        this.enderpearlCooldown = ticks;
    }

    public boolean shouldUseEnderpearl(Player target) {
        double distance = bot.distanceTo(target);
        return shouldUseEnderpearlIntelligent(target, distance);
    }

    public void onDamageReceived() {
        wasRecentlyDamaged = true;
        lastDamageTime = System.currentTimeMillis();
    }

    public boolean wasRecentlyDamaged() {
        return wasRecentlyDamaged;
    }

    public boolean isThrowingPearl() {
        return isPreparingPearl;
    }
}