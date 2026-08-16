package com.monkey.ultimatebot.compat;

import java.lang.reflect.Method;
import java.util.Objects;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.jspecify.annotations.Nullable;

/**
 * {@link Block#isPassable()} across Paper revisions.
 *
 * <p>The method exists from 1.13.1/1.13.2 Bukkit API. Pure Paper 1.13 ({@code v1_13_R1}) lacks it —
 * calling the hard-linked method crashes with {@link NoSuchMethodError}. Prefer the modern API when
 * present; otherwise treat non-solid (and air) as passable.
 */
public final class BlockPassableAccess {

    private static final @Nullable Method IS_PASSABLE = resolve();

    private BlockPassableAccess() {}

    public static boolean isPassable(Block block) {
        Objects.requireNonNull(block, "block");
        if (IS_PASSABLE != null) {
            try {
                Object result = IS_PASSABLE.invoke(block);
                return result instanceof Boolean && ((Boolean) result).booleanValue();
            } catch (ReflectiveOperationException | LinkageError ignored) {
                // fall through
            }
        }
        Material type = block.getType();
        if (MaterialAirAccess.isAir(type)) {
            return true;
        }
        try {
            if (block.isLiquid()) {
                return true;
            }
        } catch (NoSuchMethodError ignored) {
            // very old API
        }
        return !type.isSolid();
    }

    private static @Nullable Method resolve() {
        try {
            return Block.class.getMethod("isPassable");
        } catch (NoSuchMethodException ignored) {
            return null;
        }
    }
}
