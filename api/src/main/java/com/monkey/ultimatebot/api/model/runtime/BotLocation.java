package com.monkey.ultimatebot.api.model.runtime;

import java.util.Objects;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.World;
import org.jspecify.annotations.Nullable;

/**
 * Immutable API-safe spawn location payload.
 */
public record BotLocation(
        @Nullable String worldName, @Nullable UUID worldUUID, double x, double y, double z, float yaw, float pitch) {
    public BotLocation {
        if ((worldName == null || worldName.isBlank()) && worldUUID == null) {
            throw new IllegalArgumentException("worldName or worldUUID is required");
        }
    }

    public static BotLocation of(Location location) {
        Objects.requireNonNull(location, "location");
        World world = Objects.requireNonNull(location.getWorld(), "location world");
        return new BotLocation(
                world.getName(),
                world.getUID(),
                location.getX(),
                location.getY(),
                location.getZ(),
                location.getYaw(),
                location.getPitch());
    }
}
