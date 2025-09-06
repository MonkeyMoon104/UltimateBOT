package it.coralmc.sandbox.bot.ai.controllers.totem.helper.interf;

import it.coralmc.sandbox.bot.ai.controllers.totem.helper.TotemEquipmentState;
import it.coralmc.sandbox.bot.ai.controllers.totem.helper.TotemState;
import net.minecraft.world.item.ItemStack;

public interface ITotemStateAnalyzer {
    TotemState getTotemState(int totemCount);
    TotemEquipmentState analyzeCurrentEquipment(ItemStack offhand, ItemStack mainhand);
}