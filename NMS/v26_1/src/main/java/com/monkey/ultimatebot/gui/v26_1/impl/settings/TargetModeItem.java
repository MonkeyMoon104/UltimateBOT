package com.monkey.ultimatebot.gui.v26_1.impl.settings;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.api.model.configuration.BotTargetMode;
import com.monkey.ultimatebot.bot.BotOptions;
import com.monkey.ultimatebot.utils.ChatColorUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.Click;
import xyz.xenondevs.invui.item.AbstractItem;
import xyz.xenondevs.invui.item.ItemBuilder;
import xyz.xenondevs.invui.item.ItemProvider;

public class TargetModeItem extends AbstractItem {
    private final UltimateBot plugin;
    private final BotOptions options;

    public TargetModeItem(UltimateBot plugin, BotOptions options) {
        this.plugin = plugin;
        this.options = options;
    }

    @Override
    public ItemProvider getItemProvider(Player viewer) {
        BotTargetMode mode = options.getTargetMode();
        return new ItemBuilder(material(mode))
                .setLegacyName(
                        ChatColorUtils.translate(plugin.getLangString("gui.target-mode-button.name", "&bAttack mode")))
                .addLegacyLoreLines(ChatColorUtils.translate(
                        plugin.getLangString("gui.target-mode-button.current", "&7Current: &e%mode%")
                                .replace("%mode%", label(mode))))
                .addLegacyLoreLines(ChatColorUtils.translate(
                        plugin.getLangString("gui.target-mode-button.click", "&aClick to change")));
    }

    @Override
    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull Click click) {
        if (!clickType.isLeftClick()) return;
        BotTargetMode next = options.getTargetMode().next();
        options.setTargetMode(next);
        player.sendMessage(ChatColorUtils.translate("&aAttack mode: &e" + label(next)));
        notifyWindows();
    }

    private static Material material(BotTargetMode mode) {
        return switch (mode) {
            case PLAYERS -> Material.PLAYER_HEAD;
            case MOBS -> Material.PIGLIN_HEAD;
            case PLAYERS_AND_MOBS -> Material.TARGET;
        };
    }

    private String label(BotTargetMode mode) {
        return switch (mode) {
            case PLAYERS -> plugin.getLangString("gui.target-mode-button.players", "Players");
            case MOBS -> plugin.getLangString("gui.target-mode-button.mobs", "Mobs");
            case PLAYERS_AND_MOBS -> plugin.getLangString("gui.target-mode-button.both", "Players + mobs");
        };
    }
}
