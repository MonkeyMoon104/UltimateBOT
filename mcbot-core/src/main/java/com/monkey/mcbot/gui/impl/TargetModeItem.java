package com.monkey.mcbot.gui.impl;

import com.monkey.mcbot.MinecraftBot;
import com.monkey.mcbot.api.event.base.BotEventSource;
import com.monkey.mcbot.api.event.state.BotSettingKey;
import com.monkey.mcbot.api.model.BotTargetMode;
import com.monkey.mcbot.bot.BotOptions;
import com.monkey.mcbot.event.BotSettingEvents;
import com.monkey.mcbot.utils.ChatColorUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;

public class TargetModeItem extends AbstractItem {
    private final MinecraftBot plugin;
    private final BotOptions options;

    public TargetModeItem(MinecraftBot plugin, BotOptions options) {
        this.plugin = plugin;
        this.options = options;
    }

    @Override
    public ItemProvider getItemProvider() {
        BotTargetMode mode = options.getTargetMode();
        return new ItemBuilder(material(mode))
                .setDisplayName(
                        ChatColorUtils.translate(plugin.getLangString("gui.target-mode-button.name", "&bAttack mode")))
                .addLoreLines(ChatColorUtils.translate(
                        plugin.getLangString("gui.target-mode-button.current", "&7Current: &e%mode%")
                                .replace("%mode%", label(mode))))
                .addLoreLines(ChatColorUtils.translate(
                        plugin.getLangString("gui.target-mode-button.click", "&aClick to change")));
    }

    @Override
    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
        if (!clickType.isLeftClick()) {
            return;
        }
        BotTargetMode next = options.getTargetMode().next();
        java.util.UUID ownerUUID = plugin.getBotManager().findTeamAllyPrimaryOwner(player.getUniqueId());
        if (ownerUUID == null) ownerUUID = player.getUniqueId();
        var proposed = BotSettingEvents.propose(
                plugin,
                ownerUUID,
                BotEventSource.GUI,
                BotSettingKey.TARGET_MODE,
                options.getTargetMode(),
                next,
                BotTargetMode.class);
        if (plugin.getBotRegistry().getBot(ownerUUID) != null && proposed.isEmpty()) return;
        if (proposed.isPresent()) next = proposed.get();
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
