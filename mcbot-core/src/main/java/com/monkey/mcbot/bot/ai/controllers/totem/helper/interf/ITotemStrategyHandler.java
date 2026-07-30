package com.monkey.mcbot.bot.ai.controllers.totem.helper.interf;

import com.monkey.mcbot.bot.ai.controllers.totem.helper.TotemEquipmentState;

public interface ITotemStrategyHandler {
    void handleUnlimitedTotems(TotemEquipmentState equipmentState, boolean isCombat);

    void handleNoTotems(TotemEquipmentState equipmentState);

    void handleOneTotem(TotemEquipmentState equipmentState, boolean isCombat);

    void handleMultipleTotems(int totemCount, TotemEquipmentState equipmentState, boolean isCombat);
}
