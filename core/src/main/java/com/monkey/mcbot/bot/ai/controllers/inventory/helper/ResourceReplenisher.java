package com.monkey.mcbot.bot.ai.controllers.inventory.helper;

import com.monkey.mcbot.bot.ai.controllers.inventory.helper.inter.IResourceReplenisher;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import java.util.Map;

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
            case OBSIDIAN_SLOT:
                if (currentStack.getItem() == Items.OBSIDIAN && currentStack.getCount() < 64) {
                    currentStack.setCount(64);
                }
                break;
            case CRYSTAL_SLOT:
                if (currentStack.getItem() == Items.END_CRYSTAL && currentStack.getCount() < 64) {
                    currentStack.setCount(64);
                }
                break;
            case ENDERPEARL_SLOT:
                if (currentStack.getItem() == Items.ENDER_PEARL && currentStack.getCount() < 16) {
                    currentStack.setCount(16);
                }
                break;
            case ANCHOR_SLOT:
                if (currentStack.getItem() == Items.RESPAWN_ANCHOR && currentStack.getCount() < 64) {
                    currentStack.setCount(64);
                }
                break;
            case GLOW_SLOT:
                if (currentStack.getItem() == Items.GLOWSTONE && currentStack.getCount() < 64) {
                    currentStack.setCount(64);
                }
            case GOLDEN_APPLE_SLOT:
                if (currentStack.getItem() == Items.GOLDEN_APPLE && currentStack.getCount() < 64) {
                    currentStack.setCount(64);
                }
                break;
        }
    }

    @Override
    public void onItemUsed(Map<Integer, ItemStack> hotbarSlots, int slot) {
        if (!infiniteResources) return;

        ItemStack stack = hotbarSlots.get(slot);
        if (stack == null || stack.isEmpty()) return;

        switch (slot) {
            case OBSIDIAN_SLOT:
                if (stack.getItem() == Items.OBSIDIAN) {
                    stack.setCount(64);
                }
                break;
            case CRYSTAL_SLOT:
                if (stack.getItem() == Items.END_CRYSTAL) {
                    stack.setCount(64);
                }
                break;
            case ENDERPEARL_SLOT:
                if (stack.getItem() == Items.ENDER_PEARL) {
                    stack.setCount(16);
                }
                break;
            case ANCHOR_SLOT:
                if (stack.getItem() == Items.RESPAWN_ANCHOR) {
                    stack.setCount(64);
                }
                break;
            case GLOW_SLOT:
                if (stack.getItem() == Items.GLOWSTONE) {
                    stack.setCount(64);
                }
            case GOLDEN_APPLE_SLOT:
                if (stack.getItem() == Items.GOLDEN_APPLE) {
                    stack.setCount(64);
                }
                break;
        }
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
}