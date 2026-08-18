package com.monkey.ultimatebot.compat;

import java.lang.reflect.Constructor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * {@link BlockPlaceEvent} gained an {@code EquipmentSlot} hand argument in 1.9. Spigot 1.7–1.8 use
 * the six-argument constructor; a hard-linked seven-arg call is {@link NoSuchMethodError}.
 */
public final class BlockPlaceAccess {

    private static final @Nullable Constructor<BlockPlaceEvent> WITH_HAND = resolveHandCtor();

    private BlockPlaceAccess() {}

    public static boolean placementAllowed(
            Block block, BlockState replacedState, Material placementItem, Player placer) {
        ItemStack stack = new ItemStack(placementItem);
        BlockPlaceEvent event;
        if (WITH_HAND != null) {
            try {
                Object hand = EquipmentSlotAccess.hasBukkitClass()
                        ? Class.forName("org.bukkit.inventory.EquipmentSlot").getField("HAND").get(null)
                        : null;
                event =
                        WITH_HAND.newInstance(
                                block, replacedState, block.getRelative(0, -1, 0), stack, placer, Boolean.TRUE, hand);
            } catch (ReflectiveOperationException ignored) {
                event = legacyEvent(block, replacedState, stack, placer);
            }
        } else {
            event = legacyEvent(block, replacedState, stack, placer);
        }
        Bukkit.getPluginManager().callEvent(event);
        return !event.isCancelled() && event.canBuild();
    }

    @SuppressWarnings("removal")
    private static BlockPlaceEvent legacyEvent(
            Block block, BlockState replacedState, ItemStack stack, Player placer) {
        return new BlockPlaceEvent(block, replacedState, block.getRelative(0, -1, 0), stack, placer, true);
    }

    private static @Nullable Constructor<BlockPlaceEvent> resolveHandCtor() {
        try {
            Class<?> slot = Class.forName("org.bukkit.inventory.EquipmentSlot");
            return BlockPlaceEvent.class.getConstructor(
                    Block.class,
                    BlockState.class,
                    Block.class,
                    ItemStack.class,
                    Player.class,
                    boolean.class,
                    slot);
        } catch (ClassNotFoundException | NoSuchMethodException ignored) {
            return null;
        }
    }
}
