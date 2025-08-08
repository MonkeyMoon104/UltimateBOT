package it.coralmc.sandbox.listener;

import it.coralmc.sandbox.SandboxTraining;
import it.coralmc.sandbox.bot.BotSpawner;
import it.coralmc.sandbox.bot.util.TrainingBot;
import it.coralmc.sandbox.bot.util.registry.BotRegistry;
import it.coralmc.sandbox.gui.BotSettingsGUI;
import it.coralmc.sandbox.gui.builder.GUIItemBuilder;
import it.coralmc.sandbox.gui.validator.GUIValidator;
import it.coralmc.sandbox.utils.armor.ArmorCycle;
import it.coralmc.sandbox.utils.armor.InventoryArmorExtractor;
import it.coralmc.sandbox.utils.armor.PlayerArmorManager;
import it.coralmc.sandbox.utils.chatcolor.ChatColorUtils;
import it.coralmc.sandbox.utils.gui.GUISlotHandler;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.UUID;

public class InventoryClickListener implements Listener {

	private final SandboxTraining plugin;
	private final BotSpawner bot;
	private final GUISlotHandler guiSlotHandler;
	private final InventoryArmorExtractor armorExtractor;
	private final ChatColorUtils chatColorUtils;
	private final GUIItemBuilder guiItemBuilder;
	private final PlayerArmorManager playerArmorManager;
	private final BotRegistry botRegistry;

	public InventoryClickListener(SandboxTraining plugin, BotRegistry botRegistry) {
		this.plugin = plugin;
		this.bot = plugin.getBotSpawner();
		this.guiSlotHandler = plugin.getGuiSlotHandler();
		this.chatColorUtils = plugin.getChatColorUtils();
		this.guiItemBuilder = plugin.getGuiItemBuilder();
        this.playerArmorManager = plugin.getPlayerArmorManager();
        this.botRegistry = botRegistry;
        this.armorExtractor = new InventoryArmorExtractor(guiSlotHandler);
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		if (!(e.getWhoClicked() instanceof Player player)) return;
		if (!BotSettingsGUI.isBotSettingsGUI(e)) return;

		e.setCancelled(true);

		int slot = e.getRawSlot();
		if (slot >= e.getInventory().getSize()) return;

		var config =plugin.getConfig();

		playerArmorManager.initializePlayerDefaults(
			player.getUniqueId(),
			ArmorCycle.getDefaultArmorFromConfig(config, plugin)
		);

		Map<EquipmentSlot, ItemStack> selected = playerArmorManager.getPlayerArmorSelection(player.getUniqueId());
		boolean follow = playerArmorManager.getPlayerFollowSetting(player.getUniqueId());

		if (guiSlotHandler.isArmorSlot(slot)) {
			handleArmorSlotClick(e, player, slot, selected, config);
		} else if (guiSlotHandler.isFollowButton(slot)) {
			handleFollowButtonClick(e, player, config);
		} else if (guiSlotHandler.isSpawnButton(slot)) {
			handleSpawnButtonClick(e, player, config);
		} else if (guiSlotHandler.isTotemButton(slot)) {
			handleTotemButtonClick(e, player);
		} else if (slot == 40) {
			TrainingBot bot = botRegistry.getAllBots().get(player.getUniqueId());
			if (bot == null) return;
			
			bot.teleportTo(player.getX(), player.getY(), player.getZ());
			player.sendRichMessage("<green>Hai teletrasportato il bot da te stesso.");
		}
	}

	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {
		if (!event.getView().getTitle().equalsIgnoreCase(GUIValidator.getBotSettingsGUITitle())) return;

		handleSaveButtonClick((Player) event.getPlayer(), plugin.getConfig());
	}

	private void handleArmorSlotClick(InventoryClickEvent e, Player player, int slot,
									  Map<EquipmentSlot, ItemStack> selected,
									  org.bukkit.configuration.file.FileConfiguration config) {

		EquipmentSlot armorSlot = guiSlotHandler.getEquipmentSlotFromGUISlot(slot);
		if (armorSlot == null) return;

		ItemStack old = selected.getOrDefault(armorSlot, null);
		Material next = ArmorCycle.getNextArmor(old.getType(), armorSlot);

		playerArmorManager.updateArmorPiece(player.getUniqueId(), armorSlot, old, next);

		e.getInventory().setItem(slot, guiItemBuilder.createArmorItem(next));
	}

	private void handleFollowButtonClick(InventoryClickEvent e, Player player,
										 org.bukkit.configuration.file.FileConfiguration config) {

		boolean currentFollow = playerArmorManager.getPlayerFollowSetting(player.getUniqueId());
		boolean newFollow = !currentFollow;

		playerArmorManager.setPlayerFollowSetting(player.getUniqueId(), newFollow);

		e.getInventory().setItem(41, guiItemBuilder.createFollowButton(newFollow));
	}

