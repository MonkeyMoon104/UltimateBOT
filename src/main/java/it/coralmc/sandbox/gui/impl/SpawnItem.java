package it.coralmc.sandbox.gui.impl;

import it.coralmc.sandbox.SandboxTraining;
import it.coralmc.sandbox.bot.BotOptions;
import it.coralmc.sandbox.bot.ai.TrainingBot;
import it.coralmc.sandbox.utils.ChatColorUtils;
import it.coralmc.sandbox.utils.armor.PlayerOptions;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;
import xyz.xenondevs.invui.window.Window;
import xyz.xenondevs.invui.window.WindowManager;

import java.util.List;
import java.util.UUID;

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
        builder.setItemFlags(List.of(ItemFlag.HIDE_ADDITIONAL_TOOLTIP));
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
            player.sendMessage(ChatColorUtils.translate("&cBot despawnato"));
            return;
        }

        if (options.isEventBot()) {
            if (isBotEventActive()) {
                player.sendMessage(ChatColorUtils.translate("&c❌ C'è già un bot event attivo! Despawnalo prima"));
                return;
            }
            training.getBotManager().despawnAll();
            player.sendMessage(ChatColorUtils.translate("&eTutti i bot normali sono stati despawnati per l'evento"));
        }
        else {
            if (isBotEventActive()) {
                player.sendMessage(ChatColorUtils.translate("&c❌ Non puoi spawnare un bot normale mentre c'è un bot event attivo!"));
                return;
            }
        }

        Window window = WindowManager.getInstance().getOpenWindow(player);
        if (window != null) window.close();

        boolean follow = options.isFollow();
        training.getBotManager().spawn(player, options.getArmor(), options.getBlast(), follow, options.getTotems(), options);
        player.sendMessage(ChatColorUtils.translate("&a✓ Bot spawnato con successo!"));
    }

    private boolean isBotEventActive() {
        for (UUID ownerUUID : training.getBotRegistry().getAllBots().keySet()) {
            TrainingBot bot = training.getBotManager().getBotSafe(ownerUUID);

            if (bot != null) {
                BotOptions botOptions = bot.getBrainController().getBotOptions();
                if (botOptions != null && botOptions.isEventBot()) {
                    return true;
                }
            }
        }
        return false;
    }
}