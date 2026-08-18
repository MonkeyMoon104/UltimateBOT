package com.monkey.ultimatebot.integration.api.internal;

import com.monkey.ultimatebot.bot.BotOptions;
import com.monkey.ultimatebot.bot.BotType;
import com.monkey.ultimatebot.utils.armor.PlayerOptions;
import java.util.Set;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

public final class CoreBotCacheStore {

    private final PlayerOptions playerOptions;

    public CoreBotCacheStore(PlayerOptions playerOptions) {
        this.playerOptions = playerOptions;
    }

    public void cacheOptions(BotOptions options, UUID ownerUUID, Set<UUID> teamOwners) {
        playerOptions.put(ownerUUID, options);
        if (options.getBotType() == BotType.TEAM_ALLY) {
            for (UUID teamOwner : teamOwners) {
                playerOptions.put(teamOwner, options);
            }
        }
    }

    public void removeCachedOptions(UUID ownerUUID, @Nullable BotOptions options) {
        playerOptions.remove(ownerUUID);
        if (options != null && options.getBotType() == BotType.TEAM_ALLY) {
            for (UUID teamOwner : options.getTeamOwnerUUIDs()) {
                playerOptions.remove(teamOwner);
            }
        }
    }

    public void replaceTeamOwnerMappings(BotOptions options, Set<UUID> previousOwners, Set<UUID> newOwners) {
        for (UUID previousOwner : previousOwners) {
            playerOptions.remove(previousOwner);
        }
        for (UUID teamOwner : newOwners) {
            playerOptions.put(teamOwner, options);
        }
    }

    public void clear() {
        playerOptions.clear();
    }
}
