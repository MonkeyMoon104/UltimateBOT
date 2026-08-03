package com.monkey.ultimatebot.gui.v26_1.impl;

import com.monkey.ultimatebot.bot.BotOptions;
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

public final class ResetCombatTuningItem extends AbstractItem {
    private final BotOptions options;

    public ResetCombatTuningItem(BotOptions options) {
        this.options = Objects.requireNonNull(options, "options");
    }

    @Override
    public ItemProvider getItemProvider(Player viewer) {
        return new ItemBuilder(Material.BARRIER)
                .setLegacyName(ChatColorUtils.translate("&cReset combat profile"))
                .addLegacyLoreLines(
                        ChatColorUtils.translate("&7Restore the server profile for"),
                        ChatColorUtils.translate("&e" + options.getCombatMode().displayName()
                                + " &7/ &e"
                                + options.getDifficulty().name()),
                        "",
                        ChatColorUtils.translate("&eClick to reset"));
    }

    @Override
    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull Click click) {
        options.resetCombatTuning();
        player.sendMessage(ChatColorUtils.translate("&aCombat profile restored to the server defaults."));
        notifyWindows();
    }
}
