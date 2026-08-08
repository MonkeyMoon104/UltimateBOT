package com.monkey.ultimatebot.bot.ai.controllers.totem.helper.interf;

import com.monkey.ultimatebot.bot.ai.controllers.totem.helper.TotemEquipmentState;
import com.monkey.ultimatebot.bot.ai.controllers.totem.helper.TotemState;
import org.bukkit.inventory.ItemStack;

public interface ITotemStateAnalyzer {
    TotemState getTotemState(int totemCount);

    TotemEquipmentState analyzeCurrentEquipment(ItemStack offhand, ItemStack mainhand);
}
