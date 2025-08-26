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

    private int enderpearlCooldown = 0;
    private static final int ENDERPEARL_COOLDOWN_TICKS = 20;
    private static final double MIN_ENDERPEARL_DISTANCE = 8.0;
    private static final double MAX_ENDERPEARL_DISTANCE = 50.0;
    private static final int PREDICT_TICKS = 10;

    public BotEnderpearlController(Player bot, BotInventoryController inventoryController) {
        this.bot = bot;
        this.level = bot.level();
        this.inventoryController = inventoryController;
    }

    public boolean tryUseEnderpearl(Player target) {
        if (!canUseEnderpearl()) {
            return false;
        }

        double distance = bot.distanceTo(target);

        if (distance < MIN_ENDERPEARL_DISTANCE || distance > MAX_ENDERPEARL_DISTANCE) {
            return false;
        }

        if (!inventoryController.hasEnderpearls()) {
            return false;
        }

        if (!inventoryController.isHoldingEnderpearl()) {
            inventoryController.switchToEnderpearl();
        }

        Vec3 targetPos = calculateThrowTarget(target);
        if (targetPos == null) {
            return false;
        }

        throwEnderpearl(targetPos);

        inventoryController.switchToSword();

        return true;
    }

    private Vec3 calculateThrowTarget(Player target) {
        Vec3 targetPos = target.position();
        Vec3 velocity = target.getDeltaMovement();

        double distance = bot.distanceTo(target);
        int ticksAhead = Math.min(PREDICT_TICKS + (int)(distance / 3), 20);

        Vec3 predictedPos = targetPos.add(velocity.scale(ticksAhead));

        Vec3 botPos = bot.position();
        Vec3 directionToTarget = predictedPos.subtract(botPos).normalize();

        Vec3 throwTarget = predictedPos.subtract(directionToTarget.scale(1.5));

        BlockPos targetBlock = BlockPos.containing(throwTarget);

        if (isSafeLandingSpot(targetBlock)) {
            return throwTarget;
        }

        return findSafeLandingSpot(targetBlock);
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

        inventoryController.consumeEnderpearl();

        enderpearlCooldown = ENDERPEARL_COOLDOWN_TICKS;
    }

    public boolean canUseEnderpearl() {
        return enderpearlCooldown <= 0 &&
                inventoryController.hasEnderpearls() &&
                bot.isAlive();
    }

    public void tick() {
        if (enderpearlCooldown > 0) {
            enderpearlCooldown--;
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

        if (distance >= MIN_ENDERPEARL_DISTANCE && distance <= MAX_ENDERPEARL_DISTANCE) {
            return true;
        }

        return false;
    }
}