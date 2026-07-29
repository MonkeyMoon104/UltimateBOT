package com.monkey.mcbot.bot.ai.controllers.inventory.helper;

import com.monkey.mcbot.bot.ai.controllers.inventory.helper.inter.IResourceReplenisher;
import java.util.Map;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ResourceReplenisher implements IResourceReplenisher {

    private boolean infiniteResources = true;

    public static final int SWORD_SLOT = 0;
    public static final int ENDERPEARL_SLOT = 1;
    public static final int TOTEM_SLOT = 2;
    public static final int OBSIDIAN_SLOT = 3;
    public static final int CRYSTAL_SLOT = 4;
    public static final int ANCHOR_SLOT = 5;
    public static final int GLOW_SLOT = 6;
    public static final int GOLDEN_APPLE_SLOT = 7;
    public static final int EMPTY_SLOT = 8;

    @Override
    public void replenishItem(Map<Integer, ItemStack> hotbarSlots, int slot) {
        if (!infiniteResources) return;

        ItemStack currentStack = hotbarSlots.get(slot);
        if (currentStack == null || currentStack.isEmpty()) return;

        switch (slot) {
            case OBSIDIAN_SLOT -> refill(currentStack, Items.OBSIDIAN, 64);
            case CRYSTAL_SLOT -> refill(currentStack, Items.END_CRYSTAL, 64);
            case ENDERPEARL_SLOT -> refill(currentStack, Items.ENDER_PEARL, 16);
            case ANCHOR_SLOT -> refill(currentStack, Items.RESPAWN_ANCHOR, 64);
            case GLOW_SLOT -> refill(currentStack, Items.GLOWSTONE, 64);
            case GOLDEN_APPLE_SLOT -> refill(currentStack, Items.GOLDEN_APPLE, 64);
            default -> {}
        }
    }

    @Override
    public void onItemUsed(Map<Integer, ItemStack> hotbarSlots, int slot) {
        replenishItem(hotbarSlots, slot);
    }

    @Override
    public void replenishAllItems(Map<Integer, ItemStack> hotbarSlots) {
        for (int slot : hotbarSlots.keySet()) {
            replenishItem(hotbarSlots, slot);
        }
    }

    @Override
    public void setInfiniteResources(boolean infinite) {
        this.infiniteResources = infinite;
    }

    @Override
    public boolean hasInfiniteResources() {
        return infiniteResources;
    }

    private static void refill(ItemStack stack, net.minecraft.world.item.Item expectedItem, int count) {
        if (expectedItem.equals(stack.getItem()) && stack.getCount() < count) {
            stack.setCount(count);
        }
    }
}
