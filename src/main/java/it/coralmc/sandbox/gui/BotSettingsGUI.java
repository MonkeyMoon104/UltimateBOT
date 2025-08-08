package it.coralmc.sandbox.gui;

import it.coralmc.sandbox.SandboxTraining;
import it.coralmc.sandbox.bot.BotSpawner;
import it.coralmc.sandbox.bot.util.TrainingBot;
import it.coralmc.sandbox.bot.util.entity.BotEntityFinder;
import it.coralmc.sandbox.gui.builder.GUIItemBuilder;
import it.coralmc.sandbox.gui.builder.armor.ArmorUtils;
import it.coralmc.sandbox.gui.validator.GUIValidator;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.EnumMap;
import java.util.Map;

public class BotSettingsGUI {

	private final Player player;
	private final Inventory gui;
	private final Map<EquipmentSlot, Material> selectedArmor = new EnumMap<>(EquipmentSlot.class);
	private final GUIItemBuilder itemBuilder;
	private final ArmorUtils armorUtils;
	private final BotSpawner botSpawner;
	private final BotEntityFinder botEntityFinder;
	private final SandboxTraining plugin;
	private boolean follow;

	public BotSettingsGUI(Player player, SandboxTraining plugin, BotEntityFinder botEntityFinder) {
		this.player = player;
		this.plugin = plugin;
		this.armorUtils = plugin.getArmorUtils();
		this.botSpawner = plugin.getBotSpawner();
		this.itemBuilder = plugin.getGuiItemBuilder();
        this.botEntityFinder = botEntityFinder;
        this.gui = Bukkit.createInventory(null, 54, GUIValidator.getBotSettingsGUITitle());

		initializeArmor();
		setupGUIItems();
	}

	public static boolean isBotSettingsGUI(InventoryClickEvent event) {
		return GUIValidator.isBotSettingsGUI(event);
	}

	private void initializeArmor() {
		Map<EquipmentSlot, ItemStack> initialArmor = armorUtils.initializePlayerArmor(player.getUniqueId());
		
		for (EquipmentSlot slot : EquipmentSlot.values()) {
			if (!armorUtils.isValidArmorSlot(slot)) continue;

			Material material = initialArmor.get(slot).getType();
			selectedArmor.put(slot, material);
			gui.setItem(armorUtils.getSlotIndex(slot), itemBuilder.createArmorItem(material));
		}
	}

	private void setupGUIItems() {
		if (botSpawner.isBotSpawned(player.getUniqueId())) {
			TrainingBot bot = botEntityFinder.getBotByOwnerUUID(player.getUniqueId());
			if (bot != null) {
				this.follow = bot.isFollow();
			}
		}

		gui.setItem(41, itemBuilder.createFollowButton(follow));

		int totemCount = getCorrectTotemCount();
		gui.setItem(22, itemBuilder.createTotemButton(totemCount));

		if (botSpawner.isBotSpawned(player.getUniqueId())) {
			gui.setItem(39, itemBuilder.createDespawnButton());
			gui.setItem(40, itemBuilder.createTeleportItem());
		} else {
			gui.setItem(39, itemBuilder.createSpawnButton());
		}
	}

	public void open() {
		player.openInventory(gui);
	}

	public Map<EquipmentSlot, Material> getSelectedArmor() {
		return new EnumMap<>(selectedArmor);
	}

	public boolean isFollow() {
		return follow;
	}

	public void setFollow(boolean follow) {
		this.follow = follow;
		gui.setItem(7, itemBuilder.createFollowButton(follow));
	}

	public void updateArmorPiece(EquipmentSlot slot, Material material) {
		if (armorUtils.isValidArmorSlot(slot)) {
			selectedArmor.put(slot, material);
			gui.setItem(armorUtils.getSlotIndex(slot), itemBuilder.createArmorItem(material));
		}
	}

	private int getCorrectTotemCount() {
		if (botSpawner.isBotSpawned(player.getUniqueId())) {
			TrainingBot bot = botEntityFinder.getBotByOwnerUUID(player.getUniqueId());
			if (bot != null) {
				return bot.getTotemCount();
			}
		}

		return -1;
	}
}