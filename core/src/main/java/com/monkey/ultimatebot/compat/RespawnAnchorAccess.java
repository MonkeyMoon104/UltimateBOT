package com.monkey.ultimatebot.compat;

import java.lang.reflect.Method;
import java.util.Objects;
import org.bukkit.block.Block;
import org.jspecify.annotations.Nullable;

/**
 * Respawn-anchor block data without linking {@code org.bukkit.block.data.*}.
 *
 * <p>{@code BlockData} / {@code RespawnAnchor} exist only from 1.13+ / 1.16+. Hard imports make
 * {@code BotRAPVPController} fail to load on 1.12 even when RAPVP stays disabled.
 */
public final class RespawnAnchorAccess {

    private static final @Nullable Class<?> BLOCK_DATA_TYPE = classOrNull("org.bukkit.block.data.BlockData");
    private static final @Nullable Class<?> ANCHOR_TYPE = classOrNull("org.bukkit.block.data.type.RespawnAnchor");
    private static final @Nullable Method GET_BLOCK_DATA = method(Block.class, "getBlockData");
    private static final @Nullable Method SET_BLOCK_DATA =
            BLOCK_DATA_TYPE == null ? null : method(Block.class, "setBlockData", BLOCK_DATA_TYPE, boolean.class);
    private static final @Nullable Method GET_CHARGES = method(ANCHOR_TYPE, "getCharges");
    private static final @Nullable Method GET_MAXIMUM_CHARGES = method(ANCHOR_TYPE, "getMaximumCharges");
    private static final @Nullable Method SET_CHARGES = method(ANCHOR_TYPE, "setCharges", int.class);

    private RespawnAnchorAccess() {}

    public static boolean isAvailable() {
        return ANCHOR_TYPE != null
                && GET_BLOCK_DATA != null
                && SET_BLOCK_DATA != null
                && GET_CHARGES != null
                && GET_MAXIMUM_CHARGES != null
                && SET_CHARGES != null;
    }

    public static boolean isRespawnAnchor(Block block) {
        return readAnchor(block) != null;
    }

    public static int getCharges(Block block) {
        Object anchor = readAnchor(block);
        Method getCharges = GET_CHARGES;
        if (anchor == null || getCharges == null) {
            return 0;
        }
        try {
            Object value = getCharges.invoke(anchor);
            return value instanceof Integer ? ((Integer) value).intValue() : 0;
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return 0;
        }
    }

    /**
     * Sets charges to the anchor maximum (full charge), matching prior RAPVP charge behaviour.
     *
     * @return {@code true} when the block was an under-charged anchor and was updated
     */
    public static boolean chargeToMaximum(Block block) {
        Objects.requireNonNull(block, "block");
        Method getMax = GET_MAXIMUM_CHARGES;
        Method getCharges = GET_CHARGES;
        Method setCharges = SET_CHARGES;
        Method setBlockData = SET_BLOCK_DATA;
        if (getMax == null || getCharges == null || setCharges == null || setBlockData == null) {
            return false;
        }
        Object anchor = readAnchor(block);
        if (anchor == null) {
            return false;
        }
        try {
            Object maxObj = getMax.invoke(anchor);
            Object chargesObj = getCharges.invoke(anchor);
            int max = maxObj instanceof Integer ? ((Integer) maxObj).intValue() : 0;
            int charges = chargesObj instanceof Integer ? ((Integer) chargesObj).intValue() : 0;
            if (charges >= max) {
                return false;
            }
            setCharges.invoke(anchor, Integer.valueOf(max));
            setBlockData.invoke(block, anchor, Boolean.TRUE);
            return true;
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return false;
        }
    }

    private static @Nullable Object readAnchor(Block block) {
        Objects.requireNonNull(block, "block");
        Method getBlockData = GET_BLOCK_DATA;
        Class<?> anchorType = ANCHOR_TYPE;
        if (getBlockData == null || anchorType == null) {
            return null;
        }
        try {
            Object data = getBlockData.invoke(block);
            if (data == null || !anchorType.isInstance(data)) {
                return null;
            }
            return data;
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return null;
        }
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
            return type.getMethod(name, params);
        } catch (NoSuchMethodException ignored) {
            return null;
        }
    }
}
