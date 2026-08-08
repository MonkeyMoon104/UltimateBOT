package com.monkey.ultimatebot.bot.ai.controllers.totem.helper;

import com.monkey.ultimatebot.bot.ai.controllers.totem.helper.interf.ITotemInventoryManager;
import com.monkey.ultimatebot.bot.ai.controllers.totem.helper.interf.ITotemStateAnalyzer;
import org.bukkit.inventory.ItemStack;

public class TotemStateAnalyzer implements ITotemStateAnalyzer {
    private final ITotemInventoryManager inventoryManager;

    public TotemStateAnalyzer(ITotemInventoryManager inventoryManager) {
        this.inventoryManager = inventoryManager;
    }

    @Override
    public TotemState getTotemState(int totemCount) {
        if (totemCount == -1) return TotemState.UNLIMITED;
        if (totemCount == 0) return TotemState.NONE;
        if (totemCount == 1) return TotemState.ONE;
        return TotemState.MULTIPLE;
    }

    @Override
    public TotemEquipmentState analyzeCurrentEquipment(ItemStack offhand, ItemStack mainhand) {
        boolean hasOffhandTotem = inventoryManager.hasTotemInSlot(offhand);
        boolean hasMainhandTotem = inventoryManager.hasTotemInSlot(mainhand);
        int equippedTotems = (hasOffhandTotem ? 1 : 0) + (hasMainhandTotem ? 1 : 0);

        return new TotemEquipmentState(hasOffhandTotem, hasMainhandTotem, equippedTotems);
    }
}
