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

public class TotemItem extends AbstractItem {

    private final BotOptions options;
    private final SandboxTraining training;

    public TotemItem(BotOptions options, SandboxTraining training) {
        this.options = options;
        this.training = training;
    }

    @Override
    public ItemProvider getItemProvider() {
        ItemBuilder builder = new ItemBuilder(Material.valueOf(training.getConfig().getString("gui.totem-button.material")));

        String unlimitedText = training.getConfig().getString("gui.totem-button.unlimited-text");
        String countLine = options.getTotems() == -1
                ? unlimitedText
                : String.valueOf(options.getTotems());
        builder.setDisplayName(ChatColorUtils.translate(training.getConfig().getString("gui.totem-button.name")));
        var loreLines = training.getConfig().getStringList("gui.totem-button.lore");

        for (String line : loreLines) {
            assert countLine != null;
            String replaced = line.replace("%count%", countLine);
            builder.addLoreLines(ChatColorUtils.translate(replaced));
        }
        return builder;
    }

    @Override
    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
        if (clickType.isLeftClick() && options.getTotems() < 37) {
            options.setTotems(options.getTotems() + 1);
        }

        if (clickType.isRightClick() && options.getTotems() > -1) {
            options.setTotems(options.getTotems() - 1);
        }

        training.getBotSpawner().updateBotTotemCount(player.getUniqueId(), options.getTotems());

        notifyWindows();
    }
}
