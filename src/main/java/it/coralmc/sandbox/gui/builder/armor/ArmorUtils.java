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

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

public class ArmorUtils {

	private final SandboxTraining plugin;
	private final FileConfiguration config;

	public ArmorUtils(SandboxTraining plugin) {
		this.plugin = plugin;
		this.config = plugin.getConfig();
	}

	public  Map<EquipmentSlot, ItemStack> initializePlayerArmor(UUID playerUUID) {
		Map<EquipmentSlot, ItemStack> initialArmor;

		BotSpawner botSpawner = plugin.getBotSpawner();
		PlayerArmorManager playerArmorManager = plugin.getPlayerArmorManager();

		if (botSpawner.isBotSpawned(playerUUID)) {
			Map<EquipmentSlot, ItemStack> botArmor = botSpawner.getBotArmor(playerUUID);
			if (botArmor != null) {
				initialArmor = new EnumMap<>(botArmor);
				playerArmorManager.playerArmorSelections.put(playerUUID, new EnumMap<>(botArmor));
			} else {
				initialArmor = ArmorCycle.getDefaultArmorFromConfig(config, plugin);
			}
		} else {
			initialArmor = ArmorCycle.getDefaultArmorFromConfig(config, plugin);
		}

		return processArmorSlots(initialArmor);
	}

	private Map<EquipmentSlot, ItemStack> processArmorSlots(Map<EquipmentSlot, ItemStack> initialArmor) {
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

	public String getConfigKeyForSlot(EquipmentSlot slot) {
		return switch (slot) {
			case HEAD -> "helmet";
			case CHEST -> "chestplate";
			case LEGS -> "leggings";
			case FEET -> "boots";
			default -> null;
		};
	}

	public int getSlotIndex(EquipmentSlot slot) {
		return switch (slot) {
			case HEAD -> 20;
			case CHEST -> 21;
			case LEGS -> 23;
			case FEET -> 24;
			default -> -1;
		};
	}

	public Material getArmorMaterial(String type, EquipmentSlot slot) {
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

	public boolean isValidArmorSlot(EquipmentSlot slot) {
		return slot != EquipmentSlot.HAND && slot != EquipmentSlot.OFF_HAND && slot != EquipmentSlot.BODY;
	}
}