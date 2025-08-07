package it.coralmc.sandbox.utils.armor;

import org.bukkit.Material;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerArmorManager {

	public static final Map<UUID, Map<EquipmentSlot, ItemStack>> playerArmorSelections = new HashMap<>();
	private static final Map<UUID, Boolean> playerFollowSetting = new HashMap<>();
	private static final Map<UUID, Integer> playerTotemCount = new HashMap<>();

	public static void initializePlayerDefaults(UUID playerUUID, Map<EquipmentSlot, ItemStack> defaultArmor) {
		playerArmorSelections.putIfAbsent(playerUUID, defaultArmor);
		playerFollowSetting.putIfAbsent(playerUUID, false);
	}

	public static Map<EquipmentSlot, ItemStack> getPlayerArmorSelection(UUID playerUUID) {
		return playerArmorSelections.get(playerUUID);
	}

	public static void updateArmorPiece(UUID playerUUID, EquipmentSlot slot, ItemStack item, Material updated) {
		Map<EquipmentSlot, ItemStack> selected = playerArmorSelections.get(playerUUID);
		if (selected != null) {
			selected.put(slot, item.withType(updated));
		}
	}

	public static boolean getPlayerFollowSetting(UUID playerUUID) {
		return playerFollowSetting.getOrDefault(playerUUID, false);
	}

	public static void setPlayerFollowSetting(UUID playerUUID, boolean follow) {
		playerFollowSetting.put(playerUUID, follow);
	}

	public static void removePlayerSettings(UUID playerUUID) {
		playerArmorSelections.remove(playerUUID);
		playerFollowSetting.remove(playerUUID);
		playerTotemCount.remove(playerUUID);
	}

	public static void setPlayerArmorSelection(UUID playerUUID, Map<EquipmentSlot, ItemStack> armorSelection) {
		playerArmorSelections.put(playerUUID, armorSelection);
	}

	public static void setPlayerTotemCount(UUID uuid, int count) {
		playerTotemCount.put(uuid, count);
	}

	public static int getPlayerTotemCount(UUID uuid) {
		return playerTotemCount.getOrDefault(uuid, 37);
	}
}