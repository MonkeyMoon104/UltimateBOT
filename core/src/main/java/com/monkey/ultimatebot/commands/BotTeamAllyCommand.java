package com.monkey.ultimatebot.commands;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.bot.BotOptions;
import com.monkey.ultimatebot.bot.BotType;
import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.compat.WorldAccess;
import com.monkey.ultimatebot.nms.NMSBridgeManager;
import com.monkey.ultimatebot.utils.ChatColorUtils;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;
import org.jspecify.annotations.Nullable;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Sized;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.annotation.SuggestWith;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.bukkit.annotation.CommandPermission;

public class BotTeamAllyCommand {

    private final UltimateBot plugin;

    public BotTeamAllyCommand(UltimateBot plugin) {
        this.plugin = plugin;
    }

    @Command("botteamally")
    @CommandPermission(value = "ultimatebot.bot.use", defaultAccess = PermissionDefault.TRUE)
    public void openGui(BukkitCommandActor actor) {
        if (!actor.isPlayer()) {
            return;
        }
        Player player = actor.requirePlayer();
        if (!guardCommon(player)) {
            return;
        }

        ActiveTeamAlly activeTeamAlly = findActiveTeamAlly(player.getUniqueId());
        if (activeTeamAlly == null) {
            sendConfigured(player, "messages.team-ally.create-usage");
            return;
        }

        BotOptions sharedOptions = activeTeamAlly.options();
        sharedOptions.setBotType(BotType.TEAM_ALLY);
        plugin.getPlayerOptions().put(player.getUniqueId(), sharedOptions);
        if (!NMSBridgeManager.isBotRuntimeSupported()) {
            player.sendMessage(ChatColorUtils.translate(
                    "&cUltimateBot fake-player bots require Minecraft 1.17.1 or newer on this build."));
            return;
        }
        NMSBridgeManager.get().openBotGui(player, plugin, BotType.TEAM_ALLY);
        sendConfigured(player, "messages.team-ally.gui-opened");
    }

    @Subcommand("create")
    @Command("botteamally")
    @CommandPermission(value = "ultimatebot.bot.use", defaultAccess = PermissionDefault.TRUE)
    public void create(
            BukkitCommandActor actor,
            @Sized(min = 0) @SuggestWith(OnlinePlayerNameSuggestions.class) String[] ownerNames) {
        if (!actor.isPlayer()) {
            return;
        }
        Player player = actor.requirePlayer();
        if (!guardCommon(player)) {
            return;
        }

        if (ownerNames.length < 2) {
            sendConfigured(player, "messages.team-ally.create-usage");
            return;
        }

        String duplicate = findFirstDuplicateName(ownerNames);
        if (duplicate != null) {
            sendConfigured(player, "messages.team-ally.duplicate-owner", "%player%", duplicate);
            return;
        }

        Set<UUID> owners = resolveOwners(player, ownerNames);
        if (owners.size() < 2) {
            sendConfigured(player, "messages.team-ally.create-min-owners");
            return;
        }

        for (UUID ownerUUID : owners) {
            BotType busyType = getOwnerBusyType(ownerUUID, null);
            if (busyType != null) {
                String ownerName = Bukkit.getOfflinePlayer(ownerUUID).getName();
                sendConfigured(
                        player,
                        "messages.team-ally.owner-has-active-bot",
                        "%player%",
                        ownerName == null ? ownerUUID.toString() : ownerName,
                        "%bottype%",
                        busyType.name().toLowerCase(Locale.ROOT));
                return;
            }
        }

        BotOptions options = plugin.getPlayerOptions().getOptions(player.getUniqueId());
        if (options == null) {
            options = new BotOptions(
                    plugin,
                    com.monkey.ultimatebot.utils.armor.ArmorCycle.getDefaultArmorFromConfig(
                            plugin.getLanguageConfig(), plugin));
        }

        options.setBotType(BotType.TEAM_ALLY);
        options.setTeamOwnerUUIDs(owners);
        options.setOwnerUUID(owners.iterator().next());
        plugin.getPlayerOptions().put(player.getUniqueId(), options);

        if (!NMSBridgeManager.isBotRuntimeSupported()) {
            player.sendMessage(ChatColorUtils.translate(
                    "&cUltimateBot fake-player bots require Minecraft 1.17.1 or newer on this build."));
            return;
        }

        NMSBridgeManager.get().openBotGui(player, plugin, BotType.TEAM_ALLY);
        sendConfigured(player, "messages.team-ally.gui-opened");
    }