	private void handleSpawnButtonClick(InventoryClickEvent e, Player player,
										org.bukkit.configuration.file.FileConfiguration config) {

		if (bot.isBotSpawned(player.getUniqueId())) {
			bot.despawnBot(player);
			player.closeInventory();

			String despawnMsg = config.getString("messages.despawn-bot", "&cBot despawned!");
			player.sendMessage(chatColorUtils.translate(despawnMsg));

			playerArmorManager.removePlayerSettings(player.getUniqueId());
		} else {

			Map<EquipmentSlot, ItemStack> selectedFromGUI =
				armorExtractor.extractArmorFromGUI(e.getInventory());

			playerArmorManager.setPlayerArmorSelection(player.getUniqueId(), selectedFromGUI);

			int totemCountFromGUI = extractTotemCountFromGUI(e.getInventory());
			playerArmorManager.setPlayerTotemCount(player.getUniqueId(), totemCountFromGUI);

			player.closeInventory();
			int totemCount = playerArmorManager.getPlayerTotemCount(player.getUniqueId());
			boolean follow = playerArmorManager.getPlayerFollowSetting(player.getUniqueId());

			bot.spawnFakeBot(player, selectedFromGUI, follow, totemCount);
			String spawnMsg = config.getString("messages.spawn-bot", "&aBot spawned!");
			player.sendMessage(chatColorUtils.translate(spawnMsg));

			playerArmorManager.removePlayerSettings(player.getUniqueId());
		}
	}

	private void handleSaveButtonClick(Player player,
									   org.bukkit.configuration.file.FileConfiguration config) {
		UUID playerUUID = player.getUniqueId();

		if (bot.isBotSpawned(playerUUID)) {

			Map<EquipmentSlot, ItemStack> selectedFromGUI =
				armorExtractor.extractArmorFromGUI(player.getOpenInventory().getTopInventory());

			int totemCountFromGUI = extractTotemCountFromGUI(player.getOpenInventory().getTopInventory());

			boolean follow = playerArmorManager.getPlayerFollowSetting(playerUUID);

			bot.updateBotArmor(playerUUID, selectedFromGUI);
			bot.updateBotFollow(playerUUID, follow);
			bot.updateBotTotemCount(playerUUID, totemCountFromGUI);

			playerArmorManager.setPlayerFollowSetting(playerUUID, follow);
			playerArmorManager.setPlayerTotemCount(playerUUID, totemCountFromGUI);
			playerArmorManager.setPlayerArmorSelection(playerUUID, selectedFromGUI);

			String saveMsg = config.getString("messages.save-changes", "&aChanges saved!");
			player.sendMessage(chatColorUtils.translate(saveMsg));
		}
	}

	private void handleTotemButtonClick(InventoryClickEvent e, Player player) {
		UUID uuid = player.getUniqueId();

		int current = getCurrentTotemCountFromGUI(e.getInventory());

		boolean leftClick = e.isLeftClick();
		boolean rightClick = e.isRightClick();

		if (leftClick && current < 37) {
			current++;
		} else if (rightClick && current > -1) {
			current--;
		}

		if (current < -1) current = -1;
		if (current > 37) current = 37;

		e.getInventory().setItem(22, guiItemBuilder.createTotemButton(current));
	}

	private int extractTotemCountFromGUI(org.bukkit.inventory.Inventory gui) {
		var item = gui.getItem(22);
		if (item == null) {
			return 37;
		}

		var meta = item.getItemMeta();
		if (meta == null || meta.getLore() == null || meta.getLore().isEmpty()) {
			return 37;
		}

		var config = plugin.getConfig();
		String unlimitedText = config.getString("gui.totem-button.unlimited-text", "Unlimited");

		for (String loreLine : meta.getLore()) {
			String cleanLine = loreLine.replaceAll("§[0-9a-fk-or]", "");

			if (cleanLine.contains(unlimitedText)) {
				return -1;
			}

			if (cleanLine.matches(".*\\d+.*")) {
				String[] parts = cleanLine.split(":");
				if (parts.length > 1) {
					String numberPart = parts[1].trim().replaceAll("[^0-9]", "");
					if (!numberPart.isEmpty()) {
						try {
							return Integer.parseInt(numberPart);
						} catch (NumberFormatException ignored) {
						}
					}
				} else {
					String numberPart = cleanLine.replaceAll("[^0-9]", "");
					if (!numberPart.isEmpty()) {
						try {
							return Integer.parseInt(numberPart);
						} catch (NumberFormatException ignored) {
						}
					}
				}
			}
		}

		return 37;
	}

	private int getCurrentTotemCountFromGUI(org.bukkit.inventory.Inventory gui) {
		return extractTotemCountFromGUI(gui);
	}
}