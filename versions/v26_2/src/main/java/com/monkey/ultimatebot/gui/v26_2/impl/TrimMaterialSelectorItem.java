package com.monkey.ultimatebot.gui.v26_2.impl;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.bot.BotOptions;
import com.monkey.ultimatebot.bot.BotType;
import com.monkey.ultimatebot.utils.ChatColorUtils;
import com.monkey.ultimatebot.utils.equipment.ArmorTrimUtils;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.EquipmentSlot;
import xyz.xenondevs.invui.Click;
import xyz.xenondevs.invui.item.AbstractItem;
import xyz.xenondevs.invui.item.ItemBuilder;
import xyz.xenondevs.invui.item.ItemProvider;

public class TrimMaterialSelectorItem extends AbstractItem {

    private final UltimateBot training;
    private final BotOptions options;
    private final EquipmentSlot slot;
    private final ArmorItem armorItem;

    public TrimMaterialSelectorItem(UltimateBot training, BotOptions options, EquipmentSlot slot, ArmorItem armorItem) {
        this.training = training;
        this.options = options;
        this.slot = slot;
        this.armorItem = armorItem;
    }

    @Override
    public ItemProvider getItemProvider(Player viewer) {
        String selectedMaterial = options.getTrimMaterialKey(slot);
        boolean empty = selectedMaterial == null;

        Material material = empty
                ? Material.WHITE_STAINED_GLASS_PANE
                : ArmorTrimUtils.resolveTrimMaterialDisplayMaterial(selectedMaterial);

        ItemBuilder builder = new ItemBuilder(material);
        builder.setLegacyName(ChatColorUtils.translate(training.getLangString(
                        empty ? "gui.templates-button.ore-empty-name" : "gui.templates-button.ore-name",
                        empty ? "&fClick to change ore" : "&eOre: &f%value%")
                .replace("%value%", ArmorTrimUtils.formatKey(selectedMaterial))));

        for (String line : training.getLangStringList("gui.templates-button.ore-lore")) {
            builder.addLegacyLoreLines(ChatColorUtils.translate(line.replace(
                    "%value%",
                    empty
                            ? training.getLangString("gui.templates-button.empty-value", "None")
                            : ArmorTrimUtils.formatKey(selectedMaterial))));
        }

        return builder;
    }

    @Override
    public void handleClick(ClickType clickType, Player player, Click click) {
        boolean forward = !clickType.isRightClick();
        options.setTrimMaterialKey(
                slot, ArmorTrimUtils.getNextTrimMaterialKey(options.getTrimMaterialKey(slot), forward));
        training.getBotManager().updateArmor(resolveManagedOwnerUUID(player), options.getArmor(), options.getBlast());
        armorItem.notifyWindows();
        notifyWindows();
    }

    private UUID resolveManagedOwnerUUID(Player player) {
        if (options.getBotType() != BotType.TEAM_ALLY) {
            return player.getUniqueId();
        }
        UUID teamOwnerUUID = training.getBotManager().findTeamAllyPrimaryOwner(player.getUniqueId());
        return teamOwnerUUID == null ? player.getUniqueId() : teamOwnerUUID;
    }
}
