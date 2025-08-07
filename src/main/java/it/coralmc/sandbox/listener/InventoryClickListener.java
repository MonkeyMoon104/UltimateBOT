package it.coralmc.sandbox.listener;

import it.coralmc.sandbox.SandboxBot;
import it.coralmc.sandbox.bot.BotSpawner;
import it.coralmc.sandbox.gui.BotSettingsGUI;
import it.coralmc.sandbox.gui.builder.GUIItemBuilder;
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
import org.bukkit.inventory.EquipmentSlot;

import java.util.Map;
import java.util.UUID;

public class InventoryClickListener implements Listener {

    private final GUIItemBuilder guiItemBuilder;

    public InventoryClickListener() {
        this.guiItemBuilder = new GUIItemBuilder();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (!BotSettingsGUI.isBotSettingsGUI(e)) return;

        e.setCancelled(true);

        int slot = e.getRawSlot();
        if (slot >= e.getInventory().getSize()) return;

        var config = SandboxBot.getInstance().getConfig();

        PlayerArmorManager.initializePlayerDefaults(
                player.getUniqueId(),
                ArmorCycle.getDefaultArmorFromConfig(config)
        );

        Map<EquipmentSlot, Material> selected = PlayerArmorManager.getPlayerArmorSelection(player.getUniqueId());
        boolean follow = PlayerArmorManager.getPlayerFollowSetting(player.getUniqueId());

        if (GUISlotHandler.isArmorSlot(slot)) {
            handleArmorSlotClick(e, player, slot, selected, config);
        } else if (GUISlotHandler.isFollowButton(slot)) {
            handleFollowButtonClick(e, player, config);
        } else if (GUISlotHandler.isSpawnButton(slot)) {
            handleSpawnButtonClick(e, player, config);
        } else if (GUISlotHandler.isSaveButton(slot)) {
            handleSaveButtonClick(player, config);
        } else if (GUISlotHandler.isTotemButton(slot)) {
            handleTotemButtonClick(e, player);
        }
    }

    private void handleArmorSlotClick(InventoryClickEvent e, Player player, int slot,
                                      Map<EquipmentSlot, Material> selected,
                                      org.bukkit.configuration.file.FileConfiguration config) {

        EquipmentSlot armorSlot = GUISlotHandler.getEquipmentSlotFromGUISlot(slot);
        if (armorSlot == null) return;

        Material old = selected.getOrDefault(armorSlot, Material.AIR);
        Material next = ArmorCycle.getNextArmor(old, armorSlot);

        PlayerArmorManager.updateArmorPiece(player.getUniqueId(), armorSlot, next);

        e.getInventory().setItem(slot, guiItemBuilder.createArmorItem(next));
    }

    private void handleFollowButtonClick(InventoryClickEvent e, Player player,
                                         org.bukkit.configuration.file.FileConfiguration config) {

        boolean currentFollow = PlayerArmorManager.getPlayerFollowSetting(player.getUniqueId());
        boolean newFollow = !currentFollow;

        PlayerArmorManager.setPlayerFollowSetting(player.getUniqueId(), newFollow);

        e.getInventory().setItem(7, guiItemBuilder.createFollowButton(newFollow));
    }

    private void handleSpawnButtonClick(InventoryClickEvent e, Player player,
                                        org.bukkit.configuration.file.FileConfiguration config) {

        if (BotSpawner.isBotSpawned(player.getUniqueId())) {
            BotSpawner.despawnBot(player);
            player.closeInventory();

            String despawnMsg = config.getString("messages.despawn-bot", "&cBot despawned!");
            player.sendMessage(ChatColorUtils.translate(despawnMsg));

            PlayerArmorManager.removePlayerSettings(player.getUniqueId());
        } else {

            Map<EquipmentSlot, Material> selectedFromGUI =
                    InventoryArmorExtractor.extractArmorFromGUI(e.getInventory());

            PlayerArmorManager.setPlayerArmorSelection(player.getUniqueId(), selectedFromGUI);

            int totemCountFromGUI = extractTotemCountFromGUI(e.getInventory());
            PlayerArmorManager.setPlayerTotemCount(player.getUniqueId(), totemCountFromGUI);

            player.closeInventory();
            int totemCount = PlayerArmorManager.getPlayerTotemCount(player.getUniqueId());
            boolean follow = PlayerArmorManager.getPlayerFollowSetting(player.getUniqueId());

            BotSpawner.spawnFakeBot(player, selectedFromGUI, follow, totemCount);
            String spawnMsg = config.getString("messages.spawn-bot", "&aBot spawned!");
            player.sendMessage(ChatColorUtils.translate(spawnMsg));

            PlayerArmorManager.removePlayerSettings(player.getUniqueId());
        }
    }

    private void handleSaveButtonClick(Player player,
                                       org.bukkit.configuration.file.FileConfiguration config) {
        UUID playerUUID = player.getUniqueId();

        if (BotSpawner.isBotSpawned(playerUUID)) {

            Map<EquipmentSlot, Material> selectedFromGUI =
                    InventoryArmorExtractor.extractArmorFromGUI(player.getOpenInventory().getTopInventory());

            int totemCountFromGUI = extractTotemCountFromGUI(player.getOpenInventory().getTopInventory());

            boolean follow = PlayerArmorManager.getPlayerFollowSetting(playerUUID);

            BotSpawner.updateBotArmor(playerUUID, selectedFromGUI);
            BotSpawner.updateBotFollow(playerUUID, follow);
            BotSpawner.updateBotTotemCount(playerUUID, totemCountFromGUI);

            PlayerArmorManager.setPlayerFollowSetting(playerUUID, follow);
            PlayerArmorManager.setPlayerTotemCount(playerUUID, totemCountFromGUI);
            PlayerArmorManager.setPlayerArmorSelection(playerUUID, selectedFromGUI);

            player.closeInventory();

            String saveMsg = config.getString("messages.save-changes", "&aChanges saved!");
            player.sendMessage(ChatColorUtils.translate(saveMsg));
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

        e.getInventory().setItem(11, guiItemBuilder.createTotemButton(current));
    }

    private int extractTotemCountFromGUI(org.bukkit.inventory.Inventory gui) {
        var item = gui.getItem(11);
        if (item == null) {
            return 37;
        }

        var meta = item.getItemMeta();
        if (meta == null || meta.getLore() == null || meta.getLore().isEmpty()) {
            return 37;
        }

        var config = SandboxBot.getInstance().getConfig();
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