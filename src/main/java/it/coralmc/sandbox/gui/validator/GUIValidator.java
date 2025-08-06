package it.coralmc.sandbox.gui.validator;

import org.bukkit.event.inventory.InventoryClickEvent;

public class GUIValidator {

    private static final String BOT_SETTINGS_GUI_TITLE = "§6Bot Settings";

    public static boolean isBotSettingsGUI(InventoryClickEvent event) {
        return event.getView().getTitle().equals(BOT_SETTINGS_GUI_TITLE);
    }

    public static String getBotSettingsGUITitle() {
        return BOT_SETTINGS_GUI_TITLE;
    }

    public static boolean isValidSlot(int slot, int minSlot, int maxSlot) {
        return slot >= minSlot && slot <= maxSlot;
    }

    public static boolean isValidClick(InventoryClickEvent event) {
        return event != null &&
                event.getClickedInventory() != null &&
                event.getCurrentItem() != null;
    }
}