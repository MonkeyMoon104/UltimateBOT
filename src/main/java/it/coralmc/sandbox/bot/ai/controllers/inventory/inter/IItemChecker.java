package it.coralmc.sandbox.bot.ai.controllers.inventory.inter;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import java.util.Map;

public interface IItemChecker {

    boolean isHoldingSword(int currentSlot, Map<Integer, ItemStack> hotbarSlots);

    boolean isHoldingEnderpearl(int currentSlot, Map<Integer, ItemStack> hotbarSlots);

    boolean isHoldingObsidian(int currentSlot, Map<Integer, ItemStack> hotbarSlots);

    boolean isHoldingCrystal(int currentSlot, Map<Integer, ItemStack> hotbarSlots);

    boolean isHoldingAnchor(int currentSlot, Map<Integer, ItemStack> hotbarSlots);

    boolean isHoldingGlow(int currentSlot, Map<Integer, ItemStack> hotbarSlots);

    boolean hasEnderpearls(Map<Integer, ItemStack> hotbarSlots, boolean infiniteResources);

    int getItemCount(Map<Integer, ItemStack> hotbarSlots, Item item, boolean infiniteResources);

    boolean hasItem(Map<Integer, ItemStack> hotbarSlots, Item item, boolean infiniteResources);
}