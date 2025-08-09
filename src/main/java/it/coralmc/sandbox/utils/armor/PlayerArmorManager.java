package it.coralmc.sandbox.utils.armor;

import org.bukkit.Material;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerArmorManager {

	public final Map<UUID, Map<EquipmentSlot, ItemStack>> playerArmorSelections = new HashMap<>();
	private final Map<UUID, Boolean> playerFollowSetting = new HashMap<>();
	private final Map<UUID, Integer> playerTotemCount = new HashMap<>();

	private final Map<UUID, Map<EquipmentSlot, Boolean>> playerBlastProtectionSettings = new HashMap<>();

	public void initializePlayerDefaults(UUID playerUUID, Map<EquipmentSlot, ItemStack> defaultArmor) {
		playerArmorSelections.putIfAbsent(playerUUID, defaultArmor);
		playerFollowSetting.putIfAbsent(playerUUID, false);

		if (!playerBlastProtectionSettings.containsKey(playerUUID)) {
			Map<EquipmentSlot, Boolean> defaultBlastProtection = new HashMap<>();
			for (EquipmentSlot slot : EquipmentSlot.values()) {
				if (slot != EquipmentSlot.HAND && slot != EquipmentSlot.OFF_HAND && slot != EquipmentSlot.BODY) {
					defaultBlastProtection.put(slot, false);
				}
			}
			playerBlastProtectionSettings.put(playerUUID, defaultBlastProtection);
		}
	}

	public Map<EquipmentSlot, ItemStack> getPlayerArmorSelection(UUID playerUUID) {
		return playerArmorSelections.get(playerUUID);
	}

	public void updateArmorPiece(UUID playerUUID, EquipmentSlot slot, ItemStack item, Material updated) {
		Map<EquipmentSlot, ItemStack> selected = playerArmorSelections.get(playerUUID);
		if (selected != null) {
			selected.put(slot, item.withType(updated));
		}
	}

	public boolean getPlayerFollowSetting(UUID playerUUID) {
		return playerFollowSetting.getOrDefault(playerUUID, false);
	}

	public void setPlayerFollowSetting(UUID playerUUID, boolean follow) {
		playerFollowSetting.put(playerUUID, follow);
	}

	public void removePlayerSettings(UUID playerUUID) {
		playerArmorSelections.remove(playerUUID);
		playerFollowSetting.remove(playerUUID);
		playerTotemCount.remove(playerUUID);
		playerBlastProtectionSettings.remove(playerUUID);
	}

	public void setPlayerArmorSelection(UUID playerUUID, Map<EquipmentSlot, ItemStack> armorSelection) {
		playerArmorSelections.put(playerUUID, armorSelection);
	}

	public void setPlayerTotemCount(UUID uuid, int count) {
		playerTotemCount.put(uuid, count);
	}

	public int getPlayerTotemCount(UUID uuid) {
		return playerTotemCount.getOrDefault(uuid, -1);
	}

	public boolean getBlastProtectionSetting(UUID playerUUID, EquipmentSlot slot) {
		Map<EquipmentSlot, Boolean> settings = playerBlastProtectionSettings.get(playerUUID);
		return settings != null && settings.getOrDefault(slot, false);
	}

	public void setBlastProtectionSetting(UUID playerUUID, EquipmentSlot slot, boolean enabled) {
		playerBlastProtectionSettings.computeIfAbsent(playerUUID, k -> new HashMap<>()).put(slot, enabled);
	}

	public void toggleBlastProtectionSetting(UUID playerUUID, EquipmentSlot slot) {
		boolean current = getBlastProtectionSetting(playerUUID, slot);
		setBlastProtectionSetting(playerUUID, slot, !current);
	}

	public Map<EquipmentSlot, Boolean> getPlayerBlastProtectionSettings(UUID playerUUID) {
		return playerBlastProtectionSettings.getOrDefault(playerUUID, new HashMap<>());
	}

	public void setPlayerBlastProtectionSettings(UUID playerUUID, Map<EquipmentSlot, Boolean> settings) {
		playerBlastProtectionSettings.put(playerUUID, settings);
	}

	public void clearAll() {
		playerArmorSelections.clear();
		playerFollowSetting.clear();
		playerTotemCount.clear();
		playerBlastProtectionSettings.clear();
	}
}