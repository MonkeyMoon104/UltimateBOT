package com.monkey.ultimatebot.utils.equipment;

import com.monkey.ultimatebot.common.model.EquipmentSlotKind;

public class EquipmentConverter {

    public static EquipmentSlotKind[] getArmorSlots() {
        return new EquipmentSlotKind[] {
            EquipmentSlotKind.HEAD, EquipmentSlotKind.CHEST, EquipmentSlotKind.LEGS, EquipmentSlotKind.FEET
        };
    }
}
