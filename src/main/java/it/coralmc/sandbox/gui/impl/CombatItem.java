package it.coralmc.sandbox.gui.impl;

import it.coralmc.sandbox.SandboxTraining;
import it.coralmc.sandbox.bot.BotOptions;
import it.coralmc.sandbox.utils.ChatColorUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;

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

        var loreLines = training.getConfig().getStringList("gui.combat-button.lore");

        for (String line : loreLines) {
            String processedLine = line.replace("%type%", status ? "ON" : "OFF");
            builder.addLoreLines(ChatColorUtils.translate(processedLine));
        }
        return builder;
    }

    @Override
    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
        boolean oldStatus = options.isCombat();
        boolean followStatus = options.isFollow();

        if (!followStatus) {
            String msg = training.getConfig().getString("combat-need-follow", "&cFollow deve essere ON per abilitare il combat del bot");
            player.sendMessage(ChatColorUtils.translate(msg));
            return;
        }

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
                training.getBotManager().switchBotToEnderpearl(player.getUniqueId());
            }
        }

        notifyWindows();
    }
}
