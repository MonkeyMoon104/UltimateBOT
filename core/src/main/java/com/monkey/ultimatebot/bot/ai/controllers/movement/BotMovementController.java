package com.monkey.ultimatebot.bot.ai.controllers.movement;

import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.bot.ai.controllers.movement.helper.*;
import com.monkey.ultimatebot.bot.ai.controllers.movement.helper.interf.*;
import com.monkey.ultimatebot.bot.ai.controllers.movement.pathfinding.PatheticPathfinder;
import com.monkey.ultimatebot.bot.ai.controllers.movement.pathfinding.UltimateBotTraversalEnvironment;
import com.monkey.ultimatebot.config.RuntimeSettings;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;
import org.jspecify.annotations.Nullable;

public class BotMovementController {
    private final ITrainingBot bot;

    private final BlockStateValidator blockValidator;
    private final IPathfinder pathfinder;
    private final IObstacleHandler obstacleHandler;
    private final MovementExecutor movementExecutor;
    private final IMovementPatternSelector patternSelector;
    private final CombatStateManager combatStateManager;

    private long lastMovementTime = 0;
    private static final long MAX_MOVEMENT_STALL_TIME = 1000;

    public BotMovementController(ITrainingBot bot, RuntimeSettings.CacheSettings blockCacheSettings) {
        this.bot = bot;

        this.blockValidator = new BlockStateValidator(bot.getWorld(), blockCacheSettings);
        this.obstacleHandler = new ObstacleHandler(bot, blockValidator, 0.25, 0.42);
        this.movementExecutor = new MovementExecutor(bot, blockValidator, obstacleHandler);
        this.pathfinder = new PatheticPathfinder(bot, new UltimateBotTraversalEnvironment(blockValidator));
        this.patternSelector = new MovementPatternSelector(obstacleHandler);
        this.combatStateManager = new CombatStateManager();
    }

    public void reconfigureBlockStateCache(RuntimeSettings.CacheSettings settings) {
        blockValidator.reconfigure(settings);
    }

    public void moveTowards(LivingEntity target, double targetDistance) {
        if (isStuckInPlace()) {
            forceUnstick(target);
            return;
        }
        combatStateManager.updateCombatData(target);

        MovementPattern selectedPattern = patternSelector.selectOptimalPattern(
                target,
                targetDistance,
                bot.bukkitPosition(),
                target.getLocation().toVector(),
                combatStateManager.isUnderFire(),
                combatStateManager.getConsecutiveHits());

        executeMovementPattern(target, targetDistance, selectedPattern);
        lastMovementTime = System.currentTimeMillis();
    }

    public void moveToTarget(LivingEntity target, double targetDistance) {
        double currentDistance = bot.distanceTo(target);
        if (Math.abs(currentDistance - targetDistance) <= 0.3) {
            if (patternSelector.getCurrentPattern() != MovementPattern.STRAFE_CIRCLE) {
                patternSelector.setMovementPattern(MovementPattern.STRAFE_CIRCLE);
            }
            movementExecutor.executeStrafeCircle(target, targetDistance);
        } else if (currentDistance < targetDistance) {
            moveAwayFrom(target, targetDistance);
        } else {
            moveTowards(target, targetDistance);
        }
    }

    public void moveAwayFrom(LivingEntity target, double targetDistance) {
        combatStateManager.updateCombatData(target);
        if (patternSelector.getCurrentPattern() != MovementPattern.RETREAT_SPIRAL
                && patternSelector.getCurrentPattern() != MovementPattern.EVASIVE_ZIG_ZAG) {
            patternSelector.setMovementPattern(MovementPattern.RETREAT_SPIRAL);
        }
        executeRetreatMovement(target, targetDistance);
    }

