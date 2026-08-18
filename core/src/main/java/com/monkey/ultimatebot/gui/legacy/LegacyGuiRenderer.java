package com.monkey.ultimatebot.gui.legacy;

import com.monkey.ultimatebot.gui.LegacyBotGui;

public final class LegacyGuiRenderer {

    private final LegacyBotGui gui;

    public LegacyGuiRenderer(LegacyBotGui gui) {
        this.gui = gui;
    }

    public void render() {
        gui.getInventory().clear();
        gui.fillChrome();
        gui.placeTabs();
        if (gui.isKitTabActive()) {
            gui.renderKit();
        } else if (gui.isArmorTabActive()) {
            gui.renderArmor();
        } else if (gui.isOwnersTabActive()) {
            gui.renderOwners();
        } else if (gui.isTargetsTabActive()) {
            gui.renderTargets();
        } else {
            gui.renderCombat();
        }
    }
}
