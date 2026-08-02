package com.monkey.ultimatebot.bot.ai.behavior;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.api.model.BotLocation;
import com.monkey.ultimatebot.bot.BotOptions;
import com.monkey.ultimatebot.bot.ai.controllers.movement.BotMovementController;
import com.monkey.ultimatebot.bot.ai.controllers.rotation.BotRotationController;
import java.util.Objects;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jspecify.annotations.Nullable;

public final class IdleBehaviorController {

    private static final long DESTINATION_RETRY_MS = 3500L;
    private static final double DESTINATION_REACHED_DISTANCE = 1.6D;

    private final Player bot;
    private final Level level;
    private final BotOptions options;
    private final UltimateBot plugin;
    private final BotMovementController movementController;
    private final BotRotationController rotationController;
    private final Random random = new Random();

    private long lastTargetSeenAt = System.currentTimeMillis();
    private long lastDestinationSelectionAt;
    private @Nullable Vec3 destination;

    public IdleBehaviorController(
            Player bot,
            BotOptions options,
            UltimateBot plugin,
            BotMovementController movementController,
            BotRotationController rotationController) {
        this.bot = Objects.requireNonNull(bot, "bot");
        this.level = bot.level();
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

        Vec3 spawn = resolveSpawnPosition();
        if (spawn == null) {
            return;
        }

        long now = System.currentTimeMillis();
        double distanceFromSpawn = bot.position().distanceTo(spawn);
        boolean shouldReturn = distanceFromSpawn > options.getIdleReturnDistance()
                || now - lastTargetSeenAt >= options.getIdleReturnDelayMs();
        if (shouldReturn && distanceFromSpawn > DESTINATION_REACHED_DISTANCE) {
            destination = spawn;
            moveTo(spawn);
            return;
        }

        if (destination == null
                || bot.position().distanceTo(destination) <= DESTINATION_REACHED_DISTANCE
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

    private void moveTo(Vec3 position) {
        movementController.moveToPosition(position);
        rotationController.lookAt(position.x, position.y, position.z);
    }

    private Vec3 findDestination(Vec3 spawn) {
        double radius = options.getIdleWanderRadius();
        for (int attempt = 0; attempt < 12; attempt++) {
            double angle = random.nextDouble() * Math.PI * 2.0D;
            double distance = 2.0D + random.nextDouble() * Math.max(1.0D, radius - 2.0D);
            int x = (int) Math.floor(spawn.x + Math.cos(angle) * distance);
            int z = (int) Math.floor(spawn.z + Math.sin(angle) * distance);
            int y = findSafeY(x, (int) Math.round(spawn.y), z);
            if (y == Integer.MIN_VALUE) {
                continue;
            }
            Vec3 candidate = new Vec3(x + 0.5D, y, z + 0.5D);
            if (isPvpAllowed(candidate)) {
                return candidate;
            }
        }
        return spawn;
    }

    private int findSafeY(int x, int baseY, int z) {
        World world = bot.getBukkitEntity().getWorld();
        int minBuildHeight = world == null ? -64 : world.getMinHeight();
        int maxBuildHeight = world == null ? 320 : world.getMaxHeight();
        int minY = Math.max(minBuildHeight, baseY - 6);
        int maxY = Math.min(maxBuildHeight - 2, baseY + 6);
        for (int y = maxY; y >= minY; y--) {
            BlockPos feet = new BlockPos(x, y, z);
            if (level.getBlockState(feet.below()).isSolidRender()
                    && !level.getBlockState(feet).isSolidRender()
                    && !level.getBlockState(feet.above()).isSolidRender()) {
                return y;
            }
        }
        return Integer.MIN_VALUE;
    }

    private @Nullable Vec3 resolveSpawnPosition() {
        BotLocation spawn = options.getSpawnLocation();
        return spawn == null ? null : new Vec3(spawn.x(), spawn.y(), spawn.z());
    }

    private boolean isPvpAllowed(Vec3 position) {
        if (!options.isRespectWorldGuardPvp() || plugin.getWorldGuardPvpService() == null) {
            return true;
        }

        World world = bot.getBukkitEntity().getWorld();
        if (world == null) {
            BotLocation spawn = options.getSpawnLocation();
            world = spawn == null || spawn.worldUUID() == null ? null : Bukkit.getWorld(spawn.worldUUID());
        }
        return world != null
                && plugin.getWorldGuardPvpService()
                        .isPvpAllowed(new Location(world, position.x, position.y, position.z));
    }
}