    public void maintainDistance(LivingEntity target, double targetDistance) {
        if (isStuckInPlace()) {
            forceUnstick(target);
            return;
        }

        double currentDistance = bot.distanceTo(target);
        if (Math.abs(currentDistance - targetDistance) <= 0.3) {
            if (patternSelector.getCurrentPattern() != MovementPattern.STRAFE_CIRCLE) {
                patternSelector.setMovementPattern(MovementPattern.STRAFE_CIRCLE);
            }
            movementExecutor.executeStrafeCircle(target, targetDistance);
        } else if (currentDistance < targetDistance) {
            moveAwayFrom(target, targetDistance);
        } else {
            moveTowards(target, targetDistance);
        }
        lastMovementTime = System.currentTimeMillis();
    }

    public boolean calculatePathTo(Vector targetPos) {
        return pathfinder.calculatePathTo(targetPos);
    }

    public boolean followPath() {
        if (pathfinder.followPath()) {
            Vector currentPoint = pathfinder.getCurrentPathPoint();
            if (currentPoint != null) {
                movementExecutor.moveAlongPath(currentPoint);
            }
            return true;
        }
        movementExecutor.resetPathSteering();
        return false;
    }

    public boolean hasActivePath() {
        return pathfinder.hasActivePath();
    }

    public void clearPath() {
        pathfinder.clearPath();
        movementExecutor.resetPathSteering();
    }

    public boolean shouldRecalculatePath() {
        return pathfinder.shouldRecalculatePath();
    }

    public boolean shouldRecalculatePath(Vector targetPos) {
        return pathfinder.shouldRecalculatePath(targetPos);
    }

    public boolean isPathObstructed(Vector targetPos) {
        return pathfinder.isPathObstructed(targetPos);
    }

    public @Nullable Vector getCurrentPathPoint() {
        return pathfinder.getCurrentPathPoint();
    }

    public void moveToPosition(Vector targetPos) {
        movementExecutor.moveToPosition(targetPos);
    }

    public void stopMovement() {
        movementExecutor.stopMovement();
        obstacleHandler.clearDiversion();
        patternSelector.setMovementPattern(MovementPattern.DIRECT);
    }

    public void ensureMovement() {
        movementExecutor.ensureMovement();
    }

    public void setUnderFire(boolean underFire) {
        combatStateManager.setUnderFire(underFire);
        obstacleHandler.setUnderFire(underFire);
        if (underFire && patternSelector.getCurrentPattern() != MovementPattern.EVASIVE_ZIG_ZAG) {
            patternSelector.setMovementPattern(MovementPattern.EVASIVE_ZIG_ZAG);
        }
    }

    public void onDamageReceived() {
        combatStateManager.onDamageReceived();

        setUnderFire(true);

        patternSelector.setMovementPattern(MovementPattern.EVASIVE_ZIG_ZAG);

        if (pathfinder.hasActivePath() && pathfinder.shouldRecalculatePath()) {
            pathfinder.clearPath();
        }

        combatStateManager.setLastSafePosition(bot.bukkitPosition());

        ensureMovement();
        lastMovementTime = System.currentTimeMillis();
    }

    public boolean isStuckInPlace() {
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastMovementTime > MAX_MOVEMENT_STALL_TIME) {
            return true;
        }

