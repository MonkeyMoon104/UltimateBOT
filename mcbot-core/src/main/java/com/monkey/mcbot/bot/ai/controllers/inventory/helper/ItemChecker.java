package com.monkey.mcbot.bot.ai.controllers.inventory.helper;

import com.monkey.mcbot.bot.ai.controllers.inventory.helper.inter.IItemChecker;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import java.util.Map;

public class ItemChecker implements IItemChecker {

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
    public boolean isHoldingSword(int currentSlot, Map<Integer, ItemStack> hotbarSlots) {
        return currentSlot == SWORD_SLOT && getCurrentItem(currentSlot, hotbarSlots).getItem() == Items.NETHERITE_SWORD;
    }

    @Override
    public boolean isHoldingEnderpearl(int currentSlot, Map<Integer, ItemStack> hotbarSlots) {
        return currentSlot == ENDERPEARL_SLOT && getCurrentItem(currentSlot, hotbarSlots).getItem() == Items.ENDER_PEARL;
    }

    @Override
    public boolean isHoldingObsidian(int currentSlot, Map<Integer, ItemStack> hotbarSlots) {
        return currentSlot == OBSIDIAN_SLOT && getCurrentItem(currentSlot, hotbarSlots).getItem() == Items.OBSIDIAN;
    }

    @Override
    public boolean isHoldingCrystal(int currentSlot, Map<Integer, ItemStack> hotbarSlots) {
        return currentSlot == CRYSTAL_SLOT && getCurrentItem(currentSlot, hotbarSlots).getItem() == Items.END_CRYSTAL;
    }

    @Override
    public boolean isHoldingAnchor(int currentSlot, Map<Integer, ItemStack> hotbarSlots) {
        return currentSlot == ANCHOR_SLOT && getCurrentItem(currentSlot, hotbarSlots).getItem() == Items.RESPAWN_ANCHOR;
    }

    @Override
    public boolean isHoldingGlow(int currentSlot, Map<Integer, ItemStack> hotbarSlots) {
        return currentSlot == GLOW_SLOT && getCurrentItem(currentSlot, hotbarSlots).getItem() == Items.GLOWSTONE;
    }

    @Override
    public boolean isHoldingGoldenApple(int currentSlot, Map<Integer, ItemStack> hotbarSlots) {
        return currentSlot == GOLDEN_APPLE_SLOT && getCurrentItem(currentSlot, hotbarSlots).getItem() == Items.GOLDEN_APPLE;
    }

    @Override
    public boolean hasEnderpearls(Map<Integer, ItemStack> hotbarSlots, boolean infiniteResources) {
        ItemStack enderpearlStack = hotbarSlots.get(ENDERPEARL_SLOT);
        if (infiniteResources) {
            return true;
        }
        return enderpearlStack != null && !enderpearlStack.isEmpty() && enderpearlStack.getCount() > 0;
    }

    @Override
    public int getItemCount(Map<Integer, ItemStack> hotbarSlots, Item item, boolean infiniteResources) {
        if (infiniteResources) {
            if (item == Items.OBSIDIAN || item == Items.END_CRYSTAL) {
                return 64;
            } else if (item == Items.ENDER_PEARL) {
                return 16;
            }
        }

        for (ItemStack stack : hotbarSlots.values()) {
            if (stack.getItem() == item) {
                return stack.getCount();
            }
        }
        return 0;
    }

    @Override
    public boolean hasItem(Map<Integer, ItemStack> hotbarSlots, Item item, boolean infiniteResources) {
        return getItemCount(hotbarSlots, item, infiniteResources) > 0;
    }

    private ItemStack getCurrentItem(int currentSlot, Map<Integer, ItemStack> hotbarSlots) {
        return hotbarSlots.getOrDefault(currentSlot, ItemStack.EMPTY);
    }
}