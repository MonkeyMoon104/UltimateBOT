package com.monkey.ultimatebot.bot.ai.controllers.inventory.helper;

import com.monkey.ultimatebot.bot.ai.controllers.inventory.helper.inter.IItemChecker;
import com.monkey.ultimatebot.access.combat.CombatSwordAccess;
import com.monkey.ultimatebot.access.item.ItemStackAccess;
import com.monkey.ultimatebot.utils.material.MaterialCatalog;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

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
        return currentSlot == SWORD_SLOT
                && CombatSwordAccess.isKitSword(
                        getCurrentItem(currentSlot, hotbarSlots).getType());
    }

    @Override
    public boolean isHoldingEnderpearl(int currentSlot, Map<Integer, ItemStack> hotbarSlots) {
        return currentSlot == ENDERPEARL_SLOT
                && getCurrentItem(currentSlot, hotbarSlots).getType() == Material.ENDER_PEARL;
    }

    @Override
    public boolean isHoldingObsidian(int currentSlot, Map<Integer, ItemStack> hotbarSlots) {
        return currentSlot == OBSIDIAN_SLOT
                && getCurrentItem(currentSlot, hotbarSlots).getType() == Material.OBSIDIAN;
    }

    @Override
    public boolean isHoldingCrystal(int currentSlot, Map<Integer, ItemStack> hotbarSlots) {
        return currentSlot == CRYSTAL_SLOT
                && MaterialCatalog.is(getCurrentItem(currentSlot, hotbarSlots).getType(), "END_CRYSTAL");
    }

    @Override
    public boolean isHoldingAnchor(int currentSlot, Map<Integer, ItemStack> hotbarSlots) {
        return currentSlot == ANCHOR_SLOT
                && MaterialCatalog.is(getCurrentItem(currentSlot, hotbarSlots).getType(), "RESPAWN_ANCHOR");
    }

    @Override
    public boolean isHoldingGlow(int currentSlot, Map<Integer, ItemStack> hotbarSlots) {
        return currentSlot == GLOW_SLOT
                && getCurrentItem(currentSlot, hotbarSlots).getType() == Material.GLOWSTONE;
    }

    @Override
    public boolean isHoldingGoldenApple(int currentSlot, Map<Integer, ItemStack> hotbarSlots) {
        return currentSlot == GOLDEN_APPLE_SLOT
                && getCurrentItem(currentSlot, hotbarSlots).getType() == Material.GOLDEN_APPLE;
    }

    @Override
    public boolean hasEnderpearls(Map<Integer, ItemStack> hotbarSlots, boolean infiniteResources) {
        ItemStack enderpearlStack = hotbarSlots.get(ENDERPEARL_SLOT);
        if (infiniteResources) {
            return true;
        }
        return enderpearlStack != null && !ItemStackAccess.isEmpty(enderpearlStack) && enderpearlStack.getAmount() > 0;
    }

    @Override
    public int getItemCount(Map<Integer, ItemStack> hotbarSlots, Material material, boolean infiniteResources) {
        if (infiniteResources) {
            if (material == Material.OBSIDIAN
                    || MaterialCatalog.is(material, "END_CRYSTAL")
                    || MaterialCatalog.is(material, "RESPAWN_ANCHOR")
                    || material == Material.GLOWSTONE
                    || material == Material.GOLDEN_APPLE) {
                return 64;
            } else if (material == Material.ENDER_PEARL) {
                return 16;
            }
        }

        for (ItemStack stack : hotbarSlots.values()) {
            if (stack != null && stack.getType() == material) {
                return stack.getAmount();
            }
        }
        return 0;
    }

    @Override
    public boolean hasItem(Map<Integer, ItemStack> hotbarSlots, Material material, boolean infiniteResources) {
        return getItemCount(hotbarSlots, material, infiniteResources) > 0;
    }

    private ItemStack getCurrentItem(int currentSlot, Map<Integer, ItemStack> hotbarSlots) {
        return hotbarSlots.getOrDefault(currentSlot, ItemStackAccess.empty());
    }
}
