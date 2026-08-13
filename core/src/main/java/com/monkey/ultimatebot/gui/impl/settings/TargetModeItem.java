package com.monkey.ultimatebot.gui.impl.settings;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.api.event.base.BotEventSource;
import com.monkey.ultimatebot.api.event.state.BotSettingKey;
import com.monkey.ultimatebot.bot.BotOptions;
import com.monkey.ultimatebot.common.model.BotTargetMode;
import com.monkey.ultimatebot.compat.MinecraftVersionAccess;
import com.monkey.ultimatebot.event.BotSettingEvents;
import com.monkey.ultimatebot.utils.ChatColorUtils;
import com.monkey.ultimatebot.utils.material.MaterialCatalog;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;

public class TargetModeItem extends AbstractItem {
    private final UltimateBot plugin;
    private final BotOptions options;

    public TargetModeItem(UltimateBot plugin, BotOptions options) {
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
        java.util.Optional<com.monkey.ultimatebot.common.model.BotTargetMode> proposed = BotSettingEvents.propose(
                plugin,
                ownerUUID,
                BotEventSource.GUI,
                BotSettingKey.TARGET_MODE,
                options.getTargetMode(),
                next,
                BotTargetMode.class);
        if (plugin.getBotRegistry().getBot(ownerUUID) != null && !proposed.isPresent()) return;
        if (proposed.isPresent()) next = proposed.get();
        options.setTargetMode(next);
        resetLiveBotNavigation(ownerUUID);
        player.sendMessage(ChatColorUtils.translate("&aAttack mode: &e" + label(next)));
        notifyWindows();
    }

    private void resetLiveBotNavigation(java.util.UUID ownerUUID) {
        com.monkey.ultimatebot.bot.ai.ITrainingBot bot = plugin.getBotManager().getBotSafe(ownerUUID);
        if (bot == null || bot.getBotAI() == null) {
            return;
        }
        bot.getBotAI().getMovementController().clearPath();
        bot.getBotAI().clearActivePathfinding();
    }

    private static Material material(BotTargetMode mode) {
                switch (mode) {
            case PLAYERS:
                return Material.PLAYER_HEAD;
            case MOBS:
                // Piglin heads are 1.20+; on 1.19.x the material can exist as a disabled stub
                // and shows vanilla "Item disabled" / "Oggetto disattivato" in the lore.
                if (MinecraftVersionAccess.isAtLeast(1, 20)) {
                    return MaterialCatalog.optional("PIGLIN_HEAD", Material.ZOMBIE_HEAD);
                }
                return MaterialCatalog.optional("ZOMBIE_HEAD", Material.ROTTEN_FLESH);
            case PLAYERS_AND_MOBS:
                return Material.TARGET;
        }
        throw new IllegalStateException("Unexpected switch value");
    }

    private String label(BotTargetMode mode) {
                switch (mode) {
            case PLAYERS:
                return plugin.getLangString("gui.target-mode-button.players", "Players");
            case MOBS:
                return plugin.getLangString("gui.target-mode-button.mobs", "Mobs");
            case PLAYERS_AND_MOBS:
                return plugin.getLangString("gui.target-mode-button.both", "Players + mobs");
        }
        throw new IllegalStateException("Unexpected switch value");
    }
}
