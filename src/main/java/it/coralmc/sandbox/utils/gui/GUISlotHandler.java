package it.coralmc.sandbox.utils.gui;

import org.bukkit.inventory.EquipmentSlot;

import java.util.EnumMap;
import java.util.Map;

public class GUISlotHandler {

	public static EquipmentSlot getEquipmentSlotFromGUISlot(int guiSlot) {
		return switch (guiSlot) {
			case 20 -> EquipmentSlot.HEAD;
			case 21 -> EquipmentSlot.CHEST;
			case 23 -> EquipmentSlot.LEGS;
			case 24 -> EquipmentSlot.FEET;
			default -> null;
		};
	}

	public static int getGUISlotFromEquipmentSlot(EquipmentSlot equipmentSlot) {
		return switch (equipmentSlot) {
			case HEAD -> 20;
			case CHEST -> 21;
			case LEGS -> 23;
			case FEET -> 24;
			default -> -1;
		};
	}

	public static boolean isArmorSlot(int guiSlot) {
		return guiSlot == 20 || guiSlot == 21 || guiSlot == 23 || guiSlot == 24;
	}

	public static boolean isFollowButton(int guiSlot) {
		return guiSlot == 41;
	}

	public static boolean isSpawnButton(int guiSlot) {
		return guiSlot == 39;
	}

	public static boolean isTotemButton(int guiSlot) {
		return guiSlot == 22;
	}


	public static Map<EquipmentSlot, Integer> createArmorSlotMapping() {
		Map<EquipmentSlot, Integer> mapping = new EnumMap<>(EquipmentSlot.class);
		mapping.put(EquipmentSlot.HEAD, 20);
		mapping.put(EquipmentSlot.CHEST, 21);
		mapping.put(EquipmentSlot.LEGS, 23);
		return mapping;
	}
}