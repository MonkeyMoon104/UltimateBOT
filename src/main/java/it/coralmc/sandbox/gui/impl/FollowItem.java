package it.coralmc.sandbox.gui.impl;

import it.coralmc.sandbox.SandboxTraining;
import it.coralmc.sandbox.bot.BotOptions;
import it.coralmc.sandbox.utils.ChatColorUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.inventoryaccess.component.ComponentWrapper;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;

public class FollowItem extends AbstractItem {

    private final SandboxTraining training;
    private final BotOptions options;

    public FollowItem(SandboxTraining training, BotOptions options) {
        this.training = training;
        this.options = options;
    }

    @Override
    public ItemProvider getItemProvider() {
        boolean status = options.isFollow();

        ItemBuilder builder = new ItemBuilder(Material.valueOf(training.getConfig().getString("gui.follow-button.material")));
        builder.setDisplayName(ChatColorUtils.translate(training.getConfig().getString("gui.follow-button.name")));

        var loreLines = training.getConfig().getStringList("gui.follow-button.lore");

        for (String line : loreLines) {
            String processedLine = line.replace("%type%", status ? "ON" : "OFF");
            builder.addLoreLines(ChatColorUtils.translate(processedLine));
        }
        return builder;
    }

    @Override
    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
        boolean status = options.isFollow();
        System.out.println("Follow status prima: " + status);
        options.setFollow(!status);
        System.out.println("Follow status dopo: " + options.isFollow());
        training.getBotSpawner().updateBotFollow(player.getUniqueId(), options.isFollow());

        notifyWindows();
    }
}
