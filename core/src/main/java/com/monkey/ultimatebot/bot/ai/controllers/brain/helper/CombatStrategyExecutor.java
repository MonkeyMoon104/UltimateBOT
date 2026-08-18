package com.monkey.ultimatebot.bot.ai.controllers.brain.helper;

import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.bot.ai.controllers.attack.BotAttackController;
import com.monkey.ultimatebot.bot.ai.controllers.brain.helper.inter.ICombatDataManager;
import com.monkey.ultimatebot.bot.ai.controllers.brain.helper.inter.ICombatStateManager;
import com.monkey.ultimatebot.bot.ai.controllers.brain.helper.inter.ICombatStrategyExecutor;
import com.monkey.ultimatebot.bot.ai.controllers.cpvp.BotCPVPController;
import com.monkey.ultimatebot.bot.ai.controllers.enderpearl.BotEnderpearlController;
import com.monkey.ultimatebot.bot.ai.controllers.enderpearl.helper.inter.IPearlStrategyCalculator;
import com.monkey.ultimatebot.bot.ai.controllers.inventory.BotInventoryController;
import com.monkey.ultimatebot.bot.ai.controllers.movement.BotMovementController;
import com.monkey.ultimatebot.bot.ai.controllers.movement.helper.MovementPattern;
import com.monkey.ultimatebot.bot.ai.controllers.rapvp.BotRAPVPController;
import com.monkey.ultimatebot.bot.ai.controllers.rotation.BotRotationController;
import com.monkey.ultimatebot.bot.ai.difficulty.DifficultyLevel;
import com.monkey.ultimatebot.access.combat.CombatSwordAccess;
import com.monkey.ultimatebot.utils.material.MaterialCatalog;
import java.util.Random;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

@SuppressWarnings("NullAway")
public class CombatStrategyExecutor implements ICombatStrategyExecutor {

    private final ITrainingBot bot;
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
            ITrainingBot bot,
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
        if (!bot.isCombat()) {
            basicFollowBehavior(target);
            return;
        }
        double distance = bot.distanceTo(target);

        if (isManagedBotTarget(target)) {
            executeMeleeCombat(target);
            return;
        }

        boolean actionExecuted = false;
        switch (combatStateManager.getCurrentState()) {
            case AGGRESSIVE:
                actionExecuted = executeAggressive(target, distance);
                break;
            case DEFENSIVE:
                actionExecuted = executeDefensive(target, distance);
                break;
            case REPOSITIONING:
                actionExecuted = executeRepositioning(target, distance);
                break;
            case ANCHOR_SETUP:
                actionExecuted = executeAnchorSetup(target, distance);
                break;
            case CRYSTAL_SETUP:
                actionExecuted = executeCrystalSetup(target, distance);
                break;
            case RETREATING:
                actionExecuted = executeRetreating(target, distance);
                break;
            default:
                actionExecuted = false;
                break;
        }

