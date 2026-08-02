package com.monkey.ultimatebot.gui.impl;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.api.event.base.BotEventSource;
import com.monkey.ultimatebot.api.event.state.BotSettingKey;
import com.monkey.ultimatebot.bot.BotOptions;
import com.monkey.ultimatebot.common.model.CombatMode;
import com.monkey.ultimatebot.event.BotSettingEvents;
import com.monkey.ultimatebot.utils.ChatColorUtils;
import java.util.List;
import java.util.Objects;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;

public final class CombatModeItem extends AbstractItem {
    private final UltimateBot plugin;
    private final BotOptions options;

    public CombatModeItem(UltimateBot plugin, BotOptions options) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.options = Objects.requireNonNull(options, "options");
    }

    @Override
    public ItemProvider getItemProvider() {
        CombatMode mode = options.getCombatMode();
        String configuredMaterial =
                plugin.getCombatProfileCatalog().configuration(mode).iconMaterial();
        Material material = Material.matchMaterial(configuredMaterial);
        if (material == null) {
            material = Material.DIAMOND_SWORD;
        }
        ItemBuilder builder = new ItemBuilder(material)
                .setDisplayName(ChatColorUtils.translate("&6Combat mode: &e" + mode.displayName()))
                .setItemFlags(List.of(ItemFlag.HIDE_ADDITIONAL_TOOLTIP, ItemFlag.HIDE_ATTRIBUTES));
        builder.addLoreLines(
                ChatColorUtils.translate(
                        "&7Difficulty: &f" + options.getDifficulty().name()),
                ChatColorUtils.translate("&7Profile: "
                        + (options.getCustomCombatTuning() == null ? "&aServer default" : "&eCustomized")),
                "",
                ChatColorUtils.translate("&eLeft click: &7next mode"),
                ChatColorUtils.translate("&eRight click: &7previous mode"),
                ChatColorUtils.translate("&8Use the Combat Settings tab for tuning"));
        return builder;
    }

    @Override
    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
        if (!options.isChangeableCombatMode()) {
            player.sendMessage(ChatColorUtils.translate("&cThe combat mode is locked for this bot."));
            return;
        }
        if (!clickType.isLeftClick() && !clickType.isRightClick()) {
            return;
        }
        CombatMode currentMode = options.getCombatMode();
        CombatMode nextMode = options.nextCombatMode(clickType.isLeftClick());
        var proposed = BotSettingEvents.propose(
                plugin,
                player.getUniqueId(),
                BotEventSource.GUI,
                BotSettingKey.COMBAT_MODE,
                currentMode,
                nextMode,
                CombatMode.class);
        if (proposed.isEmpty()) {
            return;
        }
        options.setCombatMode(proposed.get());
        player.sendMessage(ChatColorUtils.translate(
                "&aCombat mode set to &e" + options.getCombatMode().displayName()));
        notifyWindows();
    }
}
