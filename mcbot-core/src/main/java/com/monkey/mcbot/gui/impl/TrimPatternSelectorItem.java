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

public class TrimPatternSelectorItem extends AbstractItem {

    private final MinecraftBot training;
    private final BotOptions options;
    private final EquipmentSlot slot;
    private final ArmorItem armorItem;

    public TrimPatternSelectorItem(MinecraftBot training, BotOptions options, EquipmentSlot slot, ArmorItem armorItem) {
        this.training = training;
        this.options = options;
        this.slot = slot;
        this.armorItem = armorItem;
    }

    @Override
    public ItemProvider getItemProvider() {
        String selectedPattern = options.getTrimPatternKey(slot);
        boolean empty = selectedPattern == null;

        Material material = empty
                ? Material.WHITE_STAINED_GLASS_PANE
                : ArmorTrimUtils.resolvePatternDisplayMaterial(selectedPattern);

        ItemBuilder builder = new ItemBuilder(material);
        builder.setDisplayName(ChatColorUtils.translate(training.getLangString(
                empty ? "gui.templates-button.template-empty-name" : "gui.templates-button.template-name",
                empty ? "&fClick to change template" : "&eTemplate: &f%value%"
        ).replace("%value%", ArmorTrimUtils.formatKey(selectedPattern))));

        for (String line : training.getLangStringList("gui.templates-button.template-lore")) {
            builder.addLoreLines(ChatColorUtils.translate(
                    line.replace("%value%", empty
                            ? training.getLangString("gui.templates-button.empty-value", "None")
                            : ArmorTrimUtils.formatKey(selectedPattern))
            ));
        }

        return builder;
    }

    @Override
    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
        boolean forward = !clickType.isRightClick();
        options.setTrimPatternKey(slot, ArmorTrimUtils.getNextTrimPatternKey(options.getTrimPatternKey(slot), forward));
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
