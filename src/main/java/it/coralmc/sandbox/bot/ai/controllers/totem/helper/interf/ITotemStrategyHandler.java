package it.coralmc.sandbox.bot.ai.controllers.totem.helper.interf;

import it.coralmc.sandbox.bot.ai.controllers.totem.helper.TotemEquipmentState;

public interface ITotemStrategyHandler {
    void handleUnlimitedTotems(TotemEquipmentState equipmentState, boolean isCombat);
    void handleNoTotems(TotemEquipmentState equipmentState);
    void handleOneTotem(TotemEquipmentState equipmentState, boolean isCombat);
    void handleMultipleTotems(int totemCount, TotemEquipmentState equipmentState, boolean isCombat);
}