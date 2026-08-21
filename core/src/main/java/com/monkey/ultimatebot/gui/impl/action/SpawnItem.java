package com.monkey.ultimatebot.gui.impl.action;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.bot.BotOptions;
import com.monkey.ultimatebot.bot.BotType;
import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.utils.ChatColorUtils;
import com.monkey.ultimatebot.utils.armor.PlayerOptions;
import com.monkey.ultimatebot.utils.item.ItemFlagCatalog;
import com.monkey.ultimatebot.utils.material.MaterialCatalog;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import com.monkey.ultimatebot.libs.invui.item.ItemProvider;
import com.monkey.ultimatebot.libs.invui.item.builder.ItemBuilder;
import com.monkey.ultimatebot.libs.invui.item.impl.AbstractItem;
import com.monkey.ultimatebot.libs.invui.window.Window;
import com.monkey.ultimatebot.libs.invui.window.WindowManager;

public class SpawnItem extends AbstractItem {

    private final UltimateBot training;
    private final Player player;
    private final BotOptions options;
    private final PlayerOptions playerOptions;

    public SpawnItem(UltimateBot training, Player player, BotOptions options) {
        this.training = training;
        this.player = player;
        this.options = options;
        this.playerOptions = training.getPlayerOptions();
    }

    @Override
    public ItemProvider getItemProvider() {
        boolean status = isManagedBotSpawned();

        Material mat = status
                ? MaterialCatalog.optional(
                        training.getLangString("gui.despawn-button.material", "BARRIER"), Material.BARRIER)
                : MaterialCatalog.optional(
                        training.getLangString("gui.spawn-button.material", "PLAYER_HEAD"), Material.PLAYER_HEAD);

        String name = status
                ? training.getLangString("gui.despawn-button.name")
                : training.getLangString("gui.spawn-button.name");

        java.util.List<String> lore = status
                ? training.getLangStringList("gui.despawn-button.lore")
                : training.getLangStringList("gui.spawn-button.lore");

        ItemBuilder builder = new ItemBuilder(mat);
        builder.setItemFlags(ItemFlagCatalog.resolve("HIDE_ADDITIONAL_TOOLTIP"));
        builder.setDisplayName(ChatColorUtils.translate(name));
        for (String line : lore) {
            builder.addLoreLines(ChatColorUtils.translate(line));
        }

        return builder;
    }

    @Override
    public void handleClick(
            @NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
        boolean status = isManagedBotSpawned();

        if (status) {
            UUID managedOwnerUUID = resolveManagedOwnerUUID();
            if (!training.getBotManager().despawnByOwnerUUID(managedOwnerUUID)) return;
            Window window = WindowManager.getInstance().getOpenWindow(player);
            if (window != null) window.close();
            clearCachedOptionsAfterDespawn(managedOwnerUUID);
            player.sendMessage(
                    ChatColorUtils.translate(training.getLangString("messages.despawn-bot", "&cBot removed!")));
            return;
        }

        if (options.getBotType() == BotType.EVENT) {
            if (isBotEventActive()) {
                String msg = training.getLangString("messages.event-bot-already-active");
                player.sendMessage(ChatColorUtils.translate(msg));
                return;
            }
            training.getBotManager().despawnAll();
            String msg = training.getLangString(
                    "messages.all-normal-bots-despawned", "&eAll normal bots have been despawned for the event");
            player.sendMessage(ChatColorUtils.translate(msg));
        } else {
            if (isBotEventActive()) {
                String msg = training.getLangString("messages.cannot-spawn-normal-during-event");
                player.sendMessage(ChatColorUtils.translate(msg));
                return;
            }
        }

        Window window = WindowManager.getInstance().getOpenWindow(player);
        if (window != null) window.close();

        boolean follow = options.isFollow();
        boolean spawned = training.getBotManager()
                .spawn(player, options.getArmor(), options.getBlast(), follow, options.getTotems(), options);
        if (!spawned) return;
        String msg = training.getLangString("messages.spawn-bot", "&aBot spawned with the selected settings!");
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
        String template = training.getLangString(
                "messages.team-ally.team-spawned-notify",
                "&aAllied bot spawned by %playerowner% for the team with these owners: %playerlist%");

        List<String> ownerNames = new ArrayList<>();
        for (UUID ownerUUID : options.getTeamOwnerUUIDs()) {
            String name = Bukkit.getOfflinePlayer(ownerUUID).getName();
            ownerNames.add(name == null ? ownerUUID.toString() : name);
        }

        String ownerList = String.join(", ", ownerNames);
        String message = template.replace("%playerowner%", spawner.getName()).replace("%playerlist%", ownerList);

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