    @Subcommand("addowners")
    @Command("botteamally")
    @CommandPermission(value = "ultimatebot.bot.use", defaultAccess = PermissionDefault.TRUE)
    public void addOwners(
            BukkitCommandActor actor,
            @Sized(min = 0) @SuggestWith(OnlinePlayerNameSuggestions.class) String[] ownerNames) {
        if (!actor.isPlayer()) {
            return;
        }
        Player player = actor.requirePlayer();
        if (!guardCommon(player)) {
            return;
        }

        if (ownerNames.length < 1) {
            sendConfigured(player, "messages.team-ally.addowners-usage");
            return;
        }

        String duplicate = findFirstDuplicateName(ownerNames);
        if (duplicate != null) {
            sendConfigured(player, "messages.team-ally.duplicate-owner", "%player%", duplicate);
            return;
        }

        ActiveTeamAlly activeTeamAlly = findActiveTeamAlly(player.getUniqueId());
        if (activeTeamAlly == null) {
            sendConfigured(player, "messages.team-ally.no-active-teamally");
            return;
        }

        BotOptions options = activeTeamAlly.options();
        Set<UUID> owners = new LinkedHashSet<>(options.getTeamOwnerUUIDs());

        for (String ownerName : ownerNames) {
            Player owner = Bukkit.getPlayerExact(ownerName);
            if (owner == null) {
                sendConfigured(player, "messages.team-ally.invalid-owner", "%player%", ownerName);
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
                        "%player%",
                        owner.getName(),
                        "%bottype%",
                        busyType.name().toLowerCase(Locale.ROOT));
                continue;
            }

            sendConfigured(player, "messages.team-ally.owner-added", "%player%", owner.getName());
        }

