package com.monkey.ultimatebot.bot.ai.controllers.inventory.helper;

import com.monkey.ultimatebot.bot.ai.controllers.inventory.helper.inter.IResourceReplenisher;
import com.monkey.ultimatebot.compat.ItemStackAccess;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

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
        if (currentStack == null || ItemStackAccess.isEmpty(currentStack)) return;

                switch (slot) {
            case OBSIDIAN_SLOT:
                refill(currentStack, Material.OBSIDIAN, 64);
                break;
            case CRYSTAL_SLOT:
                refill(currentStack, Material.END_CRYSTAL, 64);
                break;
            case ENDERPEARL_SLOT:
                refill(currentStack, Material.ENDER_PEARL, 16);
                break;
            case ANCHOR_SLOT:
                refill(currentStack, Material.RESPAWN_ANCHOR, 64);
                break;
            case GLOW_SLOT:
                refill(currentStack, Material.GLOWSTONE, 64);
                break;
            case GOLDEN_APPLE_SLOT:
                refill(currentStack, Material.GOLDEN_APPLE, 64);
                break;
            default:

                break;
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

    private static void refill(ItemStack stack, Material expectedMaterial, int count) {
        if (stack.getType() == expectedMaterial && stack.getAmount() < count) {
            stack.setAmount(count);
        }
    }
}