        if (!actionExecuted) {
            moveToTarget(target, 3.0);
            if (distance <= 3.5 && bot.isCombat()) {
                handleCombatAttack(target);
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
        boolean hyperAggressive = isHyperAggressiveDifficulty();

        if (distance <= 3.5 && bot.isCombat()) {
            maybeBoostMeleeTempo(hyperAggressive);
            handleCombatAttack(target);
        }
        if (shouldUseAggressivePearl(target, distance, hyperAggressive)) {
            return true;
        }

        executeAggressiveMovement(target, distance);
        maybeSwitchToSwordForAggressiveMelee(distance);

        if (distance <= 3.5 && bot.isCombat()) {
            maybeBoostMeleeTempo(hyperAggressive);
            handleCombatAttack(target);
        }

        return true;
    }

    @Override
    public boolean executeDefensive(Player target, double distance) {
        if (!bot.isCombat()) return false;
        boolean hyperAggressive = isHyperAggressiveDifficulty();

        if (tryExecuteDefensiveAnchorBreakout(target, distance, hyperAggressive)) {
            return true;
        }

        if (distance <= 3.2) {
            handleCombatAttack(target);
        }

        if (tryExecuteDefensivePearl(target, distance, hyperAggressive)) {
            return true;
        }

        executeDefensivePositioning(target, distance, hyperAggressive);
        executeDefensiveHotbarSwitch(target);

        if (distance <= 3.5 && bot.isCombat()) {
            maybeBoostMeleeTempo(hyperAggressive);
            handleCombatAttack(target);
        }

        return true;
    }

    @Override
    public boolean executeRepositioning(Player target, double distance) {
        if (!bot.isCombat()) return false;
        boolean hyperAggressive = isHyperAggressiveDifficulty();
        if (tryExecuteRepositioningPearl(target, distance, hyperAggressive)) {
            return true;
        }

        executeRepositioningMovement(target, distance, hyperAggressive);
        repositionTimer--;

        return true;
    }

    @Override
    public boolean executeAnchorSetup(Player target, double distance) {
        if (!bot.isCombat()) return false;

        if (!rapvpController.isActive()) {
            rapvpController.enable(target);
        }

        double targetDistance = 4.0;
        moveToTarget(target, targetDistance);

        return true;
    }

    @Override
    public boolean executeCrystalSetup(Player target, double distance) {
        if (!bot.isCombat()) return false;

        movementController.forceMovementPattern(MovementPattern.CRYSTAL_SPAM);
        movementController.moveToTarget(target, 4.6);

        boolean hyperAggressive = isHyperAggressiveDifficulty();
        double yDiff = bot.getLocation().getY() - target.getLocation().getY();
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
        if (!bot.isCombat()) return false;
        boolean hyperAggressive = isHyperAggressiveDifficulty();

        if (distance <= 3.2) {
            maybeBoostMeleeTempo(hyperAggressive);
            handleCombatAttack(target);
        }

        if (!hyperAggressive && enderpearlController.canUseEnderpearl()) {
            enderpearlController.tryUseEnderpearl(target);
            return true;
        }

        if (hyperAggressive) {
            moveToTarget(target, 4.0);
            if (distance <= 4.0) {
                handleCombatAttack(target);
            }
        } else {
            movementController.moveAwayFrom(target, 10.0);
        }
        return true;
    }

    @Override
    public Vector getStrafeDirection(Player target) {
        Vector toTarget = target.getLocation().toVector().subtract(bot.bukkitPosition());
        toTarget.setY(0.0D);
        if (toTarget.lengthSquared() < 1.0E-6D) {
            return new Vector(1.0D, 0.0D, 0.0D);
        }
        toTarget.normalize();
        return new Vector(-toTarget.getZ(), 0, toTarget.getX());
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

    private boolean isHyperAggressiveDifficulty() {
        DifficultyLevel difficulty = cpvpController.getDifficulty();
        return difficulty == DifficultyLevel.GOD || difficulty == DifficultyLevel.HARD;
    }

    private void handleCombatAttack(LivingEntity target) {
        if (CombatSwordAccess.mustHoldKitSwordToMelee()) {
            if (cpvpController.isDoingCrystalAction() || rapvpController.isActive() || isHoldingCrystalCombatItem()) {
                return;
            }
        }
        attackController.handleAttack(target);
    }

    private boolean shouldKeepCrystalHotbar() {
        return CombatSwordAccess.mustHoldKitSwordToMelee()
                && cpvpController.isEnabled()
                && (cpvpController.isDoingCrystalAction() || isHoldingCrystalCombatItem());
    }

    private boolean isHoldingCrystalCombatItem() {
        return inventoryController.isHoldingObsidian()
                || inventoryController.isHoldingCrystal()
                || inventoryController.isHoldingAnchor()
                || inventoryController.isHoldingGlow();
    }

    private boolean shouldUseAggressivePearl(Player target, double distance, boolean hyperAggressive) {
        double yDiff = bot.getLocation().getY() - target.getLocation().getY();

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

        return false;
    }

    private void executeAggressiveMovement(Player target, double distance) {
        double targetDistance = 2.5;
        if (CombatSwordAccess.mustHoldKitSwordToMelee() && cpvpController.isEnabled()) {
            moveToTarget(target, crystalMeleeHoldDistance());
            return;
        }

        if (distance <= 1.8) {
            Vector strafeDirection = getStrafeDirection(target);
            movementController.moveToPosition(bot.bukkitPosition().add(strafeDirection.multiply(1.9D)));
            return;
        }

        moveToTarget(target, targetDistance);
    }

    private void maybeSwitchToSwordForAggressiveMelee(double distance) {
        if (!inventoryController.isHoldingSword() && distance <= 4.0 && !shouldKeepCrystalHotbar()) {
            inventoryController.switchToSword();
        }
    }

    private double crystalMeleeHoldDistance() {
        double min = cpvpController.getConfig().getMinCrystalDistance();
        double max = cpvpController.getConfig().getMaxCrystalDistance();
        return Math.min(max - 0.4D, Math.max(4.0D, min + 1.8D));
    }

    private boolean tryExecuteDefensiveAnchorBreakout(Player target, double distance, boolean hyperAggressive) {
        if (!shouldForceAnchorBreakout(target, distance)) {
            return false;
        }

        if (!rapvpController.isActive()) {
            rapvpController.enable(target);
        }

        double anchorBreakoutDistance = Math.max(3.8D, Math.min(4.8D, distance + 0.6D));
        moveToTarget(target, anchorBreakoutDistance);
        if (distance <= 3.4D) {
            maybeBoostMeleeTempo(hyperAggressive);
            handleCombatAttack(target);
        }
        return true;
    }

    private boolean tryExecuteDefensivePearl(Player target, double distance, boolean hyperAggressive) {
        if (!hyperAggressive
                && (enderpearlController.wasRecentlyDamaged() || combatDataManager.getConsecutiveDamageCount() >= 2)) {
            if (enderpearlController.canUseEnderpearl()) {
                enderpearlController.tryUseEnderpearl(target);
                return true;
            }
        } else if (hyperAggressive
                && bot.healthValue() / bot.maxHealthValue() < 0.12f
                && distance > 8.0
                && enderpearlController.canUseEnderpearl()) {
            enderpearlController.tryUseEnderpearl(target, IPearlStrategyCalculator.PearlStrategy.REPOSITION_LOW);
            return true;
        }
        return false;
    }

    private void executeDefensivePositioning(Player target, double distance, boolean hyperAggressive) {
        double targetDistance = hyperAggressive
                ? Math.min(6.0, Math.max(3.8, distance + 0.5))
                : Math.min(8.0, Math.max(5.0, distance + 1.5));
        moveToTarget(target, targetDistance);
    }

    private void executeDefensiveHotbarSwitch(Player target) {
        int botY = bot.getLocation().getBlockY();
        int targetY = target.getLocation().getBlockY();
        int yDiff = targetY - botY;

        if (yDiff < 2) {
            if (!inventoryController.isHoldingAnchor() && hasAnchor()) {
                inventoryController.switchToAnchor();
            }
            return;
        }

        if (!inventoryController.isHoldingCrystal() && cpvpController.canPlaceCrystal()) {
            inventoryController.switchToCrystal();
        }
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

    private boolean tryExecuteRepositioningPearl(Player target, double distance, boolean hyperAggressive) {
        Vector targetPos = target.getLocation().toVector();
        Vector botPos = bot.bukkitPosition();
        double yDiff = botPos.getY() - targetPos.getY();

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
        return false;
    }

    private void executeRepositioningMovement(Player target, double distance, boolean hyperAggressive) {
        Vector botPos = bot.bukkitPosition();
        double optimalDistance = 5.0;
        if (distance < 3.0) {
            movementController.moveAwayFrom(target, optimalDistance);
            maybeBoostMeleeTempo(hyperAggressive);
            handleCombatAttack(target);
            return;
        }

        if (distance > 8.0) {
            movementController.moveTowards(target, optimalDistance);
            return;
        }

        Vector strafeDirection = getStrafeDirection(target);
        Vector newPos = botPos.add(strafeDirection.multiply(1.5D));
        movementController.moveToPosition(newPos);
        if (distance <= 3.8) {
            maybeBoostMeleeTempo(hyperAggressive);
            handleCombatAttack(target);
        }
    }

    private boolean shouldForceAnchorBreakout(Player target, double distance) {
        if (rapvpController.isActive()) {
            return false;
        }

        if (!hasAnchor() || !inventoryController.hasItem(Material.GLOWSTONE)) {
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

        double yDiff = Math.abs(bot.getLocation().getY() - target.getLocation().getY());
        if (yDiff > 2.3D) {
            return false;
        }

        float healthPercent = (float) (bot.healthValue() / bot.maxHealthValue());
        return healthPercent > 0.14f;
    }

    private boolean isManagedBotTarget(Player target) {
        return target != null && target.getUniqueId().equals(bot.getUniqueId());
    }

    @Override
    public void executeMeleeCombat(LivingEntity target) {
        double distance = bot.distanceTo(target);
        if (!inventoryController.isHoldingSword()) {
            inventoryController.switchToSword();
        }

        if (distance <= 3.5D) {
            maybeBoostMeleeTempo(isHyperAggressiveDifficulty());
            handleCombatAttack(target);
        }

        if (distance > 1.45D) {
            moveDirectlyIntoMelee(target, 1.25D);
        } else {
            movementController.stopMovement();
        }

        if (distance <= 3.5D) {
            maybeBoostMeleeTempo(isHyperAggressiveDifficulty());
            handleCombatAttack(target);
        }
    }

    private void moveDirectlyIntoMelee(LivingEntity target, double targetDistance) {
        Vector botPos = bot.bukkitPosition();
        Vector targetPos = target.getLocation().toVector();
        Vector toTarget = targetPos.clone().subtract(botPos);
        toTarget.setY(0.0D);
        double horizontalDistance = toTarget.length();
        if (horizontalDistance <= targetDistance) {
            movementController.stopMovement();
            return;
        }

        Vector horizontalDirection = toTarget.multiply(1.0D / horizontalDistance);
        Vector destination = targetPos.subtract(horizontalDirection.multiply(targetDistance));
        movementController.moveToPosition(destination);
    }

    private boolean hasAnchor() {
        Material anchor = MaterialCatalog.optional("RESPAWN_ANCHOR", Material.AIR);
        return anchor != Material.AIR && inventoryController.hasItem(anchor);
    }
}
