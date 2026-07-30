package com.monkey.mcbot.placeholders;

import org.bukkit.entity.Player;

public interface IBotPlaceholder {
    String getIdentifier();

    String getValue(Player player);

    default String getDefaultValue() {
        return "N/A";
    }
}
