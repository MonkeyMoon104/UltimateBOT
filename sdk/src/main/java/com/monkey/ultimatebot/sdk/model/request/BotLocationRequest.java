package com.monkey.ultimatebot.sdk.model.request;

import java.util.UUID;
import org.jspecify.annotations.Nullable;

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
public record BotLocationRequest(
        @Nullable String worldName, @Nullable UUID worldUUID, double x, double y, double z, float yaw, float pitch) {
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
        return worldUUID != null || (worldName != null && !worldName.isBlank());
    }
}
