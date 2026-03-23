package com.monkey.mcbot.gui.impl;

import com.monkey.mcbot.MinecraftBot;
import com.monkey.mcbot.bot.BotOptions;
import com.monkey.mcbot.bot.BotType;
import com.monkey.mcbot.bot.ai.ITrainingBot;
import com.monkey.mcbot.utils.ChatColorUtils;
import com.monkey.mcbot.utils.armor.PlayerOptions;
import org.bukkit.Bukkit;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SpawnItem extends AbstractItem {

    private final MinecraftBot training;
    private final Player player;
    private final BotOptions options;
    private final PlayerOptions playerOptions;

    public SpawnItem(MinecraftBot training, Player player, BotOptions options) {
        this.training = training;
        this.player = player;
        this.options = options;
        this.playerOptions = training.getPlayerOptions();
    }

    @Override
    public ItemProvider getItemProvider() {
        boolean status = isManagedBotSpawned();

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
        boolean status = isManagedBotSpawned();

        if (status) {
            UUID managedOwnerUUID = resolveManagedOwnerUUID();
            training.getBotManager().despawnByOwnerUUID(managedOwnerUUID);
            Window window = WindowManager.getInstance().getOpenWindow(player);
            if (window != null) window.close();
            clearCachedOptionsAfterDespawn(managedOwnerUUID);
            player.sendMessage(ChatColorUtils.translate(training.getConfig().getString("messages.despawn-bot", "&cBot rimosso!")));
            return;
        }

        if (options.getBotType() == BotType.EVENT) {
            if (isBotEventActive()) {
                String msg = training.getConfig().getString("messages.event-bot-already-active");
                player.sendMessage(ChatColorUtils.translate(msg));
                return;
            }
            training.getBotManager().despawnAll();
            String msg = training.getConfig().getString("messages.all-normal-bots-despawned", "&eTutti i bot normali sono stati despawnati per l'evento");
            player.sendMessage(ChatColorUtils.translate(msg));
        }
        else {
            if (isBotEventActive()) {
                String msg = training.getConfig().getString("messages.cannot-spawn-normal-during-event");
                player.sendMessage(ChatColorUtils.translate(msg));
                return;
            }
        }

        Window window = WindowManager.getInstance().getOpenWindow(player);
        if (window != null) window.close();

        boolean follow = options.isFollow();
        training.getBotManager().spawn(player, options.getArmor(), options.getBlast(), follow, options.getTotems(), options);
        String msg = training.getConfig().getString("messages.spawn-bot", "&aBot generato con le impostazioni selezionate!");
        player.sendMessage(ChatColorUtils.translate(msg));

        if (options.getBotType() == BotType.TEAM_ALLY) {
            notifyTeamOwners(player);
        }
    }

    private boolean isManagedBotSpawned() {
        if (options.getBotType() == BotType.TEAM_ALLY) {
            return training.getBotManager().hasActiveTeamAlly(player.getUniqueId());
        }
        return training.getBotManager().isBotSpawned(player.getUniqueId());
    }

    private UUID resolveManagedOwnerUUID() {
        if (options.getBotType() != BotType.TEAM_ALLY) {
            return player.getUniqueId();
        }

        UUID teamOwnerUUID = training.getBotManager().findTeamAllyPrimaryOwner(player.getUniqueId());
        return teamOwnerUUID == null ? player.getUniqueId() : teamOwnerUUID;
    }

    private void clearCachedOptionsAfterDespawn(UUID managedOwnerUUID) {
        playerOptions.remove(player.getUniqueId());
        playerOptions.remove(managedOwnerUUID);
        for (UUID teamOwnerUUID : options.getTeamOwnerUUIDs()) {
            playerOptions.remove(teamOwnerUUID);
        }
    }

    private void notifyTeamOwners(Player spawner) {
        String template = training.getConfig().getString(
                "messages.team-ally.team-spawned-notify",
                "&aBot alleato spawnato da %playerowner% per il team con i seguenti proprietari: %playerlist%"
        );

        List<String> ownerNames = new ArrayList<>();
        for (UUID ownerUUID : options.getTeamOwnerUUIDs()) {
            String name = Bukkit.getOfflinePlayer(ownerUUID).getName();
            ownerNames.add(name == null ? ownerUUID.toString() : name);
        }

        String ownerList = String.join(", ", ownerNames);
        String message = template
                .replace("%playerowner%", spawner.getName())
                .replace("%playerlist%", ownerList);

        for (UUID ownerUUID : options.getTeamOwnerUUIDs()) {
            Player owner = Bukkit.getPlayer(ownerUUID);
            if (owner != null && owner.isOnline()) {
                owner.sendMessage(ChatColorUtils.translate(message));
            }
        }
    }

    private boolean isBotEventActive() {
        for (UUID ownerUUID : training.getBotRegistry().getAllBots().keySet()) {
            ITrainingBot bot = training.getBotManager().getBotSafe(ownerUUID);

            if (bot != null) {
                BotOptions botOptions = bot.getBrainController().getBotOptions();
                if (botOptions != null && botOptions.getBotType() == BotType.EVENT) {
                    return true;
                }
            }
        }
        return false;
    }
}
