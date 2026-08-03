package com.monkey.ultimatebot.gui.impl.settings;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.api.event.base.BotEventSource;
import com.monkey.ultimatebot.api.event.state.BotSettingKey;
import com.monkey.ultimatebot.bot.BotOptions;
import com.monkey.ultimatebot.common.model.CombatTuning;
import com.monkey.ultimatebot.event.BotSettingEvents;
import com.monkey.ultimatebot.gui.combat.CombatTuningProperty;
import com.monkey.ultimatebot.utils.ChatColorUtils;
import java.util.List;
import java.util.Objects;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;

public final class CombatTuningItem extends AbstractItem {
    private final UltimateBot plugin;
    private final BotOptions options;
    private final CombatTuningProperty property;

    public CombatTuningItem(UltimateBot plugin, BotOptions options, CombatTuningProperty property) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.options = Objects.requireNonNull(options, "options");
        this.property = Objects.requireNonNull(property, "property");
    }

    @Override
    public ItemProvider getItemProvider() {
        CombatTuning tuning = options.getCombatTuning();
        return new ItemBuilder(property.material())
                .setDisplayName(ChatColorUtils.translate("&e" + property.displayName()))
                .setItemFlags(List.of(ItemFlag.HIDE_ADDITIONAL_TOOLTIP, ItemFlag.HIDE_ATTRIBUTES))
                .addLoreLines(
                        ChatColorUtils.translate("&7Value: &f" + property.formattedValue(tuning)),
                        "",
                        ChatColorUtils.translate("&eLeft click: &7increase"),
                        ChatColorUtils.translate("&eRight click: &7decrease"),
                        ChatColorUtils.translate("&eShift click: &7larger step"));
    }

    @Override
    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
        if (!clickType.isLeftClick() && !clickType.isRightClick()) {
            return;
        }
        CombatTuning currentTuning = options.getCombatTuning();
        CombatTuning nextTuning = property.adjust(currentTuning, clickType.isLeftClick(), clickType.isShiftClick());
        var proposed = BotSettingEvents.propose(
                plugin,
                player.getUniqueId(),
                BotEventSource.GUI,
                BotSettingKey.COMBAT_TUNING,
                currentTuning,
                nextTuning,
                CombatTuning.class);
        if (proposed.isEmpty()) {
            return;
        }
        options.setCustomCombatTuning(proposed.get());
        notifyWindows();
    }
}
