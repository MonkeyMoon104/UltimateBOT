package com.monkey.ultimatebot.bot.ai.fakeplayer.internal;

import io.papermc.paper.entity.PlayerGiveResult;
import java.util.Collection;
import java.util.Collections;
import org.bukkit.inventory.ItemStack;

public final class InventoryDelegate {
    public PlayerGiveResult give(Collection<ItemStack> items) {
        Collection<ItemStack> leftovers = com.monkey.ultimatebot.common.util.ImmutableCollections.copyOf(items);
        return new PlayerGiveResult() {
            @Override
            public Collection<ItemStack> leftovers() {
                return leftovers;
            }

            @Override
            public Collection<org.bukkit.entity.Item> drops() {
                return Collections.emptyList();
            }
        };
    }

    public void updateInventory() {}
}
