package com.monkey.mcbot.commands;

import com.monkey.mcbot.MinecraftBot;
import com.monkey.mcbot.bot.BotOptions;
import com.monkey.mcbot.bot.BotType;
import com.monkey.mcbot.bot.ai.ITrainingBot;
import com.monkey.mcbot.gui.NewBotGUI;
import com.monkey.mcbot.utils.ChatColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class BotTeamAllyCommand implements CommandExecutor, TabCompleter {

    private final MinecraftBot plugin;

    public BotTeamAllyCommand(MinecraftBot plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }

        if (!player.hasPermission("mcb.bot.use")) {
            sendConfigured(player, "messages.reload-no-permission");
            return true;
        }

        World world = player.getWorld();
        List<String> blockedWorlds = plugin.getConfig().getStringList("bot.blocked-worlds");
        if (blockedWorlds.stream().anyMatch(blockedWorld -> blockedWorld.equalsIgnoreCase(world.getName()))) {
            String msg = plugin.getConfig().getString("messages.bot-blocked-world", "&cYou cannot use this here!");
            player.sendMessage(ChatColorUtils.translate(msg));
            return true;
        }

        if (isEventBotActive()) {
            sendConfigured(player, "messages.event-bot-active-block-normal");
            return true;
        }

        if (args.length == 0) {
            ActiveTeamAlly activeTeamAlly = findActiveTeamAlly(player.getUniqueId());
            if (activeTeamAlly == null) {
                sendConfigured(player, "messages.team-ally.create-usage");
                return true;
            }

            BotOptions sharedOptions = activeTeamAlly.options();
            sharedOptions.setBotType(BotType.TEAM_ALLY);
            plugin.getPlayerOptions().put(player.getUniqueId(), sharedOptions);
            new NewBotGUI(player, plugin, BotType.TEAM_ALLY).open();
            sendConfigured(player, "messages.team-ally.gui-opened");
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        if (sub.equals("create")) {
            return handleCreate(player, args);
        }
        if (sub.equals("addowners")) {
            return handleAddOwners(player, args);
        }
        if (sub.equals("removeowners")) {
            return handleRemoveOwners(player, args);
        }

        sendConfigured(player, "messages.team-ally.create-usage");
        return true;
    }

    private boolean handleCreate(Player player, String[] args) {
        if (args.length < 3) {
            sendConfigured(player, "messages.team-ally.create-usage");
            return true;
        }

        String duplicate = findFirstDuplicateName(args, 1);
        if (duplicate != null) {
            sendConfigured(player, "messages.team-ally.duplicate-owner", "%player%", duplicate);
            return true;
        }

        Set<UUID> owners = resolveOwners(player, args, 1);
        if (owners.size() < 2) {
            sendConfigured(player, "messages.team-ally.create-min-owners");
            return true;
        }

        for (UUID ownerUUID : owners) {
            BotType busyType = getOwnerBusyType(ownerUUID, null);
            if (busyType != null) {
                String ownerName = Bukkit.getOfflinePlayer(ownerUUID).getName();
                sendConfigured(
                        player,
                        "messages.team-ally.owner-has-active-bot",
                        "%player%", ownerName == null ? ownerUUID.toString() : ownerName,
                        "%bottype%", busyType.name().toLowerCase(Locale.ROOT)
                );
                return true;
            }
        }

        BotOptions options = plugin.getPlayerOptions().getOptions(player.getUniqueId());
        if (options == null) {
            options = new BotOptions(plugin, com.monkey.mcbot.utils.armor.ArmorCycle.getDefaultArmorFromConfig(plugin.getConfig(), plugin));
        }

        options.setBotType(BotType.TEAM_ALLY);
        options.setTeamOwnerUUIDs(owners);
        options.setOwnerUUID(owners.iterator().next());
        plugin.getPlayerOptions().put(player.getUniqueId(), options);

        new NewBotGUI(player, plugin, BotType.TEAM_ALLY).open();
        sendConfigured(player, "messages.team-ally.gui-opened");
        return true;
    }

    private boolean handleAddOwners(Player player, String[] args) {
        if (args.length < 2) {
            sendConfigured(player, "messages.team-ally.addowners-usage");
            return true;
        }

        String duplicate = findFirstDuplicateName(args, 1);
        if (duplicate != null) {
            sendConfigured(player, "messages.team-ally.duplicate-owner", "%player%", duplicate);
            return true;
        }

        ActiveTeamAlly activeTeamAlly = findActiveTeamAlly(player.getUniqueId());
        if (activeTeamAlly == null) {
            sendConfigured(player, "messages.team-ally.no-active-teamally");
            return true;
        }

        BotOptions options = activeTeamAlly.options();
        Set<UUID> owners = new LinkedHashSet<>(options.getTeamOwnerUUIDs());

        for (int i = 1; i < args.length; i++) {
            Player owner = Bukkit.getPlayerExact(args[i]);
            if (owner == null) {
                sendConfigured(player, "messages.team-ally.invalid-owner", "%player%", args[i]);
                continue;
            }

            if (!owners.add(owner.getUniqueId())) {
                sendConfigured(player, "messages.team-ally.owner-already-present", "%player%", owner.getName());
                continue;
            }

            BotType busyType = getOwnerBusyType(owner.getUniqueId(), activeTeamAlly.registryOwner());
            if (busyType != null) {
                owners.remove(owner.getUniqueId());
                sendConfigured(
                        player,
                        "messages.team-ally.owner-has-active-bot",
                        "%player%", owner.getName(),
                        "%bottype%", busyType.name().toLowerCase(Locale.ROOT)
                );
                continue;
            }

            sendConfigured(player, "messages.team-ally.owner-added", "%player%", owner.getName());
        }

        options.setTeamOwnerUUIDs(owners);
        plugin.getPlayerOptions().put(player.getUniqueId(), options);
        return true;
    }

    private boolean handleRemoveOwners(Player player, String[] args) {
        if (args.length < 2) {
            sendConfigured(player, "messages.team-ally.removeowners-usage");
            return true;
        }

        String duplicate = findFirstDuplicateName(args, 1);
        if (duplicate != null) {
            sendConfigured(player, "messages.team-ally.duplicate-owner", "%player%", duplicate);
            return true;
        }

        ActiveTeamAlly activeTeamAlly = findActiveTeamAlly(player.getUniqueId());
        if (activeTeamAlly == null) {
            sendConfigured(player, "messages.team-ally.no-active-teamally");
            return true;
        }

        BotOptions options = activeTeamAlly.options();
        Set<UUID> owners = new LinkedHashSet<>(options.getTeamOwnerUUIDs());

        for (int i = 1; i < args.length; i++) {
            Player owner = Bukkit.getPlayerExact(args[i]);
            if (owner == null) {
                sendConfigured(player, "messages.team-ally.invalid-owner", "%player%", args[i]);
                continue;
            }

            if (!owners.contains(owner.getUniqueId())) {
                sendConfigured(player, "messages.team-ally.owner-not-present", "%player%", owner.getName());
                continue;
            }

            if (owners.size() <= 2) {
                sendConfigured(player, "messages.team-ally.min-owners-reached");
                break;
            }

            owners.remove(owner.getUniqueId());
            sendConfigured(player, "messages.team-ally.owner-removed", "%player%", owner.getName());
        }

        options.setTeamOwnerUUIDs(owners);
        plugin.getPlayerOptions().put(player.getUniqueId(), options);
        return true;
    }

    private Set<UUID> resolveOwners(Player sender, String[] args, int startIndex) {
        Set<UUID> owners = new LinkedHashSet<>();

        for (int i = startIndex; i < args.length; i++) {
            Player owner = Bukkit.getPlayerExact(args[i]);
            if (owner == null) {
                sendConfigured(sender, "messages.team-ally.invalid-owner", "%player%", args[i]);
                continue;
            }

            if (!owners.add(owner.getUniqueId())) {
                sendConfigured(sender, "messages.team-ally.owner-already-present", "%player%", owner.getName());
            }
        }

        return owners;
    }

    private @Nullable String findFirstDuplicateName(String[] args, int startIndex) {
        Set<String> names = new LinkedHashSet<>();
        for (int i = startIndex; i < args.length; i++) {
            String lowered = args[i].toLowerCase(Locale.ROOT);
            if (!names.add(lowered)) {
                return args[i];
            }
        }
        return null;
    }

    private @Nullable BotType getOwnerBusyType(UUID ownerUUID, @Nullable UUID allowedTeamPrimaryOwner) {
        if (plugin.getBotManager().isBotSpawned(ownerUUID)) {
            ITrainingBot bot = plugin.getBotManager().getBotSafe(ownerUUID);
            if (bot != null && bot.getBrainController() != null && bot.getBrainController().getBotOptions() != null) {
                BotType type = bot.getBrainController().getBotOptions().getBotType();
                if (type == BotType.TEAM_ALLY && allowedTeamPrimaryOwner != null && ownerUUID.equals(allowedTeamPrimaryOwner)) {
                    return null;
                }
                return type;
            }
            return BotType.SINGLE;
        }

        if (plugin.getBotManager().hasActiveTeamAlly(ownerUUID)) {
            UUID teamPrimaryOwner = plugin.getBotManager().findTeamAllyPrimaryOwner(ownerUUID);
            if (allowedTeamPrimaryOwner != null && allowedTeamPrimaryOwner.equals(teamPrimaryOwner)) {
                return null;
            }
            return BotType.TEAM_ALLY;
        }

        return null;
    }

    private boolean isEventBotActive() {
        for (ITrainingBot bot : plugin.getBotRegistry().getAllBots().values()) {
            if (bot != null && bot.getBrainController() != null) {
                var botOptions = bot.getBrainController().getBotOptions();
                if (botOptions != null && botOptions.getBotType() == BotType.EVENT) {
                    return true;
                }
            }
        }
        return false;
    }

    private ActiveTeamAlly findActiveTeamAlly(UUID ownerUUID) {
        for (Map.Entry<UUID, ITrainingBot> entry : plugin.getBotRegistry().getAllBots().entrySet()) {
            ITrainingBot bot = entry.getValue();
            if (bot == null || bot.getBrainController() == null) {
                continue;
            }

            BotOptions options = bot.getBrainController().getBotOptions();
            if (options == null || options.getBotType() != BotType.TEAM_ALLY) {
                continue;
            }

            if (options.getTeamOwnerUUIDs().contains(ownerUUID)) {
                return new ActiveTeamAlly(entry.getKey(), options);
            }
        }

        return null;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        if (!(sender instanceof Player player)) {
            return List.of();
        }

        List<String> suggestions = new ArrayList<>();
        String current = args.length == 0 ? "" : args[args.length - 1].toLowerCase(Locale.ROOT);

        if (args.length == 1) {
            addIfMatches(suggestions, "create", current);
            addIfMatches(suggestions, "addowners", current);
            addIfMatches(suggestions, "removeowners", current);
            return suggestions;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);

        if (sub.equals("create")) {
            Set<String> already = collectLowercaseArgs(args, 1, args.length - 1);
            for (Player online : Bukkit.getOnlinePlayers()) {
                String name = online.getName();
                if (already.contains(name.toLowerCase(Locale.ROOT))) {
                    continue;
                }
                addIfMatches(suggestions, name, current);
            }
            return suggestions;
        }

        if (sub.equals("addowners") || sub.equals("removeowners")) {
            ActiveTeamAlly activeTeamAlly = findActiveTeamAlly(player.getUniqueId());
            if (activeTeamAlly == null) {
                return List.of();
            }

            Set<String> already = collectLowercaseArgs(args, 1, args.length - 1);
            Set<UUID> currentOwners = activeTeamAlly.options().getTeamOwnerUUIDs();

            if (sub.equals("addowners")) {
                for (Player online : Bukkit.getOnlinePlayers()) {
                    if (currentOwners.contains(online.getUniqueId())) {
                        continue;
                    }
                    String name = online.getName();
                    if (already.contains(name.toLowerCase(Locale.ROOT))) {
                        continue;
                    }
                    addIfMatches(suggestions, name, current);
                }
            } else {
                for (UUID ownerUUID : currentOwners) {
                    Player owner = Bukkit.getPlayer(ownerUUID);
                    if (owner == null) {
                        continue;
                    }
                    String name = owner.getName();
                    if (already.contains(name.toLowerCase(Locale.ROOT))) {
                        continue;
                    }
                    addIfMatches(suggestions, name, current);
                }
            }
        }

        return suggestions;
    }

    private void addIfMatches(List<String> suggestions, String candidate, String currentLowercase) {
        if (candidate.toLowerCase(Locale.ROOT).startsWith(currentLowercase)) {
            suggestions.add(candidate);
        }
    }

    private Set<String> collectLowercaseArgs(String[] args, int startInclusive, int endExclusive) {
        Set<String> values = new LinkedHashSet<>();
        for (int i = startInclusive; i < endExclusive && i < args.length; i++) {
            values.add(args[i].toLowerCase(Locale.ROOT));
        }
        return values;
    }

    private void sendConfigured(Player player, String path) {
        String msg = plugin.getConfig().getString(path);
        if (msg != null && !msg.isBlank()) {
            player.sendMessage(ChatColorUtils.translate(msg));
        }
    }

    private void sendConfigured(Player player, String path, String placeholder, String value) {
        String msg = plugin.getConfig().getString(path);
        if (msg != null && !msg.isBlank()) {
            player.sendMessage(ChatColorUtils.translate(msg.replace(placeholder, value)));
        }
    }

    private void sendConfigured(Player player,
                                String path,
                                String placeholderA,
                                String valueA,
                                String placeholderB,
                                String valueB) {
        String msg = plugin.getConfig().getString(path);
        if (msg != null && !msg.isBlank()) {
            player.sendMessage(ChatColorUtils.translate(msg.replace(placeholderA, valueA).replace(placeholderB, valueB)));
        }
    }

    private record ActiveTeamAlly(UUID registryOwner, BotOptions options) {
    }
}
