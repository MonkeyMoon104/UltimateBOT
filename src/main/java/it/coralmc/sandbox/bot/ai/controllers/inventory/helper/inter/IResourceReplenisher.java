package it.coralmc.sandbox.bot.ai.controllers.inventory.helper.inter;

import net.minecraft.world.item.ItemStack;
import java.util.Map;

public interface IResourceReplenisher {

    void replenishItem(Map<Integer, ItemStack> hotbarSlots, int slot);

    void onItemUsed(Map<Integer, ItemStack> hotbarSlots, int slot);

    void replenishAllItems(Map<Integer, ItemStack> hotbarSlots);

    void setInfiniteResources(boolean infinite);

    boolean hasInfiniteResources();
}