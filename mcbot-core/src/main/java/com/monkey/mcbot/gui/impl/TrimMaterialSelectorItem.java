package com.monkey.mcbot.gui.impl;

import com.monkey.mcbot.MinecraftBot;
import com.monkey.mcbot.bot.BotOptions;
import org.bukkit.inventory.EquipmentSlot;
import com.monkey.mcbot.utils.ChatColorUtils;
import com.monkey.mcbot.utils.equipment.ArmorTrimUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;

public class TrimMaterialSelectorItem extends AbstractItem {

    private final MinecraftBot training;
    private final BotOptions options;
    private final EquipmentSlot slot;
    private final ArmorItem armorItem;

    public TrimMaterialSelectorItem(MinecraftBot training, BotOptions options, EquipmentSlot slot, ArmorItem armorItem) {
        this.training = training;
        this.options = options;
        this.slot = slot;
        this.armorItem = armorItem;
    }

    @Override
    public ItemProvider getItemProvider() {
        String selectedMaterial = options.getTrimMaterialKey(slot);
        boolean empty = selectedMaterial == null;

        Material material = empty
                ? Material.WHITE_STAINED_GLASS_PANE
                : ArmorTrimUtils.resolveTrimMaterialDisplayMaterial(selectedMaterial);

        ItemBuilder builder = new ItemBuilder(material);
        builder.setDisplayName(ChatColorUtils.translate(training.getLangString(
                empty ? "gui.templates-button.ore-empty-name" : "gui.templates-button.ore-name",
                empty ? "&fClick to change ore" : "&eOre: &f%value%"
        ).replace("%value%", ArmorTrimUtils.formatKey(selectedMaterial))));

        for (String line : training.getLangStringList("gui.templates-button.ore-lore")) {
            builder.addLoreLines(ChatColorUtils.translate(
                    line.replace("%value%", empty
                            ? training.getLangString("gui.templates-button.empty-value", "None")
                            : ArmorTrimUtils.formatKey(selectedMaterial))
            ));
        }

        return builder;
    }

    @Override
    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
        boolean forward = !clickType.isRightClick();
        options.setTrimMaterialKey(slot, ArmorTrimUtils.getNextTrimMaterialKey(options.getTrimMaterialKey(slot), forward));
        training.getBotManager().updateArmor(resolveManagedOwnerUUID(player), options.getArmor(), options.getBlast());
        armorItem.notifyWindows();
        notifyWindows();
    }

    private java.util.UUID resolveManagedOwnerUUID(Player player) {
        if (options.getBotType() != com.monkey.mcbot.bot.BotType.TEAM_ALLY) {
            return player.getUniqueId();
        }
        java.util.UUID teamOwnerUUID = training.getBotManager().findTeamAllyPrimaryOwner(player.getUniqueId());
        return teamOwnerUUID == null ? player.getUniqueId() : teamOwnerUUID;
    }
}
