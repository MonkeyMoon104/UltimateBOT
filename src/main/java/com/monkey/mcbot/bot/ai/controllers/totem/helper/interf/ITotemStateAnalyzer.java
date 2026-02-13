package com.monkey.mcbot.bot.ai.controllers.totem.helper.interf;

import com.monkey.mcbot.bot.ai.controllers.totem.helper.TotemEquipmentState;
import com.monkey.mcbot.bot.ai.controllers.totem.helper.TotemState;
import net.minecraft.world.item.ItemStack;

public interface ITotemStateAnalyzer {
    TotemState getTotemState(int totemCount);
    TotemEquipmentState analyzeCurrentEquipment(ItemStack offhand, ItemStack mainhand);
}