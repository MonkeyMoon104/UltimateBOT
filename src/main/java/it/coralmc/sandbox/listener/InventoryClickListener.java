package it.coralmc.sandbox.listener;

import it.coralmc.sandbox.SandboxBot;
import it.coralmc.sandbox.bot.BotSpawner;
import it.coralmc.sandbox.gui.BotSettingsGUI;
import it.coralmc.sandbox.utils.armor.ArmorCycle;
import it.coralmc.sandbox.utils.armor.InventoryArmorExtractor;
import it.coralmc.sandbox.utils.armor.PlayerArmorManager;
import it.coralmc.sandbox.utils.builder.ItemBuilder;
import it.coralmc.sandbox.utils.chatcolor.ChatColorUtils;
import it.coralmc.sandbox.utils.gui.GUISlotHandler;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.Map;

public class InventoryClickListener implements Listener {

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
        }
        else if (GUISlotHandler.isFollowButton(slot)) {
            handleFollowButtonClick(e, player, config);
        }
        else if (GUISlotHandler.isSpawnButton(slot)) {
            handleSpawnButtonClick(e, player, follow, config);
        }
        else if (GUISlotHandler.isSaveButton(slot)) {
            handleSaveButtonClick(player, selected, config);
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
        e.getInventory().setItem(slot, ItemBuilder.createArmorItem(next, armorSlot, config));
    }

    private void handleFollowButtonClick(InventoryClickEvent e, Player player,
                                         org.bukkit.configuration.file.FileConfiguration config) {

        boolean currentFollow = PlayerArmorManager.getPlayerFollowSetting(player.getUniqueId());
        boolean newFollow = !currentFollow;

        PlayerArmorManager.setPlayerFollowSetting(player.getUniqueId(), newFollow);
        e.getInventory().setItem(7, ItemBuilder.createFollowButton(newFollow, config));
    }

    private void handleSpawnButtonClick(InventoryClickEvent e, Player player, boolean follow,
                                        org.bukkit.configuration.file.FileConfiguration config) {

        if (BotSpawner.isBotSpawned(player.getUniqueId())) {
            BotSpawner.despawnBot(player);
            player.closeInventory();

            String despawnMsg = config.getString("messages.despawn-bot", "&cBot despawned!");
            player.sendMessage(ChatColorUtils.translate(despawnMsg));
        } else {
            Map<EquipmentSlot, Material> selectedFromGUI =
                    InventoryArmorExtractor.extractArmorFromGUI(e.getInventory());

            PlayerArmorManager.setPlayerArmorSelection(player.getUniqueId(), selectedFromGUI);

            player.closeInventory();
            BotSpawner.spawnFakeBot(player, selectedFromGUI, follow);

            String spawnMsg = config.getString("messages.spawn-bot", "&aBot spawned!");
            player.sendMessage(ChatColorUtils.translate(spawnMsg));

            PlayerArmorManager.removePlayerSettings(player.getUniqueId());
        }
    }

    private void handleSaveButtonClick(Player player, Map<EquipmentSlot, Material> selected,
                                       org.bukkit.configuration.file.FileConfiguration config) {

        if (BotSpawner.isBotSpawned(player.getUniqueId())) {
            BotSpawner.updateBotArmor(player.getUniqueId(), selected);
            player.closeInventory();

            String saveMsg = config.getString("messages.save-changes", "&aChanges saved!");
            player.sendMessage(ChatColorUtils.translate(saveMsg));
        }
    }
}