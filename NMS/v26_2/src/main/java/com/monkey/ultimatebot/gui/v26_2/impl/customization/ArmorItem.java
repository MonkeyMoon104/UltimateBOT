package com.monkey.ultimatebot.gui.v26_2.impl.customization;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.bot.BotOptions;
import com.monkey.ultimatebot.bot.BotType;
import com.monkey.ultimatebot.common.model.EquipmentSlotKind;
import com.monkey.ultimatebot.utils.ChatColorUtils;
import com.monkey.ultimatebot.utils.armor.ArmorCycle;
import com.monkey.ultimatebot.utils.equipment.ArmorTrimUtils;
import com.monkey.ultimatebot.utils.equipment.BotEquipmentUtils;
import java.util.Locale;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.Click;
import xyz.xenondevs.invui.item.AbstractItem;
import xyz.xenondevs.invui.item.ItemBuilder;
import xyz.xenondevs.invui.item.ItemProvider;

public class ArmorItem extends AbstractItem {

    private final UltimateBot training;
    private final EquipmentSlotKind slot;
    private final BotOptions options;
    private ItemStack piece;

    public ArmorItem(UltimateBot training, EquipmentSlotKind slot, ItemStack piece, BotOptions options) {
        this.training = training;
        this.slot = slot;
        this.piece = piece;
        this.options = options;
    }

    @Override
    public ItemProvider getItemProvider(Player viewer) {
        piece = options.getArmor().getOrDefault(slot, piece);
        ItemStack displayPiece = new ItemStack(piece.getType(), Math.max(1, piece.getAmount()));
        ArmorTrimUtils.applyTrim(displayPiece, options.getTrimPatternKey(slot), options.getTrimMaterialKey(slot));
        BotEquipmentUtils.applyArmorEnchants(displayPiece, isBlastEnabled());

        ItemBuilder builder = new ItemBuilder(displayPiece);
        var loreLines = training.getLangStringList("gui.default-armor.lore.set-type");

        String typeName = formatMaterialName(piece.getType());
        String blastState = ChatColorUtils.translate(
                isBlastEnabled()
                        ? training.getLangString("gui.default-armor.blast-enabled-text", "&aON")
                        : training.getLangString("gui.default-armor.blast-disabled-text", "&cOFF"));

        for (String line : loreLines) {
            String coloredLine =
                    ChatColorUtils.translate(line.replace("%type%", typeName).replace("%blast_state%", blastState));
            builder.addLegacyLoreLines(coloredLine);
        }

        return builder;
    }

    @Override
    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull Click click) {
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
            String msg = training.getLangString(
                    "messages.armor-locked", "&cArmor is locked: it cannot be modified for this bot.");
            player.sendMessage(ChatColorUtils.translate(msg));
            return;
        }

        Material next =
                ArmorCycle.getNextArmor(piece.getType(), slot, options.getMinArmorTier(), options.getMaxArmorTier());

        ItemStack updated = createUpdatedPiece(next, isBlastEnabled());
        applyUpdatedPiece(player, updated);
    }

    private void handleBlastToggleClick(Player player) {
        if (!options.isChangeableBlast()) {
            String msg = training.getLangString(
                    "messages.blast-locked", "&cBlast protection is locked: it cannot be modified for this bot.");
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
        ItemStack updated = new ItemStack(material);
        ArmorTrimUtils.applyTrim(updated, options.getTrimPatternKey(slot), options.getTrimMaterialKey(slot));
        BotEquipmentUtils.applyArmorEnchants(updated, blastEnabled);
        return updated;
    }

    private boolean isBlastEnabled() {
        return options.getBlast().getOrDefault(slot, false);
    }

    private String formatMaterialName(Material material) {
        String[] parts = material.name().toLowerCase(Locale.ROOT).split("_", -1);
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
