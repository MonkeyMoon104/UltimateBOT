package it.coralmc.sandbox.gui.builder.armor;

import it.coralmc.sandbox.SandboxBot;
import it.coralmc.sandbox.bot.BotSpawner;
import it.coralmc.sandbox.utils.armor.ArmorCycle;
import it.coralmc.sandbox.utils.armor.PlayerArmorManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.EquipmentSlot;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

public class ArmorUtils {

    private static final FileConfiguration config = SandboxBot.getInstance().getConfig();

    public static Map<EquipmentSlot, Material> initializePlayerArmor(UUID playerUUID) {
        Map<EquipmentSlot, Material> initialArmor;

        if (BotSpawner.isBotSpawned(playerUUID)) {
            Map<EquipmentSlot, Material> botArmor = BotSpawner.getBotArmor(playerUUID);
            if (botArmor != null) {
                initialArmor = new EnumMap<>(botArmor);
                PlayerArmorManager.playerArmorSelections.put(playerUUID, new EnumMap<>(botArmor));
            } else {
                initialArmor = ArmorCycle.getDefaultArmorFromConfig(config);
            }
        } else {
            initialArmor = ArmorCycle.getDefaultArmorFromConfig(config);
        }

        return processArmorSlots(initialArmor);
    }

    private static Map<EquipmentSlot, Material> processArmorSlots(Map<EquipmentSlot, Material> initialArmor) {
        Map<EquipmentSlot, Material> processedArmor = new EnumMap<>(EquipmentSlot.class);

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot == EquipmentSlot.HAND || slot == EquipmentSlot.OFF_HAND) continue;

            String configKey = getConfigKeyForSlot(slot);
            if (configKey == null) continue;

            Material material = initialArmor.get(slot);
            if (material == null || material == Material.AIR) {
                String fallbackType = config.getString("gui.default-armor." + configKey, "NETHERITE");
                material = getArmorMaterial(fallbackType, slot);

                if (material == null || material == Material.AIR) {
                    String noArmor = config.getString("gui.default-armor.no-armor", "BARRIER");
                    try {
                        material = Material.valueOf(noArmor.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        Bukkit.getLogger().warning("Materiale fallback no-armor non valido: " + noArmor + ", imposto BARRIER");
                        material = Material.BARRIER;
                    }
                }
            }

            processedArmor.put(slot, material);
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
            case HEAD -> 0;
            case CHEST -> 1;
            case LEGS -> 2;
            case FEET -> 3;
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
        return slot != EquipmentSlot.HAND && slot != EquipmentSlot.OFF_HAND;
    }
}