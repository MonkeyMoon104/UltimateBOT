package it.coralmc.sandbox.listener;

import it.coralmc.sandbox.SandboxBot;
import it.coralmc.sandbox.bot.BotSpawner;
import it.coralmc.sandbox.gui.BotSettingsGUI;
import it.coralmc.sandbox.utils.ArmorCycle;
import it.coralmc.sandbox.utils.ChatColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InventoryClickListener implements Listener {

    private final Map<UUID, Map<EquipmentSlot, Material>> playerArmorSelections = new HashMap<>();
    private final Map<UUID, Boolean> playerFollowSetting = new HashMap<>();

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (!BotSettingsGUI.isBotSettingsGUI(e)) return;

        e.setCancelled(true);

        int slot = e.getRawSlot();
        if (slot >= e.getInventory().getSize()) return;

        var config = SandboxBot.getInstance().getConfig();

        playerArmorSelections.putIfAbsent(player.getUniqueId(), ArmorCycle.getDefaultArmorFromConfig(config));
        playerFollowSetting.putIfAbsent(player.getUniqueId(), false);

        Map<EquipmentSlot, Material> selected = playerArmorSelections.get(player.getUniqueId());
        boolean follow = playerFollowSetting.get(player.getUniqueId());

        if (slot >= 0 && slot <= 3) {
            EquipmentSlot armorSlot = switch (slot) {
                case 0 -> EquipmentSlot.HEAD;
                case 1 -> EquipmentSlot.CHEST;
                case 2 -> EquipmentSlot.LEGS;
                case 3 -> EquipmentSlot.FEET;
                default -> null;
            };
            if (armorSlot == null) return;

            Material old = selected.getOrDefault(armorSlot, Material.AIR);
            Material next = ArmorCycle.getNextArmor(old, armorSlot);

            selected.put(armorSlot, next);

            ItemStack newItem = new ItemStack(next);
            ItemMeta meta = newItem.getItemMeta();

            String loreTemplate = config.getString("messages.set-type");
            String lore = loreTemplate.replace("%type%", next.name().replace("_" + armorSlot.name(), ""));
            meta.setLore(Collections.singletonList(ChatColorUtils.translate(lore)));

            newItem.setItemMeta(meta);
            e.getInventory().setItem(slot, newItem);
        }

        else if (slot == 7) {
            follow = !follow;
            playerFollowSetting.put(player.getUniqueId(), follow);

            ItemStack followBtn = new ItemStack(Material.LEAD);
            ItemMeta meta = followBtn.getItemMeta();

            String displayName = config.getString("messages.follow-toggle-name");
            String loreTemplate = config.getString("messages.set-follow");
            String lore = loreTemplate.replace("%type%", String.valueOf(follow));

            meta.setDisplayName(ChatColorUtils.translate(displayName));
            meta.setLore(Collections.singletonList(ChatColorUtils.translate(lore)));
            followBtn.setItemMeta(meta);

            e.getInventory().setItem(7, followBtn);
        }

        else if (slot == 8) {
            player.closeInventory();

            Bukkit.getLogger().info("Player " + player.getName() + " is spawning bot with armor: " + selected + " and follow=" + follow);
            BotSpawner.spawnFakeBot(player, selected, follow);

            String spawnMsg = config.getString("messages.spawn-bot");
            player.sendMessage(ChatColorUtils.translate(spawnMsg));

            playerArmorSelections.remove(player.getUniqueId());
            playerFollowSetting.remove(player.getUniqueId());
        }
    }
}