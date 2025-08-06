package it.coralmc.sandbox.utils.armor;

import it.coralmc.sandbox.utils.gui.GUISlotHandler;
import org.bukkit.Material;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.EnumMap;
import java.util.Map;

public class InventoryArmorExtractor {
    public static Map<EquipmentSlot, Material> extractArmorFromGUI(Inventory inventory) {
        Map<EquipmentSlot, Material> selectedArmor = new EnumMap<>(EquipmentSlot.class);

        for (EquipmentSlot armorSlot : EquipmentSlot.values()) {
            if (armorSlot == EquipmentSlot.HAND || armorSlot == EquipmentSlot.OFF_HAND) {
                continue;
            }

            int guiSlot = GUISlotHandler.getGUISlotFromEquipmentSlot(armorSlot);
            if (guiSlot == -1) continue;

            ItemStack item = inventory.getItem(guiSlot);
            if (item != null && item.getType() != Material.AIR) {
                selectedArmor.put(armorSlot, item.getType());
            }
        }

        return selectedArmor;
    }

    public static boolean isValidArmorItem(ItemStack item) {
        return item != null && item.getType() != Material.AIR;
    }

    public static Material extractSingleArmorPiece(Inventory inventory, int guiSlot) {
        ItemStack item = inventory.getItem(guiSlot);
        if (isValidArmorItem(item)) {
            return item.getType();
        }
        return Material.AIR;
    }

    public static boolean hasCompleteArmor(Inventory inventory) {
        for (int slot = 0; slot <= 3; slot++) {
            ItemStack item = inventory.getItem(slot);
            if (!isValidArmorItem(item)) {
                return false;
            }
        }
        return true;
    }
}