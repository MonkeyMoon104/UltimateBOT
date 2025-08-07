package it.coralmc.sandbox.gui;

import it.coralmc.sandbox.bot.BotSpawner;
import it.coralmc.sandbox.bot.util.TrainingBot;
import it.coralmc.sandbox.bot.util.entity.BotEntityFinder;
import it.coralmc.sandbox.gui.builder.armor.ArmorUtils;
import it.coralmc.sandbox.gui.builder.GUIItemBuilder;
import it.coralmc.sandbox.gui.validator.GUIValidator;
import it.coralmc.sandbox.utils.builder.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;

import java.util.EnumMap;
import java.util.Map;

public class BotSettingsGUI {

    private final Player player;
    private final Inventory gui;
    private final Map<EquipmentSlot, Material> selectedArmor = new EnumMap<>(EquipmentSlot.class);
    private final GUIItemBuilder itemBuilder;
    private boolean follow;

    public BotSettingsGUI(Player player) {
        this.player = player;
        this.itemBuilder = new GUIItemBuilder();
        this.gui = Bukkit.createInventory(null, 18, GUIValidator.getBotSettingsGUITitle());

        initializeArmor();
        setupGUIItems();
    }

    private void initializeArmor() {
        Map<EquipmentSlot, Material> initialArmor = ArmorUtils.initializePlayerArmor(player.getUniqueId());

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (!ArmorUtils.isValidArmorSlot(slot)) continue;

            Material material = initialArmor.get(slot);
            if (material != null) {
                selectedArmor.put(slot, material);
                gui.setItem(ArmorUtils.getSlotIndex(slot), itemBuilder.createArmorItem(material));
            }
        }
    }

    private void setupGUIItems() {
        if (BotSpawner.isBotSpawned(player.getUniqueId())) {
            TrainingBot bot = BotEntityFinder.getBotByOwnerUUID(player.getUniqueId());
            if (bot != null) {
                this.follow = bot.isFollow();
            }
        }

        gui.setItem(7, itemBuilder.createFollowButton(follow));

        int totemCount = getCorrectTotemCount();
        gui.setItem(11, ItemBuilder.createTotemButton(totemCount));

        if (BotSpawner.isBotSpawned(player.getUniqueId())) {
            gui.setItem(8, itemBuilder.createDespawnButton());
            gui.setItem(9, itemBuilder.createSaveButton());
        } else {
            gui.setItem(8, itemBuilder.createSpawnButton());
        }
    }

    public void open() {
        player.openInventory(gui);
    }

    public static boolean isBotSettingsGUI(InventoryClickEvent event) {
        return GUIValidator.isBotSettingsGUI(event);
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
        if (ArmorUtils.isValidArmorSlot(slot)) {
            selectedArmor.put(slot, material);
            gui.setItem(ArmorUtils.getSlotIndex(slot), itemBuilder.createArmorItem(material));
        }
    }

    private int getCorrectTotemCount() {
        if (BotSpawner.isBotSpawned(player.getUniqueId())) {
            TrainingBot bot = BotEntityFinder.getBotByOwnerUUID(player.getUniqueId());
            if (bot != null) {
                return bot.getTotemCount();
            }
        }

        return 37;
    }
}