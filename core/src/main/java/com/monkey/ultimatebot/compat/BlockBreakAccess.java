package com.monkey.ultimatebot.compat;

import java.lang.reflect.Method;
import java.util.Objects;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * {@link Block#breakNaturally} overloads and {@link BlockBreakEvent} drop APIs across Bukkit
 * revisions.
 *
 * <p>{@code breakNaturally(ItemStack, boolean)} exists from ~1.16. {@code isDropItems}/{@code
 * setDropItems}/{@code setExpToDrop} exist from 1.12. Hard calls crash 1.11 with {@code
 * NoSuchMethodError} during UHC web breaks.
 */
public final class BlockBreakAccess {

    private static final @Nullable Method WITH_TRIGGER =
            resolveBreak(ItemStack.class, boolean.class);
    private static final @Nullable Method WITH_TOOL = resolveBreak(ItemStack.class);
    private static final @Nullable Method IS_DROP_ITEMS = resolveEvent("isDropItems");
    private static final @Nullable Method SET_DROP_ITEMS = resolveEvent("setDropItems", boolean.class);
    private static final @Nullable Method SET_EXP_TO_DROP = resolveEvent("setExpToDrop", int.class);

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

    /** {@code true} when the event allows item drops, or when the 1.12 API is absent. */
    public static boolean isDropItems(BlockBreakEvent event) {
        Objects.requireNonNull(event, "event");
        if (IS_DROP_ITEMS == null) {
            return true;
        }
        try {
            Object result = IS_DROP_ITEMS.invoke(event);
            return !(result instanceof Boolean) || ((Boolean) result).booleanValue();
        } catch (ReflectiveOperationException ignored) {
            return true;
        }
    }

    public static void setDropItems(BlockBreakEvent event, boolean dropItems) {
        Objects.requireNonNull(event, "event");
        if (SET_DROP_ITEMS == null) {
            return;
        }
        try {
            SET_DROP_ITEMS.invoke(event, Boolean.valueOf(dropItems));
        } catch (ReflectiveOperationException ignored) {
            // 1.11 has no drop flag; WorldProtectionService restores tracked blocks instead.
        }
    }

    public static void setExpToDrop(BlockBreakEvent event, int exp) {
        Objects.requireNonNull(event, "event");
        if (SET_EXP_TO_DROP == null) {
            return;
        }
        try {
            SET_EXP_TO_DROP.invoke(event, Integer.valueOf(exp));
        } catch (ReflectiveOperationException ignored) {
            // 1.11 has no exp-drop setter.
        }
    }

    private static @Nullable Method resolveBreak(Class<?>... params) {
        try {
            return Block.class.getMethod("breakNaturally", params);
        } catch (NoSuchMethodException ignored) {
            return null;
        }
    }

    private static @Nullable Method resolveEvent(String name, Class<?>... params) {
        try {
            return BlockBreakEvent.class.getMethod(name, params);
        } catch (NoSuchMethodException ignored) {
            return null;
        }
    }
}