        options.setTeamOwnerUUIDs(owners);
        plugin.getPlayerOptions().put(player.getUniqueId(), options);
    }

    @Subcommand("removeowners")
    @Command("botteamally")
    @CommandPermission(value = "ultimatebot.bot.use", defaultAccess = PermissionDefault.TRUE)
    public void removeOwners(
            BukkitCommandActor actor,
            @Sized(min = 0) @SuggestWith(OnlinePlayerNameSuggestions.class) String[] ownerNames) {
        if (!actor.isPlayer()) {
            return;
        }
        Player player = actor.requirePlayer();
        if (!guardCommon(player)) {
            return;
        }

        if (ownerNames.length < 1) {
            sendConfigured(player, "messages.team-ally.removeowners-usage");
            return;
        }

        String duplicate = findFirstDuplicateName(ownerNames);
        if (duplicate != null) {
            sendConfigured(player, "messages.team-ally.duplicate-owner", "%player%", duplicate);
            return;
        }

        ActiveTeamAlly activeTeamAlly = findActiveTeamAlly(player.getUniqueId());
        if (activeTeamAlly == null) {
            sendConfigured(player, "messages.team-ally.no-active-teamally");
            return;
        }

        BotOptions options = activeTeamAlly.options();
        Set<UUID> owners = new LinkedHashSet<>(options.getTeamOwnerUUIDs());

        for (String ownerName : ownerNames) {
            Player owner = Bukkit.getPlayerExact(ownerName);
            if (owner == null) {
                sendConfigured(player, "messages.team-ally.invalid-owner", "%player%", ownerName);
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
    }

    private boolean guardCommon(Player player) {
        World world = player.getWorld();
        java.util.List<String> blockedWorlds = plugin.getConfig().getStringList("bot.blocked-worlds");
        if (blockedWorlds.stream().anyMatch(blockedWorld -> blockedWorld.equalsIgnoreCase(WorldAccess.name(world)))) {
            String msg = plugin.getLangString("messages.bot-blocked-world", "&cYou cannot use this here!");
            player.sendMessage(ChatColorUtils.translate(msg));
            return false;
        }

        if (isEventBotActive()) {
            sendConfigured(player, "messages.event-bot-active-block-normal");
            return false;
        }
        return true;
    }

    private Set<UUID> resolveOwners(Player sender, String[] ownerNames) {
        Set<UUID> owners = new LinkedHashSet<>();

        for (String ownerName : ownerNames) {
            Player owner = Bukkit.getPlayerExact(ownerName);
            if (owner == null) {
                sendConfigured(sender, "messages.team-ally.invalid-owner", "%player%", ownerName);
                continue;
            }

            if (!owners.add(owner.getUniqueId())) {
                sendConfigured(sender, "messages.team-ally.owner-already-present", "%player%", owner.getName());
            }
        }

        return owners;
    }

    private @Nullable String findFirstDuplicateName(String[] ownerNames) {
        Set<String> names = new LinkedHashSet<>();
        for (String ownerName : ownerNames) {
            String lowered = ownerName.toLowerCase(Locale.ROOT);
            if (!names.add(lowered)) {
                return ownerName;
            }
        }
        return null;
    }

    private @Nullable BotType getOwnerBusyType(UUID ownerUUID, @Nullable UUID allowedTeamPrimaryOwner) {
        if (plugin.getBotManager().isBotSpawned(ownerUUID)) {
            ITrainingBot bot = plugin.getBotManager().getBotSafe(ownerUUID);
            if (bot != null
                    && bot.getBrainController() != null
                    && bot.getBrainController().getBotOptions() != null) {
                BotType type = bot.getBrainController().getBotOptions().getBotType();
                if (type == BotType.TEAM_ALLY
                        && allowedTeamPrimaryOwner != null
                        && ownerUUID.equals(allowedTeamPrimaryOwner)) {
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
                com.monkey.ultimatebot.bot.BotOptions botOptions = bot.getBrainController().getBotOptions();
                if (botOptions != null && botOptions.getBotType() == BotType.EVENT) {
                    return true;
                }
            }
        }
        return false;
    }

    private @Nullable ActiveTeamAlly findActiveTeamAlly(UUID ownerUUID) {
        for (Map.Entry<UUID, ITrainingBot> entry :
                plugin.getBotRegistry().getAllBots().entrySet()) {
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

    private void sendConfigured(Player player, String path) {
        String msg = plugin.getLangString(path);
        if (msg != null && !msg.trim().isEmpty()) {
            player.sendMessage(ChatColorUtils.translate(msg));
        }
    }

    private void sendConfigured(Player player, String path, String placeholder, String value) {
        String msg = plugin.getLangString(path);
        if (msg != null && !msg.trim().isEmpty()) {
            player.sendMessage(ChatColorUtils.translate(msg.replace(placeholder, value)));
        }
    }

    private void sendConfigured(
            Player player, String path, String placeholderA, String valueA, String placeholderB, String valueB) {
        String msg = plugin.getLangString(path);
        if (msg != null && !msg.trim().isEmpty()) {
            player.sendMessage(
                    ChatColorUtils.translate(msg.replace(placeholderA, valueA).replace(placeholderB, valueB)));
        }
    }

    private static final class ActiveTeamAlly {
        private final UUID registryOwner;
        private final BotOptions options;

        private ActiveTeamAlly(UUID registryOwner, BotOptions options) {
            this.registryOwner = registryOwner;
            this.options = options;
        }

        UUID registryOwner() {
            return registryOwner;
        }

        BotOptions options() {
            return options;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof ActiveTeamAlly)) {
                return false;
            }
            ActiveTeamAlly other = (ActiveTeamAlly) obj;
            return java.util.Objects.equals(registryOwner, other.registryOwner)
                    && java.util.Objects.equals(options, other.options);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(registryOwner, options);
        }

        @Override
        public String toString() {
            return "ActiveTeamAlly[registryOwner="
                    + registryOwner
                    + ", optionsOwner="
                    + options.getOwnerUUID()
                    + "]";
        }
    }
}
