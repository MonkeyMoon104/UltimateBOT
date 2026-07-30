package com.monkey.mcbot.bot.ai.controllers.inventory.helper.inter;

import java.util.Map;
import net.minecraft.world.item.ItemStack;

public interface IResourceReplenisher {

    void replenishItem(Map<Integer, ItemStack> hotbarSlots, int slot);

    void onItemUsed(Map<Integer, ItemStack> hotbarSlots, int slot);

    void replenishAllItems(Map<Integer, ItemStack> hotbarSlots);

    void setInfiniteResources(boolean infinite);

    boolean hasInfiniteResources();
}
