package com.monkey.ultimatebot.gui.legacy;

import com.monkey.ultimatebot.gui.LegacyBotGui;
import org.bukkit.event.inventory.ClickType;

public final class LegacyGuiActionHandler {

    private final LegacyBotGui gui;

    public LegacyGuiActionHandler(LegacyBotGui gui) {
        this.gui = gui;
    }

    public void handleClick(int slot, ClickType click) {
        if (gui.tryHandleTabSelection(slot)) {
            gui.render();
            return;
        }

        if (gui.isKitTabActive()) {
            gui.handleKitClick(slot, click);
        } else if (gui.isArmorTabActive()) {
            gui.handleArmorClick(slot, click);
        } else if (gui.isCombatTabActive()) {
            gui.handleCombatClick(slot, click);
        }
    }
}
