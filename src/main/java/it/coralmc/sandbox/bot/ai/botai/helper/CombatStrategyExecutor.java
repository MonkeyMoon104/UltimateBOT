package it.coralmc.sandbox.bot.ai.botai.helper;

import it.coralmc.sandbox.bot.ai.TrainingBot;
import it.coralmc.sandbox.bot.ai.botai.helper.inter.ICombatDataManager;
import it.coralmc.sandbox.bot.ai.botai.helper.inter.ICombatStateManager;
import it.coralmc.sandbox.bot.ai.botai.helper.inter.ICombatStrategyExecutor;
import it.coralmc.sandbox.bot.ai.controllers.attack.BotAttackController;
import it.coralmc.sandbox.bot.ai.controllers.cpvp.BotCPVPController;
import it.coralmc.sandbox.bot.ai.controllers.enderpearl.BotEnderpearlController;
import it.coralmc.sandbox.bot.ai.controllers.enderpearl.helper.inter.IPearlStrategyCalculator;
import it.coralmc.sandbox.bot.ai.controllers.inventory.BotInventoryController;
import it.coralmc.sandbox.bot.ai.controllers.movement.BotMovementController;
import it.coralmc.sandbox.bot.ai.controllers.movement.helper.MovementPattern;
import it.coralmc.sandbox.bot.ai.controllers.rapvp.BotRAPVPController;
import it.coralmc.sandbox.bot.ai.controllers.rotation.BotRotationController;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

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

    public CombatStrategyExecutor(Player bot, BotMovementController movementController,
                                  BotRotationController rotationController, BotAttackController attackController,
                                  BotInventoryController inventoryController, BotEnderpearlController enderpearlController,
                                  BotCPVPController cpvpController, BotRAPVPController rapvpController,
                                  ICombatStateManager combatStateManager, ICombatDataManager combatDataManager) {
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
        if (!((TrainingBot) bot).isCombat()) {
            basicFollowBehavior(target);
            return;
        }
        double distance = bot.distanceTo(target);

        boolean actionExecuted = false;

        switch (combatStateManager.getCurrentState()) {
            case AGGRESSIVE -> actionExecuted = executeAggressive(target, distance);
            case DEFENSIVE -> actionExecuted = executeDefensive(target, distance);
            case REPOSITIONING -> actionExecuted = executeRepositioning(target, distance);
            case ANCHOR_SETUP -> actionExecuted = executeAnchorSetup(target, distance);
            case CRYSTAL_SETUP -> actionExecuted = executeCrystalSetup(target, distance);
            case RETREATING -> actionExecuted = executeRetreating(target, distance);
        }

        if (!actionExecuted) {
            moveToTarget(target, 3.0);
            actionExecuted = true;
        }

        if (!enderpearlController.isThrowingPearl() &&
                !cpvpController.isDoingCrystalAction() &&
                !rapvpController.isActive()) {
            rotationController.updateRotation(target);
        }

        if (aggressionCooldown > 0) aggressionCooldown--;
        if (repositionTimer > 0) repositionTimer--;
    }

    @Override
    public boolean executeAggressive(Player target, double distance) {
        boolean actionTaken = false;

        if (distance <= 3.5 && ((TrainingBot) bot).isCombat() && ((TrainingBot) bot).isFollow()) {
            attackController.handleAttack(target);
            actionTaken = true;
        }
        double targetDistance = 2.5;

        if (distance > 10.0 && enderpearlController.canUseEnderpearl()) {
            enderpearlController.tryUseEnderpearl(target, IPearlStrategyCalculator.PearlStrategy.AGGRESSIVE_CLOSE);
            return true;
        }

        if (distance > 6.0 && enderpearlController.canUseEnderpearl()) {
            if (enderpearlController.wasRecentlyDamaged() || distance > 9.0) {
                enderpearlController.tryUseEnderpearl(target);
                return true;
            }
        }

        moveToTarget(target, targetDistance);
        actionTaken = true;

        if (!inventoryController.isHoldingSword() && distance <= 4.0) {
            inventoryController.switchToSword();
        }

        if (distance <= 3.5 && ((TrainingBot) bot).isFollow()) {
            attackController.handleAttack(target);
            actionTaken = true;
        }

        return actionTaken;
    }

    @Override
    public boolean executeDefensive(Player target, double distance) {
        if (!((TrainingBot) bot).isCombat()) return false;
        boolean actionTaken = false;

        if (enderpearlController.wasRecentlyDamaged() || combatDataManager.getConsecutiveDamageCount() >= 2) {
            if (enderpearlController.canUseEnderpearl()) {
                enderpearlController.tryUseEnderpearl(target);
                return true;
            }
        }

        double targetDistance = Math.min(8.0, Math.max(5.0, distance + 1.5));
        moveToTarget(target, targetDistance);
        actionTaken = true;

        if (!inventoryController.isHoldingCrystal() && cpvpController.canPlaceCrystal()) {
            inventoryController.switchToCrystal();
        }

        return actionTaken;
    }

    @Override
    public boolean executeRepositioning(Player target, double distance) {
        if (!((TrainingBot) bot).isCombat()) return false;
        Vec3 targetPos = target.position();
        Vec3 botPos = bot.position();

        double yDiff = botPos.y - targetPos.y;
        double optimalDistance = 5.0;
        boolean actionTaken = false;

        if (yDiff > 2.0) {
            if (enderpearlController.canUseEnderpearl() && repositionTimer <= 0) {
                enderpearlController.tryUseEnderpearl(target, IPearlStrategyCalculator.PearlStrategy.REPOSITION_LOW);
                repositionTimer = 100;
                return true;
            }
        }

        if (distance > 10.0 && enderpearlController.canUseEnderpearl() && repositionTimer <= 0) {
            enderpearlController.tryUseEnderpearl(target, IPearlStrategyCalculator.PearlStrategy.AGGRESSIVE_CLOSE);
            repositionTimer = 100;
            return true;
        }

        if (distance < 3.0) {
            movementController.moveAwayFrom(target, optimalDistance);
        } else if (distance > 8.0) {
            movementController.moveTowards(target, optimalDistance);
        } else {
            Vec3 strafeDirection = getStrafeDirection(target);
            Vec3 newPos = botPos.add(strafeDirection.scale(1.5));
            movementController.moveToPosition(newPos);
        }
        actionTaken = true;

        repositionTimer--;
        if (repositionTimer <= 0) {
        }

        return actionTaken;
    }

    @Override
    public boolean executeAnchorSetup(Player target, double distance) {
        if (!((TrainingBot) bot).isCombat()) return false;
        if (!rapvpController.isActive()) {
            rapvpController.enable(target);
        }

        double targetDistance = 4.0;
        moveToTarget(target, targetDistance);

        if (!rapvpController.isActive() &&
                System.currentTimeMillis() - 0 > 2000) {
        }

        return true;
    }

    @Override
    public boolean executeCrystalSetup(Player target, double distance) {
        if (!((TrainingBot) bot).isCombat()) return false;
        movementController.forceMovementPattern(MovementPattern.CRYSTAL_SPAM);
        movementController.moveToTarget(target, 5.0);

        if (cpvpController.canPlaceCrystal()) {
            cpvpController.tryPlaceOptimalCrystals(target);
        }

        if (enderpearlController.canUseEnderpearl() &&
                bot.position().y > target.position().y - 1 &&
                random.nextDouble() < 0.3) {
            enderpearlController.tryUseEnderpearl(target);
        }

        return true;
    }

    @Override
    public boolean executeRetreating(Player target, double distance) {
        if (!((TrainingBot) bot).isCombat()) return false;
        boolean actionTaken = false;

        if (enderpearlController.canUseEnderpearl()) {
            enderpearlController.tryUseEnderpearl(target);
            return true;
        }

        movementController.moveAwayFrom(target, 10.0);
        actionTaken = true;

        if (cpvpController.canPlaceObsidian() && random.nextDouble() < 0.3) {
        }

        if (bot.getHealth() / bot.getMaxHealth() > 0.4f) {
        }

        return actionTaken;
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
}