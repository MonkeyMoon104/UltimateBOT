package it.coralmc.sandbox.bot.ai.controllers.totem.interf;

import it.coralmc.sandbox.bot.ai.controllers.totem.TotemEquipmentState;

public interface ITotemStrategyHandler {
    void handleUnlimitedTotems(TotemEquipmentState equipmentState, boolean isCombat);
    void handleNoTotems(TotemEquipmentState equipmentState);
    void handleOneTotem(TotemEquipmentState equipmentState, boolean isCombat);
    void handleMultipleTotems(int totemCount, TotemEquipmentState equipmentState, boolean isCombat);
}