package com.monkey.ultimatebot.gui.legacy;

import com.monkey.ultimatebot.common.model.EquipmentSlotKind;
import com.monkey.ultimatebot.gui.LegacyBotGui;
import com.monkey.ultimatebot.gui.combat.CombatTuningProperty;
import java.util.UUID;
import org.bukkit.inventory.ItemStack;

public final class LegacyGuiItemFactory {

    private final LegacyBotGui gui;

    public LegacyGuiItemFactory(LegacyBotGui gui) {
        this.gui = gui;
    }

    public ItemStack tabItem(int tab, String name, ItemStack icon) {
        return gui.tabItem(tab, name, icon);
    }

    public ItemStack difficultyItem() {
        return gui.difficultyItem();
    }

    public ItemStack totemItem() {
        return gui.totemItem();
    }

    public ItemStack spawnItem() {
        return gui.spawnItem();
    }

    public ItemStack teleportItem() {
        return gui.teleportItem();
    }

    public ItemStack followItem() {
        return gui.followItem();
    }

    public ItemStack combatToggleItem() {
        return gui.combatToggleItem();
    }

    public ItemStack combatModeItem() {
        return gui.combatModeItem();
    }

    public ItemStack targetModeItem() {
        return gui.targetModeItem();
    }

    public ItemStack armorPieceItem(EquipmentSlotKind slot) {
        return gui.armorPieceItem(slot);
    }

    public ItemStack headItem(UUID uuid, String colorPrefix, String role) {
        return gui.headItem(uuid, colorPrefix, role);
    }

    public ItemStack resetTuningItem() {
        return gui.resetTuningItem();
    }

    public ItemStack tuningItem(CombatTuningProperty property) {
        return gui.tuningItem(property);
    }
}
