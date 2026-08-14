package com.monkey.ultimatebot.compat;

import java.lang.reflect.Method;
import java.util.Objects;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * {@link Block#breakNaturally} overloads across Paper versions that share a Craft revision.
 *
 * <p>{@code breakNaturally(ItemStack, boolean)} (particle/sound trigger) exists from ~1.16. Paper
 * 1.15 is still Craft {@code v1_15_R1}, so this is dual-path in core — not a new NMS package.
 */
public final class BlockBreakAccess {

    private static final @Nullable Method WITH_TRIGGER =
            resolve(ItemStack.class, boolean.class);
    private static final @Nullable Method WITH_TOOL = resolve(ItemStack.class);

    private BlockBreakAccess() {}

    public static boolean breakNaturally(Block block, ItemStack tool, boolean triggerEffect) {
        Objects.requireNonNull(block, "block");
        Objects.requireNonNull(tool, "tool");
        if (WITH_TRIGGER != null) {
            try {
                Object result = WITH_TRIGGER.invoke(block, tool, Boolean.valueOf(triggerEffect));
                return result instanceof Boolean && ((Boolean) result).booleanValue();
            } catch (ReflectiveOperationException ignored) {
                // fall through to ItemStack-only
            }
        }
        if (WITH_TOOL != null) {
            try {
                Object result = WITH_TOOL.invoke(block, tool);
                return result instanceof Boolean && ((Boolean) result).booleanValue();
            } catch (ReflectiveOperationException ignored) {
                // fall through to no-arg
            }
        }
        return block.breakNaturally();
    }

    private static @Nullable Method resolve(Class<?>... params) {
        try {
            return Block.class.getMethod("breakNaturally", params);
        } catch (NoSuchMethodException ignored) {
            return null;
        }
    }
}
