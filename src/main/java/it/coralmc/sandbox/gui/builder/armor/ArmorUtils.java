package it.coralmc.sandbox.gui.builder.armor;

import it.coralmc.sandbox.SandboxTraining;
import it.coralmc.sandbox.bot.BotSpawner;
import it.coralmc.sandbox.utils.armor.ArmorCycle;
import it.coralmc.sandbox.utils.armor.PlayerArmorManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

public class ArmorUtils {

	private static final FileConfiguration config = SandboxTraining.getInstance().getConfig();

	public static Map<EquipmentSlot, ItemStack> initializePlayerArmor(UUID playerUUID, Plugin plugin) {
		Map<EquipmentSlot, ItemStack> initialArmor;
		
		if (BotSpawner.isBotSpawned(playerUUID)) {
			Map<EquipmentSlot, ItemStack> botArmor = BotSpawner.getBotArmor(playerUUID);
			if (botArmor != null) {
				initialArmor = new EnumMap<>(botArmor);
				PlayerArmorManager.playerArmorSelections.put(playerUUID, new EnumMap<>(botArmor));
			} else {
				initialArmor = ArmorCycle.getDefaultArmorFromConfig(config, plugin);
			}
		} else {
			initialArmor = ArmorCycle.getDefaultArmorFromConfig(config, plugin);
		}

		return processArmorSlots(initialArmor);
	}

	private static Map<EquipmentSlot, ItemStack> processArmorSlots(Map<EquipmentSlot, ItemStack> initialArmor) {
		Map<EquipmentSlot, ItemStack> processedArmor = new EnumMap<>(EquipmentSlot.class);

		for (EquipmentSlot slot : EquipmentSlot.values()) {
			if (slot == EquipmentSlot.HAND || slot == EquipmentSlot.OFF_HAND) continue;

			String configKey = getConfigKeyForSlot(slot);
			if (configKey == null) continue;

			ItemStack item = initialArmor.get(slot);
			Material material = item.getType();
			if (material == Material.AIR) {
				String fallbackType = config.getString("gui.default-armor." + configKey, "NETHERITE");
				material = getArmorMaterial(fallbackType, slot);

				if (material == Material.AIR) {
					String noArmor = config.getString("gui.default-armor.no-armor", "BARRIER");
					try {
						material = Material.valueOf(noArmor.toUpperCase());
					} catch (IllegalArgumentException e) {
						Bukkit.getLogger().warning("Materiale fallback no-armor non valido: " + noArmor + ", imposto BARRIER");
						material = Material.BARRIER;
					}
				}
			}

			processedArmor.put(slot, item.withType(material));
		}

		return processedArmor;
	}

	public static String getConfigKeyForSlot(EquipmentSlot slot) {
		return switch (slot) {
			case HEAD -> "helmet";
			case CHEST -> "chestplate";
			case LEGS -> "leggings";
			case FEET -> "boots";
			default -> null;
		};
	}

	public static int getSlotIndex(EquipmentSlot slot) {
		return switch (slot) {
			case HEAD -> 20;
			case CHEST -> 21;
			case LEGS -> 23;
			case FEET -> 24;
			default -> -1;
		};
	}

	public static Material getArmorMaterial(String type, EquipmentSlot slot) {
		String suffix = switch (slot) {
			case HEAD -> "_HELMET";
			case CHEST -> "_CHESTPLATE";
			case LEGS -> "_LEGGINGS";
			case FEET -> "_BOOTS";
			default -> "";
		};

		try {
			return Material.valueOf(type + suffix);
		} catch (IllegalArgumentException e) {
			Bukkit.getLogger().warning("Materiale armatura non valido: " + type + suffix);
			return Material.AIR;
		}
	}

	public static boolean isValidArmorSlot(EquipmentSlot slot) {
		return slot != EquipmentSlot.HAND && slot != EquipmentSlot.OFF_HAND && slot != EquipmentSlot.BODY;
	}
}