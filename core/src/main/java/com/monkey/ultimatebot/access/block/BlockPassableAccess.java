package com.monkey.ultimatebot.access.block;

import com.monkey.ultimatebot.access.item.MaterialAirAccess;
import java.lang.reflect.Method;
import java.util.Objects;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.jspecify.annotations.Nullable;

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
