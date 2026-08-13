package com.monkey.ultimatebot.bot.ai.controllers.inventory.helper;

import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.bot.ai.controllers.inventory.helper.inter.IEquipmentBroadcaster;
import com.monkey.ultimatebot.bot.ai.controllers.inventory.helper.inter.IItemManager;
import com.monkey.ultimatebot.bot.ai.controllers.inventory.helper.inter.IResourceReplenisher;
import com.monkey.ultimatebot.bot.ai.controllers.inventory.helper.inter.ISlotManager;
import com.monkey.ultimatebot.compat.ItemStackAccess;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class ItemManager implements IItemManager {

    public static final int ENDERPEARL_SLOT = 1;
    public static final int TOTEM_SLOT = 2;

    private final ITrainingBot bot;
    private final Map<Integer, ItemStack> hotbarSlots;
    private final IResourceReplenisher resourceReplenisher;
    private final IEquipmentBroadcaster equipmentBroadcaster;
    private final ISlotManager slotManager;

    public ItemManager(
            ITrainingBot bot,
            Map<Integer, ItemStack> hotbarSlots,
            IResourceReplenisher resourceReplenisher,
            IEquipmentBroadcaster equipmentBroadcaster,
            ISlotManager slotManager) {
        this.bot = bot;
        this.hotbarSlots = hotbarSlots;
        this.resourceReplenisher = resourceReplenisher;
        this.equipmentBroadcaster = equipmentBroadcaster;
        this.slotManager = slotManager;
    }

    @Override
    public void addEnderpearls(int count) {
        ItemStack enderpearlStack = hotbarSlots.get(ENDERPEARL_SLOT);
        if (enderpearlStack == null || ItemStackAccess.isEmpty(enderpearlStack)) {
            hotbarSlots.put(ENDERPEARL_SLOT, new ItemStack(Material.ENDER_PEARL, count));
        } else {
            enderpearlStack.setAmount(enderpearlStack.getAmount() + count);
        }
    }

    @Override
    public void updateTotemSlot(ItemStack totemStack) {
        hotbarSlots.put(TOTEM_SLOT, totemStack);
    }

    @Override
    public void onItemUsed(int slot) {
        resourceReplenisher.onItemUsed(hotbarSlots, slot);
        if (slotManager.getCurrentSlot() != slot) {
            return;
        }
        ItemStack stack = hotbarSlots.get(slot);
        if (stack == null || ItemStackAccess.isEmpty(stack)) {
            return;
        }
        ItemStack hand = bot.getItem(EquipmentSlot.HAND);
        if (hand.getType() != stack.getType()) {
            bot.setItem(EquipmentSlot.HAND, stack);
        }
    }
}
