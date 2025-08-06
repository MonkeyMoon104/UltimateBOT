package it.coralmc.sandbox.utils.gui;

import org.bukkit.inventory.EquipmentSlot;

import java.util.EnumMap;
import java.util.Map;

public class GUISlotHandler {

    public static EquipmentSlot getEquipmentSlotFromGUISlot(int guiSlot) {
        return switch (guiSlot) {
            case 0 -> EquipmentSlot.HEAD;
            case 1 -> EquipmentSlot.CHEST;
            case 2 -> EquipmentSlot.LEGS;
            case 3 -> EquipmentSlot.FEET;
            default -> null;
        };
    }

    public static int getGUISlotFromEquipmentSlot(EquipmentSlot equipmentSlot) {
        return switch (equipmentSlot) {
            case HEAD -> 0;
            case CHEST -> 1;
            case LEGS -> 2;
            case FEET -> 3;
            default -> -1;
        };
    }

    public static boolean isArmorSlot(int guiSlot) {
        return guiSlot >= 0 && guiSlot <= 3;
    }

    public static boolean isFollowButton(int guiSlot) {
        return guiSlot == 7;
    }

    public static boolean isSpawnButton(int guiSlot) {
        return guiSlot == 8;
    }

    public static boolean isSaveButton(int guiSlot) {
        return guiSlot == 9;
    }

    public static Map<EquipmentSlot, Integer> createArmorSlotMapping() {
        Map<EquipmentSlot, Integer> mapping = new EnumMap<>(EquipmentSlot.class);
        mapping.put(EquipmentSlot.HEAD, 0);
        mapping.put(EquipmentSlot.CHEST, 1);
        mapping.put(EquipmentSlot.LEGS, 2);
        mapping.put(EquipmentSlot.FEET, 3);
        return mapping;
    }
}