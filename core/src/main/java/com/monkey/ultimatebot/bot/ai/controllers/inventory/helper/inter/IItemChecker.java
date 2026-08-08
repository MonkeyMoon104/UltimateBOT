package com.monkey.ultimatebot.bot.ai.controllers.inventory.helper.inter;

import java.util.Map;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public interface IItemChecker {

    boolean isHoldingSword(int currentSlot, Map<Integer, ItemStack> hotbarSlots);

    boolean isHoldingEnderpearl(int currentSlot, Map<Integer, ItemStack> hotbarSlots);

    boolean isHoldingObsidian(int currentSlot, Map<Integer, ItemStack> hotbarSlots);

    boolean isHoldingCrystal(int currentSlot, Map<Integer, ItemStack> hotbarSlots);

    boolean isHoldingAnchor(int currentSlot, Map<Integer, ItemStack> hotbarSlots);

    boolean isHoldingGlow(int currentSlot, Map<Integer, ItemStack> hotbarSlots);

    boolean isHoldingGoldenApple(int currentSlot, Map<Integer, ItemStack> hotbarSlots);

    boolean hasEnderpearls(Map<Integer, ItemStack> hotbarSlots, boolean infiniteResources);

    int getItemCount(Map<Integer, ItemStack> hotbarSlots, Material material, boolean infiniteResources);

    boolean hasItem(Map<Integer, ItemStack> hotbarSlots, Material material, boolean infiniteResources);
}
