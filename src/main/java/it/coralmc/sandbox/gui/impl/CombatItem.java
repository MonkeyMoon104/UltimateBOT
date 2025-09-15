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

import java.util.List;

public class CombatItem extends AbstractItem {

    private final SandboxTraining training;
    private final BotOptions options;

    public CombatItem(SandboxTraining training, BotOptions options) {
        this.training = training;
        this.options = options;
    }

    @Override
    public ItemProvider getItemProvider() {
        boolean status = options.isCombat();

        ItemBuilder builder = new ItemBuilder(Material.valueOf(training.getConfig().getString("gui.combat-button.material")));
        builder.setDisplayName(ChatColorUtils.translate(training.getConfig().getString("gui.combat-button.name")));
        builder.setItemFlags(List.of(ItemFlag.HIDE_ADDITIONAL_TOOLTIP));

        var loreLines = training.getConfig().getStringList("gui.combat-button.lore");

        for (String line : loreLines) {
            String processedLine = line
                    .replace("%type%", status ? "ON" : "OFF")
                    .replace("%rank%", options.getRank().name());
            builder.addLoreLines(ChatColorUtils.translate(processedLine));
        }
        return builder;
    }

    @Override
    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
        boolean followStatus = options.isFollow();

        if (!followStatus) {
            String msg = training.getConfig().getString("combat-need-follow", "&cFollow deve essere ON per abilitare il combat del bot");
            player.sendMessage(ChatColorUtils.translate(msg));
            return;
        }

        if (clickType.isLeftClick()) {
            boolean oldStatus = options.isCombat();
            boolean newStatus = !oldStatus;

            options.setCombat(newStatus);
            training.getBotManager().updateCombat(player.getUniqueId(), newStatus);

            if (newStatus) {
                training.getBotManager().switchBotToEnderpearl(player.getUniqueId());
                training.getServer().getScheduler().runTaskLater(training, () -> {
                    training.getBotManager().switchBotToSword(player.getUniqueId());
                }, 1L);
            } else {
                if (training.getBotManager().getSwordSlot(player.getUniqueId())) {
                    int totemcount = options.getTotems();
                    if (totemcount > 1) {
                        training.getBotManager().switchBotToEnderpearl(player.getUniqueId());
                    } else {
                        training.getBotManager().switchBotToEmpty(player.getUniqueId());
                    }
                }
            }

            player.sendMessage(ChatColorUtils.translate("&eCombat " + (newStatus ? "&aON" : "&cOFF")));
        }

        if (clickType.isRightClick()) {
            BotRank currentRank = options.getRank();
            BotRank[] values = BotRank.values();
            int index = currentRank.ordinal();

            index = (index + 1) % values.length;

            BotRank newRank = values[index];
            options.setRank(newRank);

            training.getBotManager().setBotRank(player.getUniqueId(), newRank);

            player.sendMessage(ChatColorUtils.translate("&aRank impostato su &e" + newRank.name()));
        }

        notifyWindows();
    }
}
