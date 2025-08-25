package it.coralmc.sandbox.gui.impl;

import it.coralmc.sandbox.SandboxTraining;
import it.coralmc.sandbox.bot.BotOptions;
import it.coralmc.sandbox.utils.ChatColorUtils;
import it.coralmc.sandbox.utils.armor.PlayerOptions;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.inventoryaccess.component.ComponentWrapper;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;
import xyz.xenondevs.invui.window.Window;
import xyz.xenondevs.invui.window.WindowManager;

public class SpawnItem extends AbstractItem {

    private final SandboxTraining training;
    private final Player player;
    private final BotOptions options;

    private final PlayerOptions playerOptions;

    public SpawnItem(SandboxTraining training, Player player, BotOptions options) {
        this.training = training;
        this.player = player;
        this.options = options;
        this.playerOptions = training.getPlayerOptions();
    }

    @Override
    public ItemProvider getItemProvider() {
        boolean status = training.getBotManager().isBotSpawned(player.getUniqueId());

        Material mat = status
                ? Material.valueOf(training.getConfig().getString("gui.despawn-button.material"))
                : Material.valueOf(training.getConfig().getString("gui.spawn-button.material"));

        String name = status
                ? training.getConfig().getString("gui.despawn-button.name")
                : training.getConfig().getString("gui.spawn-button.name");

        var lore = status
                ? training.getConfig().getStringList("gui.despawn-button.lore")
                : training.getConfig().getStringList("gui.spawn-button.lore");

        ItemBuilder builder = new ItemBuilder(mat);
        builder.setDisplayName(ChatColorUtils.translate(name));
        for (String line : lore) {
            builder.addLoreLines(ChatColorUtils.translate(line));
        }

        return builder;
    }

    @Override
    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
        boolean status = training.getBotManager().isBotSpawned(player.getUniqueId());

        if (status) {
            training.getBotManager().despawn(player);
            playerOptions.remove(player.getUniqueId());
            Window window = WindowManager.getInstance().getOpenWindow(player);
            if (window != null) window.close();
            return;
        }

        Window window = WindowManager.getInstance().getOpenWindow(player);
        if (window != null) window.close();

        boolean follow = options.isFollow();
        training.getBotManager().spawn(player, options.getArmor(), options.getBlast(), follow, options.getTotems());
    }
}
