package com.monkey.ultimatebot.sdk.model.request;

import java.util.UUID;

/**
 * World position used when spawning a bot at an explicit location.
 *
 * @param worldName Bukkit world name, used when {@code worldUUID} is not available
 * @param worldUUID Bukkit world UUID, preferred when available
 * @param x x coordinate
 * @param y y coordinate
 * @param z z coordinate
 * @param yaw yaw rotation
 * @param pitch pitch rotation
 */
public final class BotLocationRequest {
    private final String worldName;
    private final UUID worldUUID;
    private final double x;
    private final double y;
    private final double z;
    private final float yaw;
    private final float pitch;

    public BotLocationRequest(String worldName, UUID worldUUID, double x, double y, double z, float yaw, float pitch) {
        this.worldName = worldName;
        this.worldUUID = worldUUID;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public String worldName() {
        return worldName;
    }

    public UUID worldUUID() {
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

    /**
     * Creates a location request by world name.
     *
     * @param worldName Bukkit world name
     * @param x x coordinate
     * @param y y coordinate
     * @param z z coordinate
     * @return location request
     */
    public static BotLocationRequest of(String worldName, double x, double y, double z) {
        return new BotLocationRequest(worldName, null, x, y, z, 0.0F, 0.0F);
    }

    /**
     * Creates a location request by world UUID.
     *
     * @param worldUUID Bukkit world UUID
     * @param x x coordinate
     * @param y y coordinate
     * @param z z coordinate
     * @return location request
     */
    public static BotLocationRequest of(UUID worldUUID, double x, double y, double z) {
        return new BotLocationRequest(null, worldUUID, x, y, z, 0.0F, 0.0F);
    }

    /**
     * Returns a copy with explicit player rotation.
     *
     * @param yaw yaw rotation
     * @param pitch pitch rotation
     * @return copied request with rotation
     */
    public BotLocationRequest withRotation(float yaw, float pitch) {
        return new BotLocationRequest(worldName, worldUUID, x, y, z, yaw, pitch);
    }

    /**
     * Returns whether the request contains at least one world identifier.
     *
     * @return whether the location can resolve a world
     */
    public boolean hasWorldReference() {
        return worldUUID != null || (worldName != null && !worldName.trim().isEmpty());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof BotLocationRequest)) {
            return false;
        }
        BotLocationRequest other = (BotLocationRequest) obj;
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
        return "BotLocationRequest[worldName=" + worldName + ", worldUUID=" + worldUUID + ", x=" + x + ", y=" + y
                + ", z=" + z + ", yaw=" + yaw + ", pitch=" + pitch + "]";
    }
}
