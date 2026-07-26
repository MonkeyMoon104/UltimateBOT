package com.monkey.mcbot.bot.ai.controllers.movement;

import com.monkey.mcbot.bot.ai.controllers.movement.helper.*;
import com.monkey.mcbot.bot.ai.controllers.movement.helper.interf.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class BotMovementController {
    private final Player bot;
    private final Level level;

    private final IBlockStateValidator blockValidator;
    private final IPathfinder pathfinder;
    private final IObstacleHandler obstacleHandler;
    private final IMovementExecutor movementExecutor;
    private final IMovementPatternSelector patternSelector;
    private final ICombatStateManager combatStateManager;

    private double currentTargetDistance = 3.0;
    private boolean preferHighGround = false;
    private boolean avoidCorners = true;
    private long lastMovementTime = 0;
    private static final long MAX_MOVEMENT_STALL_TIME = 1000;

    public BotMovementController(Player bot, Level level) {
        this.bot = bot;
        this.level = level;

        this.blockValidator = new BlockStateValidator(level);
        this.obstacleHandler = new ObstacleHandler(bot, level, blockValidator, 0.25, 0.42);
        this.movementExecutor = new MovementExecutor(bot, level, blockValidator, obstacleHandler);
        this.pathfinder = new JumpPointSearchPathfinder(bot, blockValidator);
        this.patternSelector = new MovementPatternSelector(obstacleHandler);
        this.combatStateManager = new CombatStateManager();
    }


    public void moveTowards(Player target, double targetDistance) {
        if (isStuckInPlace()) {
            forceUnstick(target);
            return;
        }
        combatStateManager.updateCombatData(target);
        this.currentTargetDistance = targetDistance;

        MovementPattern selectedPattern = patternSelector.selectOptimalPattern(
                target, targetDistance, bot.position(), target.position(),
                combatStateManager.isUnderFire(), combatStateManager.getConsecutiveHits()
        );

        executeMovementPattern(target, targetDistance, selectedPattern);
        lastMovementTime = System.currentTimeMillis();
    }

    public void moveToTarget(Player target, double targetDistance) {
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

    public void moveAwayFrom(Player target, double targetDistance) {
        combatStateManager.updateCombatData(target);
        this.currentTargetDistance = targetDistance;
        if (patternSelector.getCurrentPattern() != MovementPattern.RETREAT_SPIRAL &&
                patternSelector.getCurrentPattern() != MovementPattern.EVASIVE_ZIG_ZAG) {
            patternSelector.setMovementPattern(MovementPattern.RETREAT_SPIRAL);
        }
        executeRetreatMovement(target, targetDistance);
    }

    public void maintainDistance(Player target, double targetDistance) {
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

    public boolean calculatePathTo(Vec3 targetPos) {
        return pathfinder.calculatePathTo(targetPos);
    }

    public boolean followPath() {
        if (pathfinder.followPath()) {
            Vec3 currentPoint = pathfinder.getCurrentPathPoint();
            if (currentPoint != null) {
                movementExecutor.moveToPosition(currentPoint);
            }
            return true;
        }
        return false;
    }

    public boolean hasActivePath() {
        return pathfinder.hasActivePath();
    }

    public void clearPath() {
        pathfinder.clearPath();
    }

    public boolean shouldRecalculatePath() {
        return pathfinder.shouldRecalculatePath();
    }

    public Vec3 getCurrentPathPoint() {
        return pathfinder.getCurrentPathPoint();
    }


    public void moveToPosition(Vec3 targetPos) {
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

        if (combatStateManager instanceof CombatStateManager) {
            ((CombatStateManager) combatStateManager).setLastSafePosition(bot.position());
        }

        ensureMovement();
        lastMovementTime = System.currentTimeMillis();
    }

    public boolean isStuckInPlace() {
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastMovementTime > MAX_MOVEMENT_STALL_TIME) {
            return true;
        }

        Vec3 velocity = bot.getDeltaMovement();
        return velocity.lengthSqr() < 0.01;
    }

    public void forceUnstick(Player target) {
        resetCombatState();
        obstacleHandler.clearDiversion();

        setUnderFire(true);
        emergencyEvade();
        if (bot.onGround()) {
            Vec3 velocity = bot.getDeltaMovement();
            bot.setDeltaMovement(velocity.x, Math.max(velocity.y, 0.36), velocity.z);
        }

        if (target != null) {
            Vec3 botPos = bot.position();
            Vec3 targetPos = target.position();

            Vec3 direction = targetPos.subtract(botPos).normalize();
            Vec3 sideDirection = new Vec3(-direction.z, 0, direction.x);

            Vec3 unstuckPos = botPos.add(sideDirection.scale(3.0));
            moveToPosition(unstuckPos);
        } else {
            double randomX = (Math.random() - 0.5) * 4.0;
            double randomZ = (Math.random() - 0.5) * 4.0;
            Vec3 randomPos = bot.position().add(randomX, 0, randomZ);
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
        setPreferHighGround(true);
        patternSelector.setMovementPattern(MovementPattern.TERRAIN_ADAPTIVE);
    }

    public void beginStrafe() {
        MovementPattern strafePattern = Math.random() < 0.7 ?
                MovementPattern.STRAFE_CIRCLE : MovementPattern.STRAFE_FIGURE8;
        patternSelector.setMovementPattern(strafePattern);
    }


    public void setMovementSpeed(double speed) {
        if (movementExecutor instanceof MovementExecutor) {
            ((MovementExecutor) movementExecutor).setMovementSpeed(speed);
        }
    }

    public double getMovementSpeed() {
        if (movementExecutor instanceof MovementExecutor) {
            return ((MovementExecutor) movementExecutor).getMovementSpeed();
        }
        return 0.25;
    }

    public void setJumpVelocity(double velocity) {
        if (movementExecutor instanceof MovementExecutor) {
            ((MovementExecutor) movementExecutor).setJumpVelocity(velocity);
        }
    }

    public void setPreferHighGround(boolean prefer) {
        this.preferHighGround = prefer;
    }

    public void setAvoidCorners(boolean avoid) {
        this.avoidCorners = avoid;
    }


    public boolean isDiverting() {
        return obstacleHandler.isDiverting();
    }

    public boolean isEvading() {
        MovementPattern current = patternSelector.getCurrentPattern();
        return current == MovementPattern.EVASIVE_ZIG_ZAG ||
                current == MovementPattern.RETREAT_SPIRAL;
    }

    public boolean isStrafing() {
        MovementPattern current = patternSelector.getCurrentPattern();
        return current == MovementPattern.STRAFE_CIRCLE ||
                current == MovementPattern.STRAFE_FIGURE8;
    }

    public boolean isRetreating() {
        return patternSelector.getCurrentPattern() == MovementPattern.RETREAT_SPIRAL;
    }

    public Vec3 getTargetVelocity() {
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


    private void executeMovementPattern(Player target, double targetDistance, MovementPattern pattern) {
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

    private void executeRetreatMovement(Player target, double targetDistance) {
        movementExecutor.executeRetreatSpiral(target, targetDistance);
    }
}
