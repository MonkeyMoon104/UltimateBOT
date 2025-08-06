package it.coralmc.sandbox.utils.builder;

import it.coralmc.sandbox.utils.chatcolor.ChatColorUtils;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;

public class ItemBuilder {

    public static ItemStack createArmorItem(Material material, EquipmentSlot armorSlot, FileConfiguration config) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            String loreTemplate = config.getString("messages.set-type", "Type: %type%");
            String lore = loreTemplate.replace("%type%",
                    material.name().replace("_" + armorSlot.name(), ""));
            meta.setLore(Collections.singletonList(ChatColorUtils.translate(lore)));
            item.setItemMeta(meta);
        }

        return item;
    }

    public static ItemStack createFollowButton(boolean followState, FileConfiguration config) {
        ItemStack followBtn = new ItemStack(Material.LEAD);
        ItemMeta meta = followBtn.getItemMeta();

        if (meta != null) {
            String displayName = config.getString("messages.follow-toggle-name", "Follow Toggle");
            String loreTemplate = config.getString("messages.set-follow", "Follow: %type%");
            String lore = loreTemplate.replace("%type%", String.valueOf(followState));

            meta.setDisplayName(ChatColorUtils.translate(displayName));
            meta.setLore(Collections.singletonList(ChatColorUtils.translate(lore)));
            followBtn.setItemMeta(meta);
        }

        return followBtn;
    }

    public static ItemStack createTotemButton(int totemCount) {
        Material material = Material.TOTEM_OF_UNDYING;
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(ChatColorUtils.translate("&eTotem Count"));

        String lore = totemCount == -1 ? "&7Unlimited Totems"
                : "&7Totems: &a" + totemCount;
        meta.setLore(Collections.singletonList(ChatColorUtils.translate(lore)));

        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack createItem(Material material) {
        return new ItemStack(material);
    }

    public static ItemStack createItemWithName(Material material, String displayName) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(ChatColorUtils.translate(displayName));
            item.setItemMeta(meta);
        }

        return item;
    }

    public static ItemStack createItemWithNameAndLore(Material material, String displayName, String lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(ChatColorUtils.translate(displayName));
            meta.setLore(Collections.singletonList(ChatColorUtils.translate(lore)));
            item.setItemMeta(meta);
        }

        return item;
    }
}