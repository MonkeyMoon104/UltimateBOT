package com.monkey.ultimatebot.bot.ai.behavior;

import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.bot.ai.controllers.brain.helper.inter.IPathfindingManager;
import com.monkey.ultimatebot.bot.ai.controllers.movement.BotMovementController;
import com.monkey.ultimatebot.bot.ai.controllers.movement.helper.noobs.BotNoobMovementController;
import com.monkey.ultimatebot.compat.EntityCoordsAccess;
import java.util.Objects;
import org.bukkit.entity.Player;

public final class FollowBehaviorController {
    private final ITrainingBot bot;
    private final BotMovementController movement;
    private final BotNoobMovementController directMovement;
    private final IPathfindingManager pathfinding;
    private final FollowDistancePolicy distancePolicy = new FollowDistancePolicy();

    public FollowBehaviorController(
            ITrainingBot bot,
            BotMovementController movement,
            BotNoobMovementController directMovement,
            IPathfindingManager pathfinding) {
        this.bot = Objects.requireNonNull(bot, "bot");
        this.movement = Objects.requireNonNull(movement, "movement");
        this.directMovement = Objects.requireNonNull(directMovement, "directMovement");
        this.pathfinding = Objects.requireNonNull(pathfinding, "pathfinding");
    }

    public void tick(Player target) {
        Objects.requireNonNull(target, "target");
        double deltaX = EntityCoordsAccess.getX(target) - bot.bukkitPosition().getX();
        double deltaZ = EntityCoordsAccess.getZ(target) - bot.bukkitPosition().getZ();
        double horizontalDistance = Math.hypot(deltaX, deltaZ);
        if (!distancePolicy.shouldAdvance(horizontalDistance)) {
            stop();
            return;
        }

        if (followActivePath()) {
            return;
        }
        pathfinding.checkForStuck(target);
        if (!followActivePath()) {
            directMovement.moveTowards(target, FollowDistancePolicy.STOP_DISTANCE);
        }
    }

    public void reset() {
        distancePolicy.reset();
    }

    private boolean followActivePath() {
        if (!pathfinding.isUsingPathfinding() || !movement.hasActivePath()) {
            return false;
        }
        if (movement.followPath()) {
            return true;
        }
        pathfinding.setUsingPathfinding(false);
        movement.clearPath();
        return false;
    }

    private void stop() {
        if (pathfinding.isUsingPathfinding() || movement.hasActivePath()) {
            pathfinding.setUsingPathfinding(false);
            movement.clearPath();
        }
        directMovement.stopMovement();
        movement.stopMovement();
    }
}
