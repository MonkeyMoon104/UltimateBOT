package it.coralmc.sandbox.bot.ai;

import it.coralmc.sandbox.SandboxTraining;
import it.coralmc.sandbox.bot.ai.controllers.*;
import it.coralmc.sandbox.bot.ai.controllers.cpvp.BotCPVPController;
import it.coralmc.sandbox.bot.ai.controllers.movement.BotMovementController;
import it.coralmc.sandbox.bot.ai.controllers.movement.helper.MovementPattern;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.bukkit.craftbukkit.entity.CraftPlayer;

import java.util.Random;

public class BotAI {

    public enum CombatState {
        AGGRESSIVE,
        DEFENSIVE,
        REPOSITIONING,
        ANCHOR_SETUP,
        CRYSTAL_SETUP,
        RETREATING
    }

    private final Player bot;
    private final Level level;
    private final BotMovementController movementController;
    private final BotRotationController rotationController;
    private final BotTotemController totemController;
    private final BotAttackController attackController;
    private final BotInventoryController inventoryController;
    private final BotEnderpearlController enderpearlController;
    private final BotCPVPController cpvpController;
    private final BotRAPVPController rapvpController;

    private CombatState currentState = CombatState.AGGRESSIVE;
    private long lastStateChange = 0;
    private static final long MIN_STATE_DURATION = 500;

    private float lastKnownHealth;
    private int consecutiveDamageCount = 0;
    private long lastDamageTime = 0;
    private static final long DAMAGE_COMBO_WINDOW = 2000;

    private Vec3 lastTargetPosition;
    private Vec3 targetVelocity = Vec3.ZERO;
    private long lastPositionUpdate = 0;

    private int aggressionCooldown = 0;
    private int repositionTimer = 0;
    private long lastAnchorAttempt = 0;
    private static final long ANCHOR_ATTEMPT_COOLDOWN = 8000;

    private long lastActionTime = 0;
    private Vec3 lastBotPosition;
    private int stuckCounter = 0;
    private static final long MAX_STUCK_TIME = 1500;
    private static final double MIN_MOVEMENT_THRESHOLD = 0.1;

    private long lastPathfindingAttempt = 0;
    private static final long PATHFINDING_ATTEMPT_COOLDOWN = 3000;
    private boolean usingPathfinding = false;

    private final Random random = new Random();

    public BotAI(Player bot, SandboxTraining plugin) {
        this.bot = bot;
        this.level = bot.level();
        this.lastKnownHealth = bot.getHealth();
        this.lastBotPosition = bot.position();

        this.movementController = new BotMovementController(bot, level);
        this.rotationController = new BotRotationController(bot);
        this.totemController = new BotTotemController(bot, plugin);
        this.attackController = new BotAttackController(bot);
        this.inventoryController = new BotInventoryController(bot);
        this.enderpearlController = new BotEnderpearlController(bot, inventoryController, rotationController);
        this.cpvpController = new BotCPVPController(bot, inventoryController, rotationController);
        this.rapvpController = new BotRAPVPController(bot, inventoryController, rotationController, cpvpController, enderpearlController);
    }

    public void tick(org.bukkit.entity.Player targetBukkitPlayer) {
        if (targetBukkitPlayer == null || targetBukkitPlayer.isDead()) return;

        Player target = ((CraftPlayer) targetBukkitPlayer).getHandle();

        updateCombatData(target);
        if (usingPathfinding && movementController.hasActivePath()) {
            if (movementController.followPath()) {
                rotationController.lookAt(
                        movementController.getCurrentPathPoint().x,
                        movementController.getCurrentPathPoint().y,
                        movementController.getCurrentPathPoint().z
                );
            } else {
                usingPathfinding = false;
            }
        } else {
            checkForStuck(target);

            inventoryController.tick();
            enderpearlController.tick();

            if (((TrainingBot) bot).isCombat()) {
                updateCombatState(target);
                executeCombatStrategy(target);
            } else {
                basicFollowBehavior(target);
            }
        }

        lastBotPosition = bot.position();
        lastActionTime = System.currentTimeMillis();
    }
    private void checkForStuck(Player target) {
        long currentTime = System.currentTimeMillis();
        Vec3 currentPos = bot.position();

        double movementDistance = currentPos.distanceTo(lastBotPosition);

        if (movementDistance < MIN_MOVEMENT_THRESHOLD) {
            stuckCounter++;
        } else {
            stuckCounter = 0;
            usingPathfinding = false;
            movementController.clearPath();
        }

        if (stuckCounter > 30 || (currentTime - lastActionTime > MAX_STUCK_TIME)) {
            if (!usingPathfinding || movementController.shouldRecalculatePath()) {
                attemptPathfindingOrPearl(target);
            } else {
                movementController.followPath();
            }

            stuckCounter = 0;
            lastActionTime = currentTime;
        }
    }

    private void attemptPathfindingOrPearl(Player target) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastPathfindingAttempt < PATHFINDING_ATTEMPT_COOLDOWN) {
            return;
        }

        lastPathfindingAttempt = currentTime;

        if (movementController.calculatePathTo(target.position())) {
            usingPathfinding = true;
            movementController.followPath();
            return;
        }

        if (enderpearlController.canUseEnderpearl() &&
                bot.distanceTo(target) > 4.0 &&
                hasObstacleBetween(bot.position(), target.position())) {

            Vec3 pearlTarget = calculatePearlTargetAroundPlayer(target);
            if (pearlTarget != null) {
                enderpearlController.tryUseEnderpearlToPosition(pearlTarget);
                usingPathfinding = false;
                return;
            }
        }

        forceUnstuck(target);
    }

    private boolean hasObstacleBetween(Vec3 start, Vec3 end) {
        Vec3 direction = end.subtract(start).normalize();
        double distance = start.distanceTo(end);
        int steps = (int) (distance * 2);

        for (int i = 1; i < steps; i++) {
            Vec3 checkPos = start.add(direction.scale(i * 0.5));
            BlockPos blockPos = BlockPos.containing(checkPos);

            if (!level.getBlockState(blockPos).isAir() ||
                    !level.getBlockState(blockPos.above()).isAir()) {
                return true;
            }
        }

        return false;
    }

    private Vec3 calculatePearlTargetAroundPlayer(Player target) {
        Vec3 targetPos = target.position();
        Random rand = new Random();

        for (int i = 0; i < 8; i++) {
            double angle = rand.nextDouble() * 2 * Math.PI;
            double radius = 3 + rand.nextDouble() * 4;

            double x = targetPos.x + Math.cos(angle) * radius;
            double z = targetPos.z + Math.sin(angle) * radius;
            double y = targetPos.y;

            Vec3 potentialTarget = new Vec3(x, y, z);
            BlockPos blockPos = BlockPos.containing(potentialTarget);

            if (isSafeLandingSpot(blockPos) &&
                    !hasObstacleBetween(bot.position(), potentialTarget)) {
                return potentialTarget;
            }
        }

        return null;
    }

    private boolean isSafeLandingSpot(BlockPos pos) {
        return !level.getBlockState(pos.below()).isAir() &&
                level.getBlockState(pos).isAir() &&
                level.getBlockState(pos.above()).isAir();
    }


    private void forceUnstuck(Player target) {
        double distance = bot.distanceTo(target);

        if (distance <= 1.0) {
            movementController.moveAwayFrom(target, 3.0);
        } else if (distance >= 10.0) {
            movementController.moveTowards(target, 3.0);
        } else {
            Vec3 strafeDirection = getStrafeDirection(target);
            Vec3 botPos = bot.position();
            Vec3 newPos = botPos.add(strafeDirection.scale(2.0));
            movementController.moveToPosition(newPos);
        }

        if (System.currentTimeMillis() - lastStateChange > 3000) {
            if (currentState == CombatState.DEFENSIVE || currentState == CombatState.REPOSITIONING) {
                currentState = CombatState.AGGRESSIVE;
                lastStateChange = System.currentTimeMillis();
            }
        }
    }

    private void updateCombatData(Player target) {
        float currentHealth = bot.getHealth();
        long currentTime = System.currentTimeMillis();

        if (currentHealth < lastKnownHealth) {
            consecutiveDamageCount++;
            lastDamageTime = currentTime;
            enderpearlController.onDamageReceived();
        } else if (currentTime - lastDamageTime > DAMAGE_COMBO_WINDOW) {
            consecutiveDamageCount = Math.max(0, consecutiveDamageCount - 1);
        }

        lastKnownHealth = currentHealth;

        Vec3 currentTargetPos = target.position();
        if (lastTargetPosition != null && currentTime - lastPositionUpdate > 50) {
            targetVelocity = currentTargetPos.subtract(lastTargetPosition).scale(20.0 / (currentTime - lastPositionUpdate) * 1000);
        }
        lastTargetPosition = currentTargetPos;
        lastPositionUpdate = currentTime;

        rapvpController.tick();
        cpvpController.tick(target);
    }

    private void updateCombatState(Player target) {
        long currentTime = System.currentTimeMillis();
        double distance = bot.distanceTo(target);
        float healthPercent = bot.getHealth() / bot.getMaxHealth();

        long minDuration = (currentTime - lastDamageTime < 1000) ? 250 : MIN_STATE_DURATION;
        if (currentTime - lastStateChange < minDuration) return;

        CombatState newState = currentState;

        double yDiff = bot.position().y - target.position().y;

        if (healthPercent < 0.25f) {
            newState = CombatState.RETREATING;
        } else if (consecutiveDamageCount >= 2 && currentTime - lastDamageTime < 1500) {
            newState = CombatState.DEFENSIVE;
        }
        else if (Math.abs(yDiff) <= 3.0 && shouldAttemptAnchor(target, currentTime)) {
            newState = CombatState.ANCHOR_SETUP;
        }
        else if (yDiff < -1.0 && cpvpController.canPlaceCrystal()) {
            newState = CombatState.CRYSTAL_SETUP;
        }
        else if (shouldReposition(target, distance)) {
            newState = CombatState.REPOSITIONING;
        } else if (distance > 4.0 && distance < 12.0 && healthPercent > 0.4f) {
            newState = CombatState.CRYSTAL_SETUP;
        } else if (distance <= 4.0 && healthPercent > 0.3f) {
            newState = CombatState.AGGRESSIVE;
        } else {
            newState = CombatState.AGGRESSIVE;
        }

        if (newState != currentState) {
            currentState = newState;
            lastStateChange = currentTime;
            onStateChange(target);
        }
    }

    private void onStateChange(Player target) {
        switch (currentState) {
            case DEFENSIVE -> {
                aggressionCooldown = 20;
                repositionTimer = 40;
            }
            case ANCHOR_SETUP -> {
                rapvpController.enable(target);
                lastAnchorAttempt = System.currentTimeMillis();
            }
            case RETREATING -> {
                aggressionCooldown = 60;
            }
            case REPOSITIONING -> {
                repositionTimer = 40;
            }
        }
    }

    private void executeCombatStrategy(Player target) {
        double distance = bot.distanceTo(target);

        boolean actionExecuted = false;

        switch (currentState) {
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

    private boolean executeAggressive(Player target, double distance) {
        double targetDistance = 2.5;
        boolean actionTaken = false;

        if (distance > 10.0 && enderpearlController.canUseEnderpearl()) {
            enderpearlController.tryUseEnderpearl(target, BotEnderpearlController.PearlStrategy.AGGRESSIVE_CLOSE);
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

    private boolean executeDefensive(Player target, double distance) {
        boolean actionTaken = false;

        if (enderpearlController.wasRecentlyDamaged() || consecutiveDamageCount >= 2) {
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

    private boolean executeRepositioning(Player target, double distance) {
        Vec3 targetPos = target.position();
        Vec3 botPos = bot.position();

        double yDiff = botPos.y - targetPos.y;
        double optimalDistance = 5.0;
        boolean actionTaken = false;

        if (yDiff > 2.0) {
            if (enderpearlController.canUseEnderpearl() && repositionTimer <= 0) {
                enderpearlController.tryUseEnderpearl(target, BotEnderpearlController.PearlStrategy.REPOSITION_LOW);
                repositionTimer = 100;
                return true;
            }
        }

        if (distance > 10.0 && enderpearlController.canUseEnderpearl() && repositionTimer <= 0) {
            enderpearlController.tryUseEnderpearl(target, BotEnderpearlController.PearlStrategy.AGGRESSIVE_CLOSE);
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
            currentState = CombatState.CRYSTAL_SETUP;
            lastStateChange = System.currentTimeMillis();
        }

        return actionTaken;
    }

    private boolean executeAnchorSetup(Player target, double distance) {
        if (!rapvpController.isActive()) {
            rapvpController.enable(target);
        }

        double targetDistance = 4.0;
        moveToTarget(target, targetDistance);

        if (!rapvpController.isActive() &&
                System.currentTimeMillis() - lastStateChange > 2000) {
            currentState = CombatState.CRYSTAL_SETUP;
            lastStateChange = System.currentTimeMillis();
        }

        return true;
    }

    private boolean executeCrystalSetup(Player target, double distance) {
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

    private boolean executeRetreating(Player target, double distance) {
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
            currentState = CombatState.DEFENSIVE;
            lastStateChange = System.currentTimeMillis();
        }

        return actionTaken;
    }

    private boolean shouldAttemptAnchor(Player target, long currentTime) {
        if (currentTime - lastAnchorAttempt < ANCHOR_ATTEMPT_COOLDOWN) return false;
        if (!inventoryController.hasItem(Items.RESPAWN_ANCHOR)) return false;
        if (!inventoryController.hasItem(Items.GLOWSTONE)) return false;

        double distance = bot.distanceTo(target);

        return distance > 1.0 && distance < 8.0 && target.onGround();
    }

    private boolean shouldReposition(Player target, double distance) {
        Vec3 botPos = bot.position();
        Vec3 targetPos = target.position();
        double yDiff = botPos.y - targetPos.y;

        return (yDiff < -2 && distance > 2.0) ||
                (distance < 1.5 && consecutiveDamageCount > 0) ||
                (distance > 15.0);
    }

    private Vec3 getStrafeDirection(Player target) {
        Vec3 toTarget = target.position().subtract(bot.position()).normalize();
        return new Vec3(-toTarget.z, 0, toTarget.x);
    }

    private void moveToTarget(Player target, double targetDistance) {
        double currentDistance = bot.distanceTo(target);

        if (Math.abs(currentDistance - targetDistance) <= 0.3) {
            movementController.maintainDistance(target, targetDistance);
        } else if (currentDistance < targetDistance) {
            movementController.moveAwayFrom(target, targetDistance);
        } else {
            movementController.moveTowards(target, targetDistance);
        }
    }

    private void basicFollowBehavior(Player target) {
        double distance = bot.distanceTo(target);
        double targetDistance = 2.0;

        moveToTarget(target, targetDistance);
        rotationController.updateRotation(target);

        if (distance <= 3.0 && ((TrainingBot) bot).isFollow()) {
            if (!inventoryController.isHoldingSword()) {
                inventoryController.switchToSword();
            }
            attackController.handleAttack(target);
        }
    }

    public void manageTotem() {
        totemController.manageTotem();
    }

    public BotMovementController getMovementController() { return movementController; }
    public BotRotationController getRotationController() { return rotationController; }
    public BotTotemController getTotemController() { return totemController; }
    public BotInventoryController getInventoryController() { return inventoryController; }
    public BotEnderpearlController getEnderpearlController() { return enderpearlController; }
    public BotCPVPController getCPVPController() { return cpvpController; }
    public CombatState getCurrentState() { return currentState; }
}