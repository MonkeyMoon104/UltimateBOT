package com.monkey.ultimatebot.extension.runtime;

import com.monkey.ultimatebot.combat.mode.runtime.ModeKit;
import com.monkey.ultimatebot.common.model.EquipmentSlotKind;
import com.monkey.ultimatebot.access.item.EquipmentSlotAccess;
import com.monkey.ultimatebot.access.item.ItemStackAccess;

public final class ModeKitAdapter {
    private ModeKitAdapter() {}

    public static ModeKit toInternal(com.monkey.ultimatebot.api.extension.combat.ModeKit kit) {
        ModeKit.Builder builder = ModeKit.builder();
        kit.inventory().forEach((slot, item) -> {
            if (!ItemStackAccess.isEmpty(item)) {
                builder.slot(slot, item);
            }
        });
        kit.equipment().forEach((slot, item) -> {
            if (ItemStackAccess.isEmpty(item)) {
                return;
            }
            EquipmentSlotKind kind = EquipmentSlotAccess.fromBukkit(slot);
            if (kind != null) {
                builder.equipment(kind, item);
            }
        });
        return builder.build();
    }
}
