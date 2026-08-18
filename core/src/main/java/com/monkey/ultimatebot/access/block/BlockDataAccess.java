package com.monkey.ultimatebot.access.block;

import java.lang.reflect.Method;
import java.util.Objects;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

public final class BlockDataAccess {

    private static final @Nullable Class<?> BLOCK_DATA_TYPE = classOrNull("org.bukkit.block.data.BlockData");
    private static final @Nullable Method GET_BLOCK_DATA = method(Block.class, "getBlockData");
    private static final @Nullable Method SET_BLOCK_DATA =
            BLOCK_DATA_TYPE == null ? null : method(Block.class, "setBlockData", BLOCK_DATA_TYPE, boolean.class);
    private static final @Nullable Method CREATE_BLOCK_DATA = method(Material.class, "createBlockData");
    private static final @Nullable Method SEND_BLOCK_CHANGE_DATA =
            BLOCK_DATA_TYPE == null ? null : method(Player.class, "sendBlockChange", Location.class, BLOCK_DATA_TYPE);
    private static final @Nullable Method SEND_BLOCK_CHANGE_LEGACY =
            method(Player.class, "sendBlockChange", Location.class, Material.class, byte.class);
    private static final @Nullable Method BLOCK_GET_DATA = method(Block.class, "getData");

    private BlockDataAccess() {}

    public static boolean supportsBlockData() {
        return GET_BLOCK_DATA != null && SET_BLOCK_DATA != null;
    }

    public static Object capture(Block block) {
        Objects.requireNonNull(block, "block");
        Method getBlockData = GET_BLOCK_DATA;
        if (getBlockData != null) {
            try {
                Object data = getBlockData.invoke(block);
                if (data != null) {
                    return data;
                }
            } catch (ReflectiveOperationException | LinkageError ignored) {

            }
        }
        return block.getState();
    }

    public static void restore(Block block, Object snapshot, boolean applyPhysics) {
        Objects.requireNonNull(block, "block");
        Objects.requireNonNull(snapshot, "snapshot");
        Method setBlockData = SET_BLOCK_DATA;
        Class<?> blockDataType = BLOCK_DATA_TYPE;
        if (setBlockData != null && blockDataType != null && blockDataType.isInstance(snapshot)) {
            try {
                setBlockData.invoke(block, snapshot, Boolean.valueOf(applyPhysics));
                return;
            } catch (ReflectiveOperationException | LinkageError ignored) {

            }
        }
        if (snapshot instanceof BlockState) {
            ((BlockState) snapshot).update(true, applyPhysics);
            return;
        }
        throw new IllegalArgumentException(
                "Unsupported block snapshot: " + snapshot.getClass().getName());
    }

    public static void broadcastMaterial(Location location, Material material) {
        Objects.requireNonNull(location, "location");
        Objects.requireNonNull(material, "material");
        Method create = CREATE_BLOCK_DATA;
        Method sendData = SEND_BLOCK_CHANGE_DATA;
        if (create != null && sendData != null) {
            try {
                Object data = create.invoke(material);
                broadcastRaw(location, data, sendData);
                return;
            } catch (ReflectiveOperationException | LinkageError ignored) {

            }
        }
        broadcastLegacy(location, material, (byte) 0);
    }

    public static void broadcastCurrent(Location location) {
        Objects.requireNonNull(location, "location");
        Block block = location.getBlock();
        Method getBlockData = GET_BLOCK_DATA;
        Method sendData = SEND_BLOCK_CHANGE_DATA;
        if (getBlockData != null && sendData != null) {
            try {
                Object data = getBlockData.invoke(block);
                broadcastRaw(location, data, sendData);
                return;
            } catch (ReflectiveOperationException | LinkageError ignored) {

            }
        }
        broadcastLegacy(location, block.getType(), legacyData(block));
    }

    private static void broadcastRaw(Location location, @Nullable Object blockData, Method sendData)
            throws ReflectiveOperationException {
        if (blockData == null) {
            return;
        }
        World world = Objects.requireNonNull(location.getWorld(), "location world");
        for (Player viewer : world.getPlayers()) {
            sendData.invoke(viewer, location, blockData);
        }
    }

    private static void broadcastLegacy(Location location, Material material, byte data) {
        Method sendLegacy = SEND_BLOCK_CHANGE_LEGACY;
        World world = Objects.requireNonNull(location.getWorld(), "location world");
        if (sendLegacy == null) {
            return;
        }
        for (Player viewer : world.getPlayers()) {
            try {
                sendLegacy.invoke(viewer, location, material, Byte.valueOf(data));
            } catch (ReflectiveOperationException | LinkageError ignored) {

            }
        }
    }

    private static byte legacyData(Block block) {
        Method getData = BLOCK_GET_DATA;
        if (getData == null) {
            return 0;
        }
        try {
            Object value = getData.invoke(block);
            if (value instanceof Byte) {
                return ((Byte) value).byteValue();
            }
            if (value instanceof Number) {
                return ((Number) value).byteValue();
            }
        } catch (ReflectiveOperationException | LinkageError ignored) {

        }
        return 0;
    }

    private static @Nullable Class<?> classOrNull(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException | LinkageError ignored) {
            return null;
        }
    }

    private static @Nullable Method method(@Nullable Class<?> type, String name, Class<?>... params) {
        if (type == null) {
            return null;
        }
        try {
            Method method = type.getMethod(name, params);
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException | SecurityException ignored) {
            return null;
        }
    }
}
