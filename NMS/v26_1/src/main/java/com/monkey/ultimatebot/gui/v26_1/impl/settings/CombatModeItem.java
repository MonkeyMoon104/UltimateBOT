package com.monkey.ultimatebot.gui.v26_1.impl.settings;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.bot.BotOptions;
import com.monkey.ultimatebot.common.model.CombatMode;
import com.monkey.ultimatebot.utils.ChatColorUtils;
import java.util.Objects;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.Click;
import xyz.xenondevs.invui.item.AbstractItem;
import xyz.xenondevs.invui.item.ItemBuilder;
import xyz.xenondevs.invui.item.ItemProvider;

public final class CombatModeItem extends AbstractItem {
    private final UltimateBot plugin;
    private final BotOptions options;

    public CombatModeItem(UltimateBot plugin, BotOptions options) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.options = Objects.requireNonNull(options, "options");
    }

    @Override
    public ItemProvider getItemProvider(Player viewer) {
        CombatMode mode = options.getCombatMode();
        Material material = Material.matchMaterial(
                plugin.getCombatProfileCatalog().configuration(mode).iconMaterial());
        if (material == null) {
            material = Material.DIAMOND_SWORD;
        }
        return new ItemBuilder(material)
                .setLegacyName(ChatColorUtils.translate("&6Combat mode: &e" + mode.displayName()))
                .addLegacyLoreLines(
                        ChatColorUtils.translate(
                                "&7Difficulty: &f" + options.getDifficulty().name()),
                        ChatColorUtils.translate("&7Profile: "
                                + (options.getCustomCombatTuning() == null ? "&aServer default" : "&eCustomized")),
                        "",
                        ChatColorUtils.translate("&eLeft click: &7next mode"),
                        ChatColorUtils.translate("&eRight click: &7previous mode"));
    }

    @Override
    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull Click click) {
        if (!options.isChangeableCombatMode()) {
            player.sendMessage(ChatColorUtils.translate("&cThe combat mode is locked for this bot."));
            return;
        }
        if (!clickType.isLeftClick() && !clickType.isRightClick()) {
            return;
        }
        options.setCombatMode(options.nextCombatMode(clickType.isLeftClick()));
        player.sendMessage(ChatColorUtils.translate(
                "&aCombat mode set to &e" + options.getCombatMode().displayName()));
        notifyWindows();
    }
}
