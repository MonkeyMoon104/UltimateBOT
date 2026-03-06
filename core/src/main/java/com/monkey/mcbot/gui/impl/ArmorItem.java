package com.monkey.mcbot.gui.impl;

import com.monkey.mcbot.MinecraftBot;
import com.monkey.mcbot.bot.BotOptions;
import com.monkey.mcbot.utils.ChatColorUtils;
import com.monkey.mcbot.utils.armor.ArmorCycle;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;

import java.util.List;

public class ArmorItem extends AbstractItem {

    private final MinecraftBot training;
    private final EquipmentSlot slot;
    private final BotOptions options;
    private ItemStack piece;

    public ArmorItem(MinecraftBot training, EquipmentSlot slot, ItemStack piece, BotOptions options) {
        this.training = training;
        this.slot = slot;
        this.piece = piece;
        this.options = options;
    }

    @Override
    public ItemProvider getItemProvider() {
        ItemBuilder builder = new ItemBuilder(piece);
        builder.setItemFlags(List.of(ItemFlag.HIDE_ADDITIONAL_TOOLTIP));

        var loreLines = training.getConfig().getStringList("gui.default-armor.lore.set-type");

        String typeName = piece.getType().name();

        for (String line : loreLines) {
            String coloredLine = ChatColorUtils.translate(line.replace("%type%", typeName));
            builder.addLoreLines(coloredLine);
        }

        return builder;
    }


    @Override
    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
        Material current = piece.getType();
        Material next = ArmorCycle.getNextArmor(current, slot);

        ItemStack updated = piece.withType(next);

        options.getArmor().put(slot, updated);
        training.getBotManager().updateArmor(player.getUniqueId(), options.getArmor());
        piece = updated;

        notifyWindows();
    }

    public void setPiece(ItemStack piece) {
        this.piece = piece;
    }
}