        Vector velocity = bot.bukkitVelocity();
        return velocity.lengthSquared() < 0.01;
    }

    public void forceUnstick(@Nullable LivingEntity target) {
        resetCombatState();
        obstacleHandler.clearDiversion();

        setUnderFire(true);
        emergencyEvade();
        if (bot.isOnGround()) {
            Vector velocity = bot.bukkitVelocity();
            bot.setBukkitVelocity(new Vector(velocity.getX(), Math.max(velocity.getY(), 0.36), velocity.getZ()));
        }

        if (target != null) {
            Vector botPos = bot.bukkitPosition();
            Vector targetPos = target.getLocation().toVector();

            Vector direction = targetPos.subtract(botPos).normalize();
            Vector sideDirection = new Vector(-direction.getZ(), 0, direction.getX());

            Vector unstuckPos = botPos.clone().add(sideDirection.multiply(3.0));
            moveToPosition(unstuckPos);
        } else {
            double randomX = (Math.random() - 0.5) * 4.0;
            double randomZ = (Math.random() - 0.5) * 4.0;
            Vector randomPos = bot.bukkitPosition().add(new Vector(randomX, 0, randomZ));
            moveToPosition(randomPos);
        }

        lastMovementTime = System.currentTimeMillis();
    }

    public void forceMovementPattern(MovementPattern pattern) {
        patternSelector.setMovementPattern(pattern);
    }

    public MovementPattern getCurrentPattern() {
        return patternSelector.getCurrentPattern();
    }

    public void emergencyEvade() {
        patternSelector.setMovementPattern(MovementPattern.EVASIVE_ZIG_ZAG);
        setUnderFire(true);
        setMovementSpeed(getMovementSpeed() * 1.4);
    }

    public void seekHighGround() {
        patternSelector.setMovementPattern(MovementPattern.TERRAIN_ADAPTIVE);
    }

    public void beginStrafe() {
        MovementPattern strafePattern =
                Math.random() < 0.7 ? MovementPattern.STRAFE_CIRCLE : MovementPattern.STRAFE_FIGURE8;
        patternSelector.setMovementPattern(strafePattern);
    }

    public void setMovementSpeed(double speed) {
        movementExecutor.setMovementSpeed(speed);
    }

    public double getMovementSpeed() {
        return movementExecutor.getMovementSpeed();
    }

    public void setJumpVelocity(double velocity) {
        movementExecutor.setJumpVelocity(velocity);
    }

    public boolean isDiverting() {
        return obstacleHandler.isDiverting();
    }

    public boolean isEvading() {
        MovementPattern current = patternSelector.getCurrentPattern();
        return current == MovementPattern.EVASIVE_ZIG_ZAG || current == MovementPattern.RETREAT_SPIRAL;
    }

    public boolean isStrafing() {
        MovementPattern current = patternSelector.getCurrentPattern();
        return current == MovementPattern.STRAFE_CIRCLE || current == MovementPattern.STRAFE_FIGURE8;
    }

    public boolean isRetreating() {
        return patternSelector.getCurrentPattern() == MovementPattern.RETREAT_SPIRAL;
    }

    public Vector getTargetVelocity() {
        return combatStateManager.getTargetVelocity();
    }

    public boolean hasRecentDamage() {
        return combatStateManager.hasRecentDamage();
    }

    public int getConsecutiveHits() {
        return combatStateManager.getConsecutiveHits();
    }

    public void resetCombatState() {
        combatStateManager.resetCombatState();
        patternSelector.setMovementPattern(MovementPattern.DIRECT);
        setMovementSpeed(0.25);
        blockValidator.clearCache();
    }

    public int getCacheSize() {
        return blockValidator.getCacheSize();
    }

    public void clearCache() {
        blockValidator.clearCache();
    }

    public void forceCacheClean() {
        blockValidator.forceCacheClean();
    }

    private void executeMovementPattern(LivingEntity target, double targetDistance, MovementPattern pattern) {
        switch (pattern) {
            case DIRECT -> movementExecutor.executeDirectMovement(target, targetDistance);
            case STRAFE_CIRCLE -> movementExecutor.executeStrafeCircle(target, targetDistance);
            case STRAFE_FIGURE8 -> movementExecutor.executeStrafeFigure8(target, targetDistance);
            case EVASIVE_ZIG_ZAG -> movementExecutor.executeEvasiveZigZag(target, targetDistance);
            case TERRAIN_ADAPTIVE -> movementExecutor.executeTerrainAdaptive(target, targetDistance);
            case RETREAT_SPIRAL -> movementExecutor.executeRetreatSpiral(target, targetDistance);
            case CRYSTAL_SPAM -> movementExecutor.executeCrystalSpamMovement(target, targetDistance);
        }
    }

    private void executeRetreatMovement(LivingEntity target, double targetDistance) {
        movementExecutor.executeRetreatSpiral(target, targetDistance);
    }

}
