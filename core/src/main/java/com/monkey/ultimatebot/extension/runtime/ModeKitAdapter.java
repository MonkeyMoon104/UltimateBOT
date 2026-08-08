package com.monkey.ultimatebot.extension.runtime;

import com.monkey.ultimatebot.combat.mode.runtime.ModeKit;

public final class ModeKitAdapter {
    private ModeKitAdapter() {}

    public static ModeKit toInternal(com.monkey.ultimatebot.api.extension.combat.ModeKit kit) {
        ModeKit.Builder builder = ModeKit.builder();
        kit.inventory().forEach((slot, item) -> {
            if (!item.isEmpty()) {
                builder.slot(slot, item);
            }
        });
        kit.equipment().forEach((slot, item) -> {
            if (!item.isEmpty()) {
                builder.equipment(slot, item);
            }
        });
        return builder.build();
    }
}
