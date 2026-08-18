package com.monkey.ultimatebot.integration.api.internal;

import com.monkey.ultimatebot.bot.BotManager;
import com.monkey.ultimatebot.bot.BotOptions;
import com.monkey.ultimatebot.bot.BotRegistry;
import com.monkey.ultimatebot.bot.BotType;
import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

public final class CoreBotOwnerResolver {

    private final BotManager botManager;
    private final BotRegistry botRegistry;

    public CoreBotOwnerResolver(BotManager botManager, BotRegistry botRegistry) {
        this.botManager = botManager;
        this.botRegistry = botRegistry;
    }

    public @Nullable UUID resolveOwnerByBotUUID(@Nullable UUID botUUID) {
        return botRegistry.getOwnerUUIDByBotUUID(botUUID);
    }

    public UUID resolveManagedOwner(UUID ownerUUID) {
        if (botManager.isBotSpawned(ownerUUID)) {
            return ownerUUID;
        }
        UUID teamPrimaryOwner = botManager.findTeamAllyPrimaryOwner(ownerUUID);
        return teamPrimaryOwner == null ? ownerUUID : teamPrimaryOwner;
    }

    public UUID resolveManagedOwnerUUID(BotType type, @Nullable UUID requestedOwnerUUID, UUID contextOwnerUUID) {
        if (type == BotType.EVENT && requestedOwnerUUID == null) {
            return UUID.randomUUID();
        }
        return contextOwnerUUID;
    }

    public @Nullable UUID resolvePrimaryOwnerUUID(
            BotType type, @Nullable UUID requestedOwnerUUID, Set<UUID> teamOwners, Set<UUID> targetUUIDs) {
        if (type == BotType.EVENT) {
            UUID contextOwner = resolveOnlineUUID(requestedOwnerUUID);
            if (contextOwner != null) {
                return contextOwner;
            }

            for (UUID targetUUID : targetUUIDs) {
                contextOwner = resolveOnlineUUID(targetUUID);
                if (contextOwner != null) {
                    return contextOwner;
                }
            }

            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player != null && player.isOnline()) {
                    return player.getUniqueId();
                }
            }

            return null;
        }

        if (type != BotType.TEAM_ALLY) {
            return resolveOnlineUUID(requestedOwnerUUID);
        }

        UUID owner = resolveOnlineUUID(requestedOwnerUUID);
        if (owner != null) {
            return owner;
        }

        for (UUID teamOwner : teamOwners) {
            owner = resolveOnlineUUID(teamOwner);
            if (owner != null) {
                return owner;
            }
        }

        return null;
    }

    public @Nullable BotType getOwnerBusyType(UUID ownerUUID, @Nullable UUID allowedTeamPrimaryOwner) {
        if (ownerUUID == null) {
            return null;
        }

        if (botManager.isBotSpawned(ownerUUID)) {
            ITrainingBot bot = botManager.getBotSafe(ownerUUID);
            if (bot != null && bot.getBrainController() != null && bot.getBrainController().getBotOptions() != null) {
                BotOptions options = bot.getBrainController().getBotOptions();
                BotType type = options.getBotType();
                if (type == BotType.TEAM_ALLY
                        && allowedTeamPrimaryOwner != null
                        && ownerUUID.equals(allowedTeamPrimaryOwner)) {
                    return null;
                }
                return type;
            }
            return BotType.SINGLE;
        }

        if (botManager.hasActiveTeamAlly(ownerUUID)) {
            UUID teamPrimaryOwner = botManager.findTeamAllyPrimaryOwner(ownerUUID);
            if (allowedTeamPrimaryOwner != null && allowedTeamPrimaryOwner.equals(teamPrimaryOwner)) {
                return null;
            }
            return BotType.TEAM_ALLY;
        }

        return null;
    }

    private @Nullable UUID resolveOnlineUUID(@Nullable UUID uuid) {
        if (uuid == null) {
            return null;
        }

        Player player = Bukkit.getPlayer(uuid);
        return player != null && player.isOnline() ? uuid : null;
    }
}
