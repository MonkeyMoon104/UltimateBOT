package it.coralmc.sandbox.bot.ai.controllers.totem.interf;

import it.coralmc.sandbox.bot.ai.controllers.totem.TotemEquipmentState;
import it.coralmc.sandbox.bot.ai.controllers.totem.TotemState;
import net.minecraft.world.item.ItemStack;

public interface ITotemStateAnalyzer {
    TotemState getTotemState(int totemCount);
    TotemEquipmentState analyzeCurrentEquipment(ItemStack offhand, ItemStack mainhand);
}