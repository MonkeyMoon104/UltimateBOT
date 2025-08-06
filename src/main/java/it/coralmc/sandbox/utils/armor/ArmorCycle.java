package it.coralmc.sandbox.utils.armor;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.EquipmentSlot;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class ArmorCycle {

    private static List<String> armorTypes = List.of("LEATHER", "IRON", "GOLDEN", "DIAMOND", "NETHERITE");

    public static Material getNextArmor(Material current, EquipmentSlot slot) {
        String slotSuffix = switch (slot) {
            case HEAD -> "_HELMET";
            case CHEST -> "_CHESTPLATE";
            case LEGS -> "_LEGGINGS";
            case FEET -> "_BOOTS";
            default -> "";
        };

        String currentBase = current.name().replace(slotSuffix, "");
        int index = armorTypes.indexOf(currentBase);
        int nextIndex = (index + 1) % armorTypes.size();
        return Material.valueOf(armorTypes.get(nextIndex) + slotSuffix);
    }

    public static Map<EquipmentSlot, Material> getDefaultArmorFromConfig(FileConfiguration config) {
        Map<EquipmentSlot, Material> defaultArmor = new EnumMap<>(EquipmentSlot.class);

        defaultArmor.put(EquipmentSlot.HEAD, getMaterialFromConfig(config, "helmet"));
        defaultArmor.put(EquipmentSlot.CHEST, getMaterialFromConfig(config, "chestplate"));
        defaultArmor.put(EquipmentSlot.LEGS, getMaterialFromConfig(config, "leggings"));
        defaultArmor.put(EquipmentSlot.FEET, getMaterialFromConfig(config, "boots"));

        return defaultArmor;
    }

    public static Material getMaterialFromConfig(FileConfiguration config, String key) {
        String matName = config.getString("default-armor." + key, "NETHERITE");
        String suffix = switch (key) {
            case "helmet" -> "_HELMET";
            case "chestplate" -> "_CHESTPLATE";
            case "leggings" -> "_LEGGINGS";
            case "boots" -> "_BOOTS";
            default -> "";
        };

        try {
            return Material.valueOf(matName + suffix);
        } catch (IllegalArgumentException e) {
            Bukkit.getLogger().warning("Materiale armatura non valido: " + matName + suffix);
            return Material.AIR;
        }
    }
}
