package com.monkey.mcbot.gui.impl;

import com.monkey.mcbot.MinecraftBot;
import com.monkey.mcbot.bot.BotOptions;
import com.monkey.mcbot.bot.BotType;
import com.monkey.mcbot.utils.ChatColorUtils;
import com.monkey.mcbot.utils.armor.ArmorCycle;
import com.monkey.mcbot.utils.equipment.BotEquipmentUtils;
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
import java.util.UUID;

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
        ItemStack displayPiece = piece.clone();
        BotEquipmentUtils.applyArmorEnchants(displayPiece, isBlastEnabled());

        ItemBuilder builder = new ItemBuilder(displayPiece);
        builder.setItemFlags(List.of(ItemFlag.HIDE_ADDITIONAL_TOOLTIP, ItemFlag.HIDE_ENCHANTS));

        var loreLines = training.getConfig().getStringList("gui.default-armor.lore.set-type");

        String typeName = formatMaterialName(piece.getType());
        String blastState = ChatColorUtils.translate(
                isBlastEnabled()
                        ? training.getConfig().getString("gui.default-armor.blast-enabled-text", "&aON")
                        : training.getConfig().getString("gui.default-armor.blast-disabled-text", "&cOFF")
        );

        for (String line : loreLines) {
            String coloredLine = ChatColorUtils.translate(
                    line.replace("%type%", typeName)
                            .replace("%blast_state%", blastState)
            );
            builder.addLoreLines(coloredLine);
        }

        return builder;
    }


    @Override
    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
        if (clickType.isLeftClick()) {
            handleArmorCycleClick(player);
            return;
        }

        if (clickType.isRightClick()) {
            handleBlastToggleClick(player);
        }
    }

    private void handleArmorCycleClick(Player player) {
        if (!options.isChangeableArmor()) {
            String msg = training.getConfig().getString("messages.armor-locked", "&cArmor is locked: it cannot be modified for this bot.");
            player.sendMessage(ChatColorUtils.translate(msg));
            return;
        }

        Material next = ArmorCycle.getNextArmor(
                piece.getType(),
                slot,
                options.getMinArmorTier(),
                options.getMaxArmorTier()
        );

        ItemStack updated = createUpdatedPiece(next, isBlastEnabled());
        applyUpdatedPiece(player, updated);
    }

    private void handleBlastToggleClick(Player player) {
        if (!options.isChangeableBlast()) {
            String msg = training.getConfig().getString("messages.blast-locked", "&cBlast protection is locked: it cannot be modified for this bot.");
            player.sendMessage(ChatColorUtils.translate(msg));
            return;
        }

        boolean updatedBlastState = !isBlastEnabled();
        options.getBlast().put(slot, updatedBlastState);
        ItemStack updated = createUpdatedPiece(piece.getType(), updatedBlastState);
        applyUpdatedPiece(player, updated);
    }

    private void applyUpdatedPiece(Player player, ItemStack updated) {
        options.getArmor().put(slot, updated);
        training.getBotManager().updateArmor(resolveManagedOwnerUUID(player), options.getArmor(), options.getBlast());
        piece = updated;
        notifyWindows();
    }

    private ItemStack createUpdatedPiece(Material material, boolean blastEnabled) {
        ItemStack updated = piece.withType(material);
        BotEquipmentUtils.applyArmorEnchants(updated, blastEnabled);
        return updated;
    }

    private boolean isBlastEnabled() {
        return options.getBlast().getOrDefault(slot, false);
    }

    private String formatMaterialName(Material material) {
        String[] parts = material.name().toLowerCase().split("_");
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (part.isEmpty()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }

        return builder.toString();
    }

    private UUID resolveManagedOwnerUUID(Player player) {
        if (options.getBotType() != BotType.TEAM_ALLY) {
            return player.getUniqueId();
        }
        UUID teamOwnerUUID = training.getBotManager().findTeamAllyPrimaryOwner(player.getUniqueId());
        return teamOwnerUUID == null ? player.getUniqueId() : teamOwnerUUID;
    }
}
