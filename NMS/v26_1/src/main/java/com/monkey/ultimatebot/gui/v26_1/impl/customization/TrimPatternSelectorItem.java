package com.monkey.ultimatebot.gui.v26_1.impl.customization;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.bot.BotOptions;
import com.monkey.ultimatebot.bot.BotType;
import com.monkey.ultimatebot.common.model.bot.EquipmentSlotKind;
import com.monkey.ultimatebot.utils.ChatColorUtils;
import com.monkey.ultimatebot.utils.equipment.ArmorTrimUtils;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import com.monkey.ultimatebot.libs.invui.Click;
import com.monkey.ultimatebot.libs.invui.item.AbstractItem;
import com.monkey.ultimatebot.libs.invui.item.ItemBuilder;
import com.monkey.ultimatebot.libs.invui.item.ItemProvider;

public class TrimPatternSelectorItem extends AbstractItem {

    private final UltimateBot training;
    private final BotOptions options;
    private final EquipmentSlotKind slot;
    private final ArmorItem armorItem;

    public TrimPatternSelectorItem(
            UltimateBot training, BotOptions options, EquipmentSlotKind slot, ArmorItem armorItem) {
        this.training = training;
        this.options = options;
        this.slot = slot;
        this.armorItem = armorItem;
    }

    @Override
    public ItemProvider getItemProvider(Player viewer) {
        String selectedPattern = options.getTrimPatternKey(slot);
        boolean empty = selectedPattern == null;

        Material material = empty
                ? Material.WHITE_STAINED_GLASS_PANE
                : ArmorTrimUtils.resolvePatternDisplayMaterial(selectedPattern);

        ItemBuilder builder = new ItemBuilder(material);
        builder.setLegacyName(ChatColorUtils.translate(training.getLangString(
                        empty ? "gui.templates-button.template-empty-name" : "gui.templates-button.template-name",
                        empty ? "&fClick to change template" : "&eTemplate: &f%value%")
                .replace("%value%", ArmorTrimUtils.formatKey(selectedPattern))));

        for (String line : training.getLangStringList("gui.templates-button.template-lore")) {
            builder.addLegacyLoreLines(ChatColorUtils.translate(line.replace(
                    "%value%",
                    empty
                            ? training.getLangString("gui.templates-button.empty-value", "None")
                            : ArmorTrimUtils.formatKey(selectedPattern))));
        }

        return builder;
    }

    @Override
    public void handleClick(ClickType clickType, Player player, Click click) {
        boolean forward = !clickType.isRightClick();
        options.setTrimPatternKey(slot, ArmorTrimUtils.getNextTrimPatternKey(options.getTrimPatternKey(slot), forward));
        training.getBotManager().updateArmor(resolveManagedOwnerUUID(player), options.getArmor(), options.getBlast());
        armorItem.notifyWindows();
        notifyWindows();
    }

    private UUID resolveManagedOwnerUUID(Player player) {
        if (options.getBotType() != BotType.TEAM_ALLY) {
            return player.getUniqueId();
        }
        UUID teamOwnerUUID = training.getBotManager().findTeamAllyPrimaryOwner(player.getUniqueId());
        return teamOwnerUUID == null ? player.getUniqueId() : teamOwnerUUID;
    }
}
