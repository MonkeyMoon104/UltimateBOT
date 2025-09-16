package it.coralmc.sandbox.bot.ai.controllers.brain.helper;

import it.coralmc.sandbox.bot.ai.controllers.brain.helper.inter.ICombatStateManager;
import it.coralmc.sandbox.bot.ai.controllers.brain.helper.inter.IPathfindingManager;
import it.coralmc.sandbox.bot.ai.controllers.enderpearl.BotEnderpearlController;
import it.coralmc.sandbox.bot.ai.controllers.movement.BotMovementController;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

public class PathfindingManager implements IPathfindingManager {

    private final Player bot;
    private final Level level;
    private final BotMovementController movementController;
    private final BotEnderpearlController enderpearlController;
    private final ICombatStateManager combatStateManager;

    private long lastActionTime = 0;
    private Vec3 lastBotPosition;
    private int stuckCounter = 0;
    private static final long MAX_STUCK_TIME = 1500;
    private static final double MIN_MOVEMENT_THRESHOLD = 0.1;

    private long lastPathfindingAttempt = 0;
    private static final long PATHFINDING_ATTEMPT_COOLDOWN = 3000;
    private boolean usingPathfinding = false;

    public PathfindingManager(Player bot, Level level, BotMovementController movementController,
                              BotEnderpearlController enderpearlController, ICombatStateManager combatStateManager) {
        this.bot = bot;
        this.level = level;
        this.movementController = movementController;
        this.enderpearlController = enderpearlController;
        this.combatStateManager = combatStateManager;
        this.lastBotPosition = bot.position();
    }

    @Override
    public void checkForStuck(Player target) {
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

    @Override
    public void attemptPathfindingOrPearl(Player target) {
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

    @Override
    public boolean hasObstacleBetween(Vec3 start, Vec3 end) {
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

    @Override
    public Vec3 calculatePearlTargetAroundPlayer(Player target) {
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

    @Override
    public boolean isSafeLandingSpot(BlockPos pos) {
        return !level.getBlockState(pos.below()).isAir() &&
                level.getBlockState(pos).isAir() &&
                level.getBlockState(pos.above()).isAir();
    }

    @Override
    public void forceUnstuck(Player target) {
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

        if (System.currentTimeMillis() - combatStateManager.getCurrentState().ordinal() > 3000) {
            if (combatStateManager.getCurrentState() == ICombatStateManager.CombatState.DEFENSIVE ||
                    combatStateManager.getCurrentState() == ICombatStateManager.CombatState.REPOSITIONING) {
            }
        }
    }

    private Vec3 getStrafeDirection(Player target) {
        Vec3 toTarget = target.position().subtract(bot.position()).normalize();
        return new Vec3(-toTarget.z, 0, toTarget.x);
    }

    @Override
    public boolean isUsingPathfinding() {
        return usingPathfinding;
    }

    @Override
    public void setUsingPathfinding(boolean using) {
        this.usingPathfinding = using;
    }

    public void updateLastBotPosition() {
        this.lastBotPosition = bot.position();
    }

    public void updateLastActionTime() {
        this.lastActionTime = System.currentTimeMillis();
    }
}