package it.coralmc.sandbox.gui.builder;

import it.coralmc.sandbox.SandboxBot;
import it.coralmc.sandbox.utils.chatcolor.ChatColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;
import java.util.Locale;

public class GUIItemBuilder {

    private final FileConfiguration config;

    public GUIItemBuilder() {
        this.config = SandboxBot.getInstance().getConfig();
    }

    public ItemStack createSpawnButton() {
        String materialName = config.getString("gui.spawn-button.material", "SPAWNER");
        Material material = getMaterialSafely(materialName, Material.SPAWNER, "spawn-button");

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        String name = config.getString("gui.spawn-button.name", "&aSpawn Bot");
        meta.setDisplayName(ChatColorUtils.translate(name));

        item.setItemMeta(meta);
        return item;
    }

    public ItemStack createDespawnButton() {
        String materialName = config.getString("gui.despawn-button.material", "BARRIER");
        Material material = getMaterialSafely(materialName, Material.BARRIER, "despawn-button");

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        String name = config.getString("gui.despawn-button.name", "&cDespawn Bot");
        meta.setDisplayName(ChatColorUtils.translate(name));

        item.setItemMeta(meta);
        return item;
    }

    public ItemStack createSaveButton() {
        String materialName = config.getString("gui.save-button.material", "GREEN_WOOL");
        Material material = getMaterialSafely(materialName, Material.GREEN_WOOL, "save-button");

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        String name = config.getString("gui.save-button.name", "&aSave Changes");
        meta.setDisplayName(ChatColorUtils.translate(name));

        item.setItemMeta(meta);
        return item;
    }

    public ItemStack createFollowButton(boolean followStatus) {
        String materialName = config.getString("gui.follow-button.material", "LEAD");
        Material material = getMaterialSafely(materialName, Material.LEAD, "follow-button");

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        String displayName = config.getString("gui.follow-button.name", "&bToggle Follow");
        meta.setDisplayName(ChatColorUtils.translate(displayName));

        String loreTemplate = config.getString("messages.set-follow", "Set follow = %type%");
        String lore = loreTemplate.replace("%type%", followStatus ? "true" : "false");
        meta.setLore(Collections.singletonList(ChatColorUtils.translate(lore)));

        item.setItemMeta(meta);
        return item;
    }

    public ItemStack createArmorItem(Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        String lore = config.getString("messages.set-type", "Set type = %type%")
                .replace("%type%", material.name());
        meta.setLore(Collections.singletonList(lore));

        item.setItemMeta(meta);
        return item;
    }

    private Material getMaterialSafely(String materialName, Material fallback, String buttonType) {
        try {
            return Material.valueOf(materialName.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            Bukkit.getLogger().warning("Materiale " + buttonType + " non valido: " + materialName +
                    ", imposto " + fallback.name());
            return fallback;
        }
    }
}