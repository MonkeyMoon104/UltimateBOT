package com.monkey.ultimatebot.gui.v26_2.impl.settings;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.api.event.base.BotEventSource;
import com.monkey.ultimatebot.api.event.state.BotSettingKey;
import com.monkey.ultimatebot.bot.BotOptions;
import com.monkey.ultimatebot.combat.mode.shared.CombatModeLoadoutDefaults;
import com.monkey.ultimatebot.common.model.CombatMode;
import com.monkey.ultimatebot.event.BotSettingEvents;
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
    private final Runnable refreshModeDependents;

    public CombatModeItem(UltimateBot plugin, BotOptions options, Runnable refreshModeDependents) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.options = Objects.requireNonNull(options, "options");
        this.refreshModeDependents = Objects.requireNonNull(refreshModeDependents, "refreshModeDependents");
    }

    @Override
    public ItemProvider getItemProvider(Player viewer) {
        Material material = Material.matchMaterial(options.getCombatModeIconMaterial());
        if (material == null) {
            material = Material.DIAMOND_SWORD;
        }
        return new ItemBuilder(material)
                .setLegacyName(ChatColorUtils.translate("&6Combat mode: &e" + options.getCombatModeDisplayName()))
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
        CombatMode currentMode = options.getCombatMode();
        CombatMode nextMode = options.nextCombatMode(player, clickType.isLeftClick());
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
        if (options.getCombatMode().builtIn()) {
            CombatModeLoadoutDefaults.applyArmor(options, options.getCombatMode());
        }
        plugin.getBotManager().updateArmor(resolveManagedOwnerUUID(player), options.getArmor(), options.getBlast());
        player.sendMessage(ChatColorUtils.translate("&aCombat mode set to &e" + options.getCombatModeDisplayName()));
        refreshModeDependents.run();
        notifyWindows();
    }

    private java.util.UUID resolveManagedOwnerUUID(Player player) {
        if (options.getBotType() != com.monkey.ultimatebot.bot.BotType.TEAM_ALLY) {
            return player.getUniqueId();
        }
        java.util.UUID teamOwnerUUID = plugin.getBotManager().findTeamAllyPrimaryOwner(player.getUniqueId());
        return teamOwnerUUID == null ? player.getUniqueId() : teamOwnerUUID;
    }
}
