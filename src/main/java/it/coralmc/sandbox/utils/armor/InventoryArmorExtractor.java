package it.coralmc.sandbox.utils.armor;

import it.coralmc.sandbox.utils.gui.GUISlotHandler;
import org.bukkit.Material;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.EnumMap;
import java.util.Map;

public class InventoryArmorExtractor {

	private final GUISlotHandler guiSlotHandler;

	public InventoryArmorExtractor(GUISlotHandler guiSlotHandler) {
		this.guiSlotHandler = guiSlotHandler;
	}
	public Map<EquipmentSlot, ItemStack> extractArmorFromGUI(Inventory inventory) {
		Map<EquipmentSlot, ItemStack> selectedArmor = new EnumMap<>(EquipmentSlot.class);

		for (EquipmentSlot armorSlot : EquipmentSlot.values()) {
			if (armorSlot == EquipmentSlot.HAND || armorSlot == EquipmentSlot.OFF_HAND) {
				continue;
			}

			int guiSlot = guiSlotHandler.getGUISlotFromEquipmentSlot(armorSlot);
			if (guiSlot == -1) continue;

			ItemStack item = inventory.getItem(guiSlot);
			if (item != null && item.getType() != Material.AIR) {
				selectedArmor.put(armorSlot, item);
			}
		}

		return selectedArmor;
	}

	public boolean isValidArmorItem(ItemStack item) {
		return item != null && item.getType() != Material.AIR;
	}

	public Material extractSingleArmorPiece(Inventory inventory, int guiSlot) {
		ItemStack item = inventory.getItem(guiSlot);
		if (isValidArmorItem(item)) {
			return item.getType();
		}
		return Material.AIR;
	}

	public boolean hasCompleteArmor(Inventory inventory) {
		for (int slot = 0; slot <= 3; slot++) {
			ItemStack item = inventory.getItem(slot);
			if (!isValidArmorItem(item)) {
				return false;
			}
		}
		return true;
	}
}