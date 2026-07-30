package com.monkey.mcbot.api.managers;

import com.monkey.mcbot.api.model.BotSnapshot;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Read-only registry view for currently active bots.
 *
 * <p>This interface is intended for inspection and monitoring use-cases.
 * Mutation operations are exposed through {@link IBotManager}.</p>
 */
public interface IBotRegistry {

    /**
     * Returns a snapshot map of all active bots.
     *
     * @return immutable or defensive-copied map keyed by owner UUID
     */
    Map<UUID, BotSnapshot> getAllBots();

    /**
     * Returns the bot snapshot for a specific owner.
     *
     * @param ownerUUID owner UUID
     * @return snapshot when present, otherwise empty
     */
    Optional<BotSnapshot> getBot(UUID ownerUUID);

    /**
     * Returns the runtime bot entity UUID for the given owner.
     *
     * @param ownerUUID owner UUID
     * @return bot UUID when present, otherwise empty
     */
    Optional<UUID> getBotUUID(UUID ownerUUID);

    /**
     * Checks whether a bot exists for the provided owner.
     *
     * @param ownerUUID owner UUID
     * @return {@code true} if a bot is currently tracked
     */
    boolean isBotSpawned(UUID ownerUUID);

    /**
     * Returns the number of tracked active bots.
     *
     * @return registry size
     */
    int size();
}
