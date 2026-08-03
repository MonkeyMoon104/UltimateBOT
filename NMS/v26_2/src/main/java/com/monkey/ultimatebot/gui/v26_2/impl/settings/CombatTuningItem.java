package com.monkey.ultimatebot.gui.v26_2.impl.settings;

import com.monkey.ultimatebot.bot.BotOptions;
import com.monkey.ultimatebot.common.model.CombatTuning;
import com.monkey.ultimatebot.gui.combat.CombatTuningProperty;
import com.monkey.ultimatebot.utils.ChatColorUtils;
import java.util.Objects;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.Click;
import xyz.xenondevs.invui.item.AbstractItem;
import xyz.xenondevs.invui.item.ItemBuilder;
import xyz.xenondevs.invui.item.ItemProvider;

public final class CombatTuningItem extends AbstractItem {
    private final BotOptions options;
    private final CombatTuningProperty property;

    public CombatTuningItem(BotOptions options, CombatTuningProperty property) {
        this.options = Objects.requireNonNull(options, "options");
        this.property = Objects.requireNonNull(property, "property");
    }

    @Override
    public ItemProvider getItemProvider(Player viewer) {
        return new ItemBuilder(property.material())
                .setLegacyName(ChatColorUtils.translate("&e" + property.displayName()))
                .addLegacyLoreLines(
                        ChatColorUtils.translate("&7Value: &f" + property.formattedValue(options.getCombatTuning())),
                        "",
                        ChatColorUtils.translate("&eLeft click: &7increase"),
                        ChatColorUtils.translate("&eRight click: &7decrease"),
                        ChatColorUtils.translate("&eShift click: &7larger step"));
    }

    @Override
    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull Click click) {
        if (!clickType.isLeftClick() && !clickType.isRightClick()) {
            return;
        }
        CombatTuning tuning =
                property.adjust(options.getCombatTuning(), clickType.isLeftClick(), clickType.isShiftClick());
        options.setCustomCombatTuning(tuning);
        notifyWindows();
    }
}
