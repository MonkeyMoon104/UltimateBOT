package com.monkey.ultimatebot.bot.ai.behavior;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.api.model.runtime.BotLocation;
import com.monkey.ultimatebot.bot.BotOptions;
import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.bot.ai.controllers.movement.BotMovementController;
import com.monkey.ultimatebot.bot.ai.controllers.rotation.BotRotationController;
import com.monkey.ultimatebot.compat.WorldAccess;
import java.util.Objects;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;
import org.jspecify.annotations.Nullable;

public final class IdleBehaviorController {

    private static final long DESTINATION_RETRY_MS = 3500L;
    private static final double DESTINATION_REACHED_DISTANCE = 1.6D;

    private final ITrainingBot bot;
    private final BotOptions options;
    private final UltimateBot plugin;
    private final BotMovementController movementController;
    private final BotRotationController rotationController;
    private final Random random = new Random();

    private long lastTargetSeenAt = System.currentTimeMillis();
    private long lastDestinationSelectionAt;
    private @Nullable Vector destination;

    public IdleBehaviorController(
            ITrainingBot bot,
            BotOptions options,
            UltimateBot plugin,
            BotMovementController movementController,
            BotRotationController rotationController) {
        this.bot = Objects.requireNonNull(bot, "bot");
        this.options = Objects.requireNonNull(options, "options");
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.movementController = Objects.requireNonNull(movementController, "movementController");
        this.rotationController = Objects.requireNonNull(rotationController, "rotationController");
    }

    public void recordTargetSeen() {
        lastTargetSeenAt = System.currentTimeMillis();
        destination = null;
    }

    public void tick() {
        if (!options.isIdleWander()) {
            destination = null;
            movementController.stopMovement();
            return;
        }

        Vector spawn = resolveSpawnPosition();
        if (spawn == null) {
            return;
        }

        long now = System.currentTimeMillis();
        double distanceFromSpawn = bot.bukkitPosition().distance(spawn);
        boolean shouldReturn = distanceFromSpawn > options.getIdleReturnDistance()
                || now - lastTargetSeenAt >= options.getIdleReturnDelayMs();
        if (shouldReturn && distanceFromSpawn > DESTINATION_REACHED_DISTANCE) {
            destination = spawn;
            moveTo(spawn);
            return;
        }

        if (destination == null
                || bot.bukkitPosition().distance(destination) <= DESTINATION_REACHED_DISTANCE
                || now - lastDestinationSelectionAt >= DESTINATION_RETRY_MS) {
            destination = findDestination(spawn);
            lastDestinationSelectionAt = now;
        }
        if (destination != null) {
            moveTo(destination);
        }
    }

    public void close() {
        destination = null;
    }

    private void moveTo(Vector position) {
        movementController.moveToPosition(position);
        rotationController.lookAt(position);
    }

    private Vector findDestination(Vector spawn) {
        double radius = options.getIdleWanderRadius();
        for (int attempt = 0; attempt < 12; attempt++) {
            double angle = random.nextDouble() * Math.PI * 2.0D;
            double distance = 2.0D + random.nextDouble() * Math.max(1.0D, radius - 2.0D);
            int x = (int) Math.floor(spawn.getX() + Math.cos(angle) * distance);
            int z = (int) Math.floor(spawn.getZ() + Math.sin(angle) * distance);
            int y = findSafeY(x, (int) Math.round(spawn.getY()), z);
            if (y == Integer.MIN_VALUE) {
                continue;
            }
            Vector candidate = new Vector(x + 0.5D, y, z + 0.5D);
            if (isPvpAllowed(candidate)) {
                return candidate;
            }
        }
        return spawn;
    }

    private int findSafeY(int x, int baseY, int z) {
        World world = bot.getWorld();
        int minBuildHeight = WorldAccess.minHeight(world);
        int maxBuildHeight = WorldAccess.maxHeight(world);
        int minY = Math.max(minBuildHeight, baseY - 6);
        int maxY = Math.min(maxBuildHeight - 2, baseY + 6);
        for (int y = maxY; y >= minY; y--) {
            Block feet = world.getBlockAt(x, y, z);
            if (feet.getRelative(0, -1, 0).getType().isSolid()
                    && feet.isPassable()
                    && feet.getRelative(0, 1, 0).isPassable()) {
                return y;
            }
        }
        return Integer.MIN_VALUE;
    }

    private @Nullable Vector resolveSpawnPosition() {
        BotLocation spawn = options.getSpawnLocation();
        return spawn == null ? null : new Vector(spawn.x(), spawn.y(), spawn.z());
    }

    private boolean isPvpAllowed(Vector position) {
        if (!options.isRespectWorldGuardPvp() || plugin.getWorldGuardPvpService() == null) {
            return true;
        }

        World world = bot.getWorld();
        if (world == null) {
            BotLocation spawn = options.getSpawnLocation();
            world = spawn == null || spawn.worldUUID() == null ? null : Bukkit.getWorld(spawn.worldUUID());
        }
        return world != null
                && plugin.getWorldGuardPvpService()
                        .isPvpAllowed(new Location(world, position.getX(), position.getY(), position.getZ()));
    }
}
