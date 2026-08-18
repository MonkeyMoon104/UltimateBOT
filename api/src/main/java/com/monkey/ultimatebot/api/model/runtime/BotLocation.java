package com.monkey.ultimatebot.api.model.runtime;

import java.util.Objects;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.World;
import org.jspecify.annotations.Nullable;

/**
 * Immutable API-safe spawn location payload.
 */
public final class BotLocation {
    private final @Nullable String worldName;
    private final @Nullable UUID worldUUID;
    private final double x;
    private final double y;
    private final double z;
    private final float yaw;
    private final float pitch;

    public BotLocation(
            @Nullable String worldName,
            @Nullable UUID worldUUID,
            double x,
            double y,
            double z,
            float yaw,
            float pitch) {

        if ((worldName == null || worldName.trim().isEmpty()) && worldUUID == null) {
            throw new IllegalArgumentException("worldName or worldUUID is required");
        }
        this.worldName = worldName;
        this.worldUUID = worldUUID;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public @Nullable String worldName() {
        return worldName;
    }

    public @Nullable UUID worldUUID() {
        return worldUUID;
    }

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }

    public double z() {
        return z;
    }

    public float yaw() {
        return yaw;
    }

    public float pitch() {
        return pitch;
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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof BotLocation)) {
            return false;
        }
        BotLocation other = (BotLocation) obj;
        return java.util.Objects.equals(worldName, other.worldName)
                && java.util.Objects.equals(worldUUID, other.worldUUID)
                && Double.compare(x, other.x) == 0
                && Double.compare(y, other.y) == 0
                && Double.compare(z, other.z) == 0
                && Float.compare(yaw, other.yaw) == 0
                && Float.compare(pitch, other.pitch) == 0;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(worldName, worldUUID, x, y, z, yaw, pitch);
    }

    @Override
    public String toString() {
        return "BotLocation[worldName=" + worldName + ", worldUUID=" + worldUUID + ", x=" + x + ", y=" + y + ", z=" + z
                + ", yaw=" + yaw + ", pitch=" + pitch + "]";
    }
}
