package it.coralmc.sandbox.gui.impl;

import it.coralmc.sandbox.SandboxTraining;
import it.coralmc.sandbox.bot.BotOptions;
import it.coralmc.sandbox.bot.ai.rank.BotRank;
import it.coralmc.sandbox.utils.ChatColorUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;

import java.util.Arrays;
import java.util.List;

public class RankItem extends AbstractItem {

    private final SandboxTraining training;
    private final BotOptions options;

    public RankItem(SandboxTraining training, BotOptions options) {
        this.training = training;
        this.options = options;
    }

    @Override
    public ItemProvider getItemProvider() {
        boolean status = options.isCombat();

        ItemBuilder builder = new ItemBuilder(Material.valueOf(training.getConfig().getString("gui.rank-button.material")));
        builder.setDisplayName(ChatColorUtils.translate(training.getConfig().getString("gui.rank-button.name")));
        builder.setItemFlags(List.of(ItemFlag.HIDE_ADDITIONAL_TOOLTIP));

        var loreLines = training.getConfig().getStringList("gui.rank-button.lore");
        List<String> ranks = Arrays.stream(BotRank.values())
                .map(botRank -> botRank == options.getRank() ? botRank.getSelectedName() : botRank.getDisplayName())
                .toList();

        for (String line : loreLines) {
            if (line.contains("%ranks%")) {
                for (String rank : ranks) {
                    builder.addLoreLines(ChatColorUtils.translate("- " + rank));
                }
            } else {
                String processedLine = line.replace("%ranks%", "");
                builder.addLoreLines(ChatColorUtils.translate(processedLine));
            }
        }
        return builder;
    }

    @Override
    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
            BotRank currentRank = options.getRank();
            BotRank[] values = BotRank.values();
            int index = currentRank.ordinal();

            index = (index + 1) % values.length;

            BotRank newRank = values[index];
            options.setRank(newRank);

            training.getBotManager().setBotRank(player.getUniqueId(), newRank);

            player.sendMessage(ChatColorUtils.translate("&aRank impostato su &e" + newRank.getSelectedName()));
        notifyWindows();
    }
}
