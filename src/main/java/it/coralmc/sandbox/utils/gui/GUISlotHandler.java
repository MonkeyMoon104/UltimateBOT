package it.coralmc.sandbox.utils.gui;

import org.bukkit.inventory.EquipmentSlot;

import java.util.EnumMap;
import java.util.Map;

public class GUISlotHandler {

	public EquipmentSlot getEquipmentSlotFromGUISlot(int guiSlot) {
		return switch (guiSlot) {
			case 20 -> EquipmentSlot.HEAD;
			case 21 -> EquipmentSlot.CHEST;
			case 23 -> EquipmentSlot.LEGS;
			case 24 -> EquipmentSlot.FEET;
			default -> null;
		};
	}

	public int getGUISlotFromEquipmentSlot(EquipmentSlot equipmentSlot) {
		return switch (equipmentSlot) {
			case HEAD -> 20;
			case CHEST -> 21;
			case LEGS -> 23;
			case FEET -> 24;
			default -> -1;
		};
	}

	public EquipmentSlot getEquipmentSlotFromGlassSlot(int guiSlot) {
		return switch (guiSlot) {
			case 11 -> EquipmentSlot.HEAD;
			case 12 -> EquipmentSlot.CHEST;
			case 14 -> EquipmentSlot.LEGS;
			case 15 -> EquipmentSlot.FEET;
			default -> null;
		};
	}

	public int getGlassSlotFromEquipmentSlot(EquipmentSlot equipmentSlot) {
		return switch (equipmentSlot) {
			case HEAD -> 11;
			case CHEST -> 12;
			case LEGS -> 14;
			case FEET -> 15;
			default -> -1;
		};
	}

	public boolean isArmorSlot(int guiSlot) {
		return guiSlot == 20 || guiSlot == 21 || guiSlot == 23 || guiSlot == 24;
	}

	public boolean isGlassSlot(int guiSlot) {
		return guiSlot == 11 || guiSlot == 12 || guiSlot == 14 || guiSlot == 15;
	}

	public boolean isFollowButton(int guiSlot) {
		return guiSlot == 41;
	}

	public boolean isSpawnButton(int guiSlot) {
		return guiSlot == 39;
	}

	public boolean isTotemButton(int guiSlot) {
		return guiSlot == 22;
	}

	public Map<EquipmentSlot, Integer> createArmorSlotMapping() {
		Map<EquipmentSlot, Integer> mapping = new EnumMap<>(EquipmentSlot.class);
		mapping.put(EquipmentSlot.HEAD, 20);
		mapping.put(EquipmentSlot.CHEST, 21);
		mapping.put(EquipmentSlot.LEGS, 23);
		mapping.put(EquipmentSlot.FEET, 24);
		return mapping;
	}

	public Map<EquipmentSlot, Integer> createGlassSlotMapping() {
		Map<EquipmentSlot, Integer> mapping = new EnumMap<>(EquipmentSlot.class);
		mapping.put(EquipmentSlot.HEAD, 11);
		mapping.put(EquipmentSlot.CHEST, 12);
		mapping.put(EquipmentSlot.LEGS, 14);
		mapping.put(EquipmentSlot.FEET, 15);
		return mapping;
	}
}