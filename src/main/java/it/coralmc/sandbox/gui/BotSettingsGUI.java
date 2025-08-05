package it.coralmc.sandbox.gui;

import it.coralmc.sandbox.SandboxBot;
import it.coralmc.sandbox.utils.ChatColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class BotSettingsGUI {

    private final Player player;
    private final Inventory gui;
    private final Map<EquipmentSlot, Material> selectedArmor = new EnumMap<>(EquipmentSlot.class);
    private boolean follow = false;

    private static final String GUI_TITLE = "§6Bot Settings";

    FileConfiguration config = SandboxBot.getInstance().getConfig();

    public BotSettingsGUI(Player player) {
        this.player = player;
        this.gui = Bukkit.createInventory(null, 18, GUI_TITLE);

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot == EquipmentSlot.HAND || slot == EquipmentSlot.OFF_HAND) continue;

            String key = switch (slot) {
                case HEAD -> "helmet";
                case CHEST -> "chestplate";
                case LEGS -> "leggings";
                case FEET -> "boots";
                default -> null;
            };
            if (key == null) continue;

            String matName = config.getString("default-armor." + key, "NETHERITE");

            Material mat = getArmorMaterial(matName, slot);

            selectedArmor.put(slot, mat);

            ItemStack item = new ItemStack(mat);
            ItemMeta meta = item.getItemMeta();

            String lore = config.getString("messages.set-type", "Set type = %type%")
                    .replace("%type%", matName);
            meta.setLore(Collections.singletonList(lore));

            item.setItemMeta(meta);
            gui.setItem(getSlotIndex(slot), item);
        }

        gui.setItem(7, createFollowButton());

        gui.setItem(8, createSpawnButton());
    }

    private ItemStack createSpawnButton() {
        String spawnMatName = config.getString("gui.spawn-button.material", "SPAWNER");
        Material spawnMat;
        try {
            spawnMat = Material.valueOf(spawnMatName.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            Bukkit.getLogger().warning("Materiale spawn-button non valido: " + spawnMatName + ", imposto SPAWNER");
            spawnMat = Material.SPAWNER;
        }

        ItemStack spawnBtn = new ItemStack(spawnMat);
        ItemMeta spawnMeta = spawnBtn.getItemMeta();

        String spawnName = config.getString("gui.spawn-button.name", "&aSpawn Bot");
        spawnMeta.setDisplayName(ChatColorUtils.translate(spawnName));

        spawnBtn.setItemMeta(spawnMeta);
        return spawnBtn;

    }

    private ItemStack createFollowButton() {
        String matName = config.getString("gui.follow-button.material", "LEAD");
        Material material;
        try {
            material = Material.valueOf(matName.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            Bukkit.getLogger().warning("Materiale follow-button non valido: " + matName + ", imposto lead");
            material = Material.LEAD;
        }

        ItemStack followBtn = new ItemStack(material);
        ItemMeta meta = followBtn.getItemMeta();

        String displayName = config.getString("gui.follow-button.name", "&bToggle Follow");
        meta.setDisplayName(ChatColorUtils.translate(displayName));

        String loreTemplate = config.getString("messages.set-follow", "Set follow = %type%");
        String lore = loreTemplate.replace("%type%", follow ? "true" : "false");
        meta.setLore(Collections.singletonList(ChatColorUtils.translate(lore)));

        followBtn.setItemMeta(meta);
        return followBtn;
    }

    private int getSlotIndex(EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> 0;
            case CHEST -> 1;
            case LEGS -> 2;
            case FEET -> 3;
            default -> -1;
        };
    }

    public void open() {
        player.openInventory(gui);
    }

    public static boolean isBotSettingsGUI(InventoryClickEvent e) {
        return e.getView().getTitle().equals(GUI_TITLE);
    }

    private Material getArmorMaterial(String type, EquipmentSlot slot) {
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
}
