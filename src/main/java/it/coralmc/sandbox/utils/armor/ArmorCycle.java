package it.coralmc.sandbox.utils.armor;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class ArmorCycle {

	private static final List<String> armorTypes = List.of("LEATHER", "IRON", "GOLDEN", "DIAMOND", "NETHERITE");

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

	public static Map<EquipmentSlot, ItemStack> getDefaultArmorFromConfig(FileConfiguration config, Plugin plugin) {
		Map<EquipmentSlot, ItemStack> defaultArmor = new EnumMap<>(EquipmentSlot.class);

		defaultArmor.put(EquipmentSlot.HEAD, new ItemStack(getMaterialFromConfig(config, "helmet", plugin)));
		defaultArmor.put(EquipmentSlot.CHEST, new ItemStack(getMaterialFromConfig(config, "chestplate", plugin)));
		defaultArmor.put(EquipmentSlot.LEGS, new ItemStack(getMaterialFromConfig(config, "leggings", plugin)));
		defaultArmor.put(EquipmentSlot.FEET, new ItemStack(getMaterialFromConfig(config, "boots", plugin)));

		return defaultArmor;
	}

	public static Material getMaterialFromConfig(FileConfiguration config, String key, Plugin plugin) {
		String matName = config.getString("gui.default-armor." + key, "NETHERITE");
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
			plugin.getLogger().warning("Materiale armatura non valido (" + matName + suffix + "): " + e);
			return Material.AIR;
		}
	}
}
