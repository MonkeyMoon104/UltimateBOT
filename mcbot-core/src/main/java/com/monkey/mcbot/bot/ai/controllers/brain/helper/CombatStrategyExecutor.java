package com.monkey.mcbot.bot.ai.controllers.brain.helper;

import com.monkey.mcbot.bot.ai.ITrainingBot;
import com.monkey.mcbot.bot.ai.controllers.attack.BotAttackController;
import com.monkey.mcbot.bot.ai.controllers.brain.helper.inter.ICombatDataManager;
import com.monkey.mcbot.bot.ai.controllers.brain.helper.inter.ICombatStateManager;
import com.monkey.mcbot.bot.ai.controllers.brain.helper.inter.ICombatStrategyExecutor;
import com.monkey.mcbot.bot.ai.controllers.cpvp.BotCPVPController;
import com.monkey.mcbot.bot.ai.controllers.enderpearl.BotEnderpearlController;
import com.monkey.mcbot.bot.ai.controllers.enderpearl.helper.inter.IPearlStrategyCalculator;
import com.monkey.mcbot.bot.ai.controllers.inventory.BotInventoryController;
import com.monkey.mcbot.bot.ai.controllers.movement.BotMovementController;
import com.monkey.mcbot.bot.ai.controllers.movement.helper.MovementPattern;
import com.monkey.mcbot.bot.ai.controllers.rapvp.BotRAPVPController;
import com.monkey.mcbot.bot.ai.controllers.rotation.BotRotationController;
import com.monkey.mcbot.bot.ai.rank.BotRank;
import java.util.Random;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class CombatStrategyExecutor implements ICombatStrategyExecutor {

    private final Player bot;
    private final BotMovementController movementController;
    private final BotRotationController rotationController;
    private final BotAttackController attackController;
    private final BotInventoryController inventoryController;
    private final BotEnderpearlController enderpearlController;
    private final BotCPVPController cpvpController;
    private final BotRAPVPController rapvpController;
    private final ICombatStateManager combatStateManager;
    private final ICombatDataManager combatDataManager;

    private int aggressionCooldown = 0;
    private int repositionTimer = 0;
    private final Random random = new Random();

    public CombatStrategyExecutor(
            Player bot,
            BotMovementController movementController,
            BotRotationController rotationController,
            BotAttackController attackController,
            BotInventoryController inventoryController,
            BotEnderpearlController enderpearlController,
            BotCPVPController cpvpController,
            BotRAPVPController rapvpController,
            ICombatStateManager combatStateManager,
            ICombatDataManager combatDataManager) {
        this.bot = bot;
        this.movementController = movementController;
        this.rotationController = rotationController;
        this.attackController = attackController;
        this.inventoryController = inventoryController;
        this.enderpearlController = enderpearlController;
        this.cpvpController = cpvpController;
        this.rapvpController = rapvpController;
        this.combatStateManager = combatStateManager;
        this.combatDataManager = combatDataManager;
    }

    @Override
    public void executeCombatStrategy(Player target) {
        if (!((ITrainingBot) bot).isCombat()) {
            basicFollowBehavior(target);
            return;
        }
        double distance = bot.distanceTo(target);

        if (isManagedBotTarget(target)) {
            executeMeleeCombat(target);
            return;
        }

        boolean actionExecuted =
                switch (combatStateManager.getCurrentState()) {
                    case AGGRESSIVE -> executeAggressive(target, distance);
                    case DEFENSIVE -> executeDefensive(target, distance);
                    case REPOSITIONING -> executeRepositioning(target, distance);
                    case ANCHOR_SETUP -> executeAnchorSetup(target, distance);
                    case CRYSTAL_SETUP -> executeCrystalSetup(target, distance);
                    case RETREATING -> executeRetreating(target, distance);
                };

        if (!actionExecuted) {
            moveToTarget(target, 3.0);
            if (distance <= 3.5 && ((ITrainingBot) bot).isCombat()) {
                attackController.handleAttack(target);
            }
        }

        if (!enderpearlController.isThrowingPearl()
                && !cpvpController.isDoingCrystalAction()
                && !rapvpController.isActive()) {
            rotationController.updateRotation(target);
        }

        if (aggressionCooldown > 0) aggressionCooldown--;
        if (repositionTimer > 0) repositionTimer--;
    }

    @Override
    public boolean executeAggressive(Player target, double distance) {
        boolean hyperAggressive = isHyperAggressiveRank();

        if (distance <= 3.5 && ((ITrainingBot) bot).isCombat()) {
            maybeBoostMeleeTempo(hyperAggressive);
            attackController.handleAttack(target);
        }
        double targetDistance = 2.5;

        double yDiff = bot.position().y - target.position().y;

        if (!hyperAggressive && distance > 10.0 && enderpearlController.canUseEnderpearl()) {
            enderpearlController.tryUseEnderpearl(target, IPearlStrategyCalculator.PearlStrategy.AGGRESSIVE_CLOSE);
            return true;
        }

        if (hyperAggressive && distance > 13.0 && yDiff > 3.5 && enderpearlController.canUseEnderpearl()) {
            enderpearlController.tryUseEnderpearl(target, IPearlStrategyCalculator.PearlStrategy.REPOSITION_LOW);
            return true;
        }

        if (!hyperAggressive && distance > 6.0 && enderpearlController.canUseEnderpearl()) {
            if (enderpearlController.wasRecentlyDamaged() || distance > 9.0) {
                enderpearlController.tryUseEnderpearl(target);
                return true;
            }
        }

        if (distance <= 1.8) {
            Vec3 strafeDirection = getStrafeDirection(target);
            movementController.moveToPosition(bot.position().add(strafeDirection.scale(1.9)));
        } else {
            moveToTarget(target, targetDistance);
        }

        if (!inventoryController.isHoldingSword() && distance <= 4.0) {
            inventoryController.switchToSword();
        }

        if (distance <= 3.5 && ((ITrainingBot) bot).isCombat()) {
            maybeBoostMeleeTempo(hyperAggressive);
            attackController.handleAttack(target);
        }

        return true;
    }

    @Override
    public boolean executeDefensive(Player target, double distance) {
        if (!((ITrainingBot) bot).isCombat()) return false;
        boolean hyperAggressive = isHyperAggressiveRank();

        if (shouldForceAnchorBreakout(target, distance)) {
            if (!rapvpController.isActive()) {
                rapvpController.enable(target);
            }

            double anchorBreakoutDistance = Math.max(3.8D, Math.min(4.8D, distance + 0.6D));
            moveToTarget(target, anchorBreakoutDistance);
            if (distance <= 3.4D) {
                maybeBoostMeleeTempo(hyperAggressive);
                attackController.handleAttack(target);
            }
            return true;
        }

        if (distance <= 3.2) {
            attackController.handleAttack(target);
        }

        if (!hyperAggressive
                && (enderpearlController.wasRecentlyDamaged() || combatDataManager.getConsecutiveDamageCount() >= 2)) {
            if (enderpearlController.canUseEnderpearl()) {
                enderpearlController.tryUseEnderpearl(target);
                return true;
            }
        } else if (hyperAggressive
                && bot.getHealth() / bot.getMaxHealth() < 0.12f
                && distance > 8.0
                && enderpearlController.canUseEnderpearl()) {
            enderpearlController.tryUseEnderpearl(target, IPearlStrategyCalculator.PearlStrategy.REPOSITION_LOW);
            return true;
        }

        double targetDistance = hyperAggressive
                ? Math.min(6.0, Math.max(3.8, distance + 0.5))
                : Math.min(8.0, Math.max(5.0, distance + 1.5));
        moveToTarget(target, targetDistance);

        int botY = bot.blockPosition().getY();
        int targetY = target.blockPosition().getY();
        int yDiff = targetY - botY;

        if (yDiff < 2) {
            if (!inventoryController.isHoldingAnchor()
                    && inventoryController.hasItem(net.minecraft.world.item.Items.RESPAWN_ANCHOR)) {
                inventoryController.switchToAnchor();
            }
        } else {
            if (!inventoryController.isHoldingCrystal() && cpvpController.canPlaceCrystal()) {
                inventoryController.switchToCrystal();
            }
        }

        if (distance <= 3.5 && ((ITrainingBot) bot).isCombat()) {
            maybeBoostMeleeTempo(hyperAggressive);
            attackController.handleAttack(target);
        }

        return true;
    }

    @Override
    public boolean executeRepositioning(Player target, double distance) {
        if (!((ITrainingBot) bot).isCombat()) return false;
        boolean hyperAggressive = isHyperAggressiveRank();
        Vec3 targetPos = target.position();
        Vec3 botPos = bot.position();

        double yDiff = botPos.y - targetPos.y;
        double optimalDistance = 5.0;

        if (yDiff > 2.0 && (!hyperAggressive || yDiff > 4.0)) {
            if (enderpearlController.canUseEnderpearl() && repositionTimer <= 0) {
                enderpearlController.tryUseEnderpearl(target, IPearlStrategyCalculator.PearlStrategy.REPOSITION_LOW);
                repositionTimer = 100;
                return true;
            }
        }

        if (distance > (hyperAggressive ? 14.0 : 10.0)
                && enderpearlController.canUseEnderpearl()
                && repositionTimer <= 0) {
            enderpearlController.tryUseEnderpearl(target, IPearlStrategyCalculator.PearlStrategy.AGGRESSIVE_CLOSE);
            repositionTimer = 100;
            return true;
        }

        if (distance < 3.0) {
            movementController.moveAwayFrom(target, optimalDistance);
            maybeBoostMeleeTempo(hyperAggressive);
            attackController.handleAttack(target);
        } else if (distance > 8.0) {
            movementController.moveTowards(target, optimalDistance);
        } else {
            Vec3 strafeDirection = getStrafeDirection(target);
            Vec3 newPos = botPos.add(strafeDirection.scale(1.5));
            movementController.moveToPosition(newPos);
            if (distance <= 3.8) {
                maybeBoostMeleeTempo(hyperAggressive);
                attackController.handleAttack(target);
            }
        }
        repositionTimer--;

        return true;
    }

    @Override
    public boolean executeAnchorSetup(Player target, double distance) {
        if (!((ITrainingBot) bot).isCombat()) return false;

        if (!rapvpController.isActive()) {
            rapvpController.enable(target);
        }

        double targetDistance = 4.0;
        moveToTarget(target, targetDistance);

        return true;
    }

    @Override
    public boolean executeCrystalSetup(Player target, double distance) {
        if (!((ITrainingBot) bot).isCombat()) return false;

        movementController.forceMovementPattern(MovementPattern.CRYSTAL_SPAM);
        movementController.moveToTarget(target, 4.6);

        boolean hyperAggressive = isHyperAggressiveRank();
        double yDiff = bot.position().y - target.position().y;
        if (hyperAggressive
                && enderpearlController.canUseEnderpearl()
                && yDiff > 4.0
                && distance > 10.5
                && !rapvpController.isActive()) {
            enderpearlController.tryUseEnderpearl(target, IPearlStrategyCalculator.PearlStrategy.REPOSITION_LOW);
        }

        return true;
    }

    @Override
    public boolean executeRetreating(Player target, double distance) {
        if (!((ITrainingBot) bot).isCombat()) return false;
        boolean hyperAggressive = isHyperAggressiveRank();

        if (distance <= 3.2) {
            maybeBoostMeleeTempo(hyperAggressive);
            attackController.handleAttack(target);
        }

        if (!hyperAggressive && enderpearlController.canUseEnderpearl()) {
            enderpearlController.tryUseEnderpearl(target);
            return true;
        }

        if (hyperAggressive) {
            moveToTarget(target, 4.0);
            if (distance <= 4.0) {
                attackController.handleAttack(target);
            }
        } else {
            movementController.moveAwayFrom(target, 10.0);
        }
        return true;
    }

    @Override
    public Vec3 getStrafeDirection(Player target) {
        Vec3 toTarget = target.position().subtract(bot.position()).normalize();
        return new Vec3(-toTarget.z, 0, toTarget.x);
    }

    @Override
    public void moveToTarget(Player target, double targetDistance) {
        double currentDistance = bot.distanceTo(target);

        if (Math.abs(currentDistance - targetDistance) <= 0.3) {
            movementController.maintainDistance(target, targetDistance);
        } else if (currentDistance < targetDistance) {
            movementController.moveAwayFrom(target, targetDistance);
        } else {
            movementController.moveTowards(target, targetDistance);
        }
    }

    @Override
    public void basicFollowBehavior(Player target) {
        double targetDistance = 2.0;

        moveToTarget(target, targetDistance);
        rotationController.updateRotation(target);
    }

    public void updateAggressionCooldown(int value) {
        this.aggressionCooldown = value;
    }

    public void updateRepositionTimer(int value) {
        this.repositionTimer = value;
    }

    private boolean isHyperAggressiveRank() {
        BotRank rank = cpvpController.getRank();
        return rank == BotRank.GOD || rank == BotRank.HARD;
    }

    private void maybeBoostMeleeTempo(boolean hyperAggressive) {
        if (!hyperAggressive) {
            return;
        }
        if (random.nextDouble() < 0.35D) {
            return;
        }
        if (attackController.getAttackCooldown() > 2) {
            attackController.setAttackCooldown(2);
        }
    }

    private boolean shouldForceAnchorBreakout(Player target, double distance) {
        if (rapvpController.isActive()) {
            return false;
        }

        if (!inventoryController.hasItem(net.minecraft.world.item.Items.RESPAWN_ANCHOR)
                || !inventoryController.hasItem(net.minecraft.world.item.Items.GLOWSTONE)) {
            return false;
        }

        long timeSinceDamage = System.currentTimeMillis() - combatDataManager.getLastDamageTime();
        int comboCount = combatDataManager.getConsecutiveDamageCount();
        boolean underPressure = comboCount >= 2 && timeSinceDamage < 2200L;
        if (!underPressure) {
            return false;
        }

        if (distance < 1.4D || distance > 6.6D) {
            return false;
        }

        double yDiff = Math.abs(bot.position().y - target.position().y);
        if (yDiff > 2.3D) {
            return false;
        }

        float healthPercent = bot.getHealth() / bot.getMaxHealth();
        return healthPercent > 0.14f;
    }

    private boolean isManagedBotTarget(Player target) {
        return target instanceof ITrainingBot;
    }

    @Override
    public void executeMeleeCombat(LivingEntity target) {
        double distance = bot.distanceTo(target);
        if (!inventoryController.isHoldingSword()) {
            inventoryController.switchToSword();
        }

        if (distance <= 3.5D) {
            maybeBoostMeleeTempo(isHyperAggressiveRank());
            attackController.handleAttack(target);
        }

        if (distance > 1.45D) {
            moveDirectlyIntoMelee(target, 1.25D);
        } else {
            movementController.stopMovement();
        }

        if (distance <= 3.5D) {
            maybeBoostMeleeTempo(isHyperAggressiveRank());
            attackController.handleAttack(target);
        }
    }

    private void moveDirectlyIntoMelee(LivingEntity target, double targetDistance) {
        Vec3 botPos = bot.position();
        Vec3 targetPos = target.position();
        Vec3 toTarget = targetPos.subtract(botPos);
        double horizontalDistance = Math.hypot(toTarget.x, toTarget.z);
        if (horizontalDistance <= targetDistance) {
            movementController.stopMovement();
            return;
        }

        Vec3 horizontalDirection = new Vec3(toTarget.x / horizontalDistance, 0, toTarget.z / horizontalDistance);
        Vec3 destination = targetPos.subtract(horizontalDirection.scale(targetDistance));
        movementController.moveToPosition(destination);
    }
}
