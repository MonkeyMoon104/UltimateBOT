package com.monkey.mcbot.api.managers;

import com.monkey.mcbot.api.model.BotMode;
import com.monkey.mcbot.api.model.BotOperationResult;
import com.monkey.mcbot.api.model.BotRank;
import com.monkey.mcbot.api.model.BotSettings;
import com.monkey.mcbot.api.model.BotSnapshot;
import com.monkey.mcbot.api.model.BotSource;
import com.monkey.mcbot.api.model.BotSpawnRequest;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface IBotManager {

    boolean isBotSpawned(UUID ownerUUID);

    Optional<BotSnapshot> getBot(UUID ownerUUID);

    Optional<BotSnapshot> getTeamAllyBot(UUID teamOwnerUUID);

    Optional<UUID> findTeamAllyPrimaryOwner(UUID teamOwnerUUID);

    BotOperationResult spawn(BotSpawnRequest request);

    BotOperationResult spawnByReferences(BotMode mode,
                                         String ownerReference,
                                         Collection<String> targetReferences,
                                         Collection<String> teamOwnerReferences,
                                         BotSettings settings);

    Optional<UUID> parsePlayerReference(String playerReference);

    Map<String, UUID> parsePlayerReferences(Collection<String> playerReferences);

    boolean updateTotems(UUID ownerUUID, int totemCount);

    boolean updateFollow(UUID ownerUUID, boolean follow);

    boolean updateCombat(UUID ownerUUID, boolean combat);

    boolean updateRank(UUID ownerUUID, BotRank rank);

    boolean remove(UUID ownerUUID);

    int removeBySource(BotSource source);

    void despawn(UUID ownerUUID);

    void despawnAll();

    int getActiveBotCount();
}
