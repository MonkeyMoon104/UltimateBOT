package com.monkey.mcbot.api.managers;

import com.monkey.mcbot.api.model.*;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

/**
 * Public service interface for creating, managing and removing MinecraftBot instances.
 *
 * <p>This contract is intended for third-party plugins that integrate with MinecraftBot.
 * It supports both UUID-based operations and convenience parsing from string references.</p>
 */
public interface IBotManager {

    /**
     * Checks whether a bot is currently active for the given owner.
     *
     * <p>For TEAM_ALLY bots, implementations may also return {@code true} for team members
     * mapped to the same shared bot.</p>
     *
     * @param ownerUUID owner UUID to check
     * @return {@code true} if an active bot exists for the owner context
     */
    boolean isBotSpawned(UUID ownerUUID);

    /**
     * Returns a snapshot for the bot associated with the specified owner.
     *
     * @param ownerUUID owner UUID
     * @return snapshot when found, otherwise empty
     */
    Optional<BotSnapshot> getBot(UUID ownerUUID);

    default Optional<BotSnapshot> getBotByBotUUID(UUID botUUID) {
        return Optional.empty();
    }

    /**
     * Returns the TEAM_ALLY bot snapshot associated with a team owner UUID.
     *
     * @param teamOwnerUUID one team member UUID
     * @return team bot snapshot when available, otherwise empty
     */
    Optional<BotSnapshot> getTeamAllyBot(UUID teamOwnerUUID);

    /**
     * Resolves the primary owner UUID used internally for a TEAM_ALLY bot.
     *
     * @param teamOwnerUUID one team member UUID
     * @return primary owner UUID when TEAM_ALLY mapping exists, otherwise empty
     */
    Optional<UUID> findTeamAllyPrimaryOwner(UUID teamOwnerUUID);

    /**
     * Spawns a bot using a fully structured spawn request.
     *
     * <p>On success, the returned result contains a snapshot of the spawned bot.</p>
     *
     * @param request validated request describing mode, owners, targets and settings
     * @return operation result with success flag, message and optional snapshot
     */
    BotOperationResult spawn(BotSpawnRequest request);

    /**
     * Spawns a bot by parsing player references (name/UUID strings).
     *
     * <p>This is a convenience overload for command-like integrations where player identifiers
     * are provided as strings.</p>
     *
     * @param mode bot mode to spawn
     * @param ownerReference owner reference (name or UUID string), may be null for TEAM_ALLY
     * @param targetReferences target references (name or UUID strings), may be null
     * @param teamOwnerReferences team owner references for TEAM_ALLY, may be null
     * @param settings spawn settings; when null, implementation-defined defaults may be used
     * @return operation result with success flag and details
     */
    BotOperationResult spawnByReferences(BotMode mode,
                                         String ownerReference,
                                         Collection<String> targetReferences,
                                         Collection<String> teamOwnerReferences,
                                         BotSettings settings);

    /**
     * Parses a single player reference into an online UUID.
     *
     * <p>Implementations commonly support exact name, fuzzy name and UUID string parsing,
     * but usually resolve only online players.</p>
     *
     * @param playerReference player name or UUID string
     * @return resolved UUID when parsing succeeds and player is available, otherwise empty
     */
    Optional<UUID> parsePlayerReference(String playerReference);

    /**
     * Parses a collection of player references into UUID mappings.
     *
     * <p>Entries that cannot be resolved are omitted from the returned map.</p>
     *
     * @param playerReferences collection of references to parse
     * @return map {@code reference -> uuid} containing only successfully resolved entries
     */
    Map<String, UUID> parsePlayerReferences(Collection<String> playerReferences);

    /**
     * Updates the current totem count for a managed bot.
     *
     * <p>The update can fail when the bot does not exist, the setting is locked, or the
     * provided value is outside the allowed configured range.</p>
     *
     * @param ownerUUID owner UUID
     * @param totemCount desired totem count
     * @return {@code true} if update was applied
     */
    boolean updateTotems(UUID ownerUUID, int totemCount);

    /**
     * Updates follow behavior for a managed bot.
     *
     * @param ownerUUID owner UUID
     * @param follow whether follow should be enabled
     * @return {@code true} if update was applied
     */
    boolean updateFollow(UUID ownerUUID, boolean follow);

    /**
     * Updates combat behavior for a managed bot.
     *
     * <p>Implementations typically enforce {@code combat=true} only when follow is enabled.</p>
     *
     * @param ownerUUID owner UUID
     * @param combat whether combat should be enabled
     * @return {@code true} if update was applied
     */
    boolean updateCombat(UUID ownerUUID, boolean combat);

    /**
     * Updates blast protection toggle for a managed bot.
     *
     * @param ownerUUID owner UUID
     * @param blastProtection desired global blast protection state
     * @return {@code true} if update was applied
     */
    boolean updateBlastProtection(UUID ownerUUID, boolean blastProtection);

    /**
     * Updates rank for a managed bot.
     *
     * @param ownerUUID owner UUID
     * @param rank desired bot rank
     * @return {@code true} if update was applied
     */
    boolean updateRank(UUID ownerUUID, BotRank rank);

    boolean updateArmor(UUID ownerUUID, Map<EquipmentSlot, ItemStack> armor, Map<EquipmentSlot, Boolean> blastProtection);

    boolean updateEquipment(UUID ownerUUID, Map<Integer, ItemStack> equipment);

    boolean updateEquipmentSlot(UUID ownerUUID, int slot, ItemStack item);

    boolean updateAutoTarget(UUID ownerUUID, boolean autoTarget, double range);

    default boolean updateAutoTargetByBotUUID(UUID botUUID, boolean autoTarget, double range) {
        return false;
    }

    default boolean updateAttackBots(UUID ownerUUID, boolean attackBots) {
        return false;
    }

    default boolean updateAttackBotsByBotUUID(UUID botUUID, boolean attackBots) {
        return false;
    }

    boolean updateWorldGuardPvpRespect(UUID ownerUUID, boolean respectWorldGuardPvp);

    boolean updateStayAfterOwnerDeath(UUID ownerUUID, boolean stayAfterOwnerDeath);

    boolean updateIdleWander(UUID ownerUUID,
                             boolean idleWander,
                             double idleWanderRadius,
                             double idleReturnDistance,
                             long idleReturnDelayMs);

    boolean updateCrystalPvp(UUID ownerUUID, boolean crystalPvp);

    default boolean updateCrystalPvpByBotUUID(UUID botUUID, boolean crystalPvp) {
        return false;
    }

    default boolean updateExplosions(UUID ownerUUID, boolean explosions) {
        return false;
    }

    default boolean updateExplosionsByBotUUID(UUID botUUID, boolean explosions) {
        return false;
    }

    boolean updateEnderPearls(UUID ownerUUID, boolean enderPearls);

    default boolean updateEnderPearlsByBotUUID(UUID botUUID, boolean enderPearls) {
        return false;
    }

    default boolean updateHealing(UUID ownerUUID, boolean healing) {
        return false;
    }

    default boolean updateHealingByBotUUID(UUID botUUID, boolean healing) {
        return false;
    }

    boolean updateKillMessage(UUID ownerUUID, String killMessage);

    boolean disableKillMessage(UUID ownerUUID);

    /**
     * Removes/despawns the bot associated with the given owner.
     *
     * @param ownerUUID owner UUID
     * @return {@code true} if a bot was present and removed
     */
    boolean remove(UUID ownerUUID);

    default boolean removeByBotUUID(UUID botUUID) {
        return false;
    }

    /**
     * Removes all active bots created from the given source.
     *
     * @param source origin filter (e.g. CORE or API)
     * @return number of removed bots
     */
    int removeBySource(BotSource source);

    /**
     * Removes all currently active bots and returns how many were tracked before cleanup.
     *
     * <p>This is the preferred API method for integrations that need an explicit
     * "remove all" operation. {@link #despawnAll()} remains available as a void alias.</p>
     *
     * @return number of bots that were active before removal
     */
    default int removeAll() {
        int activeBots = getActiveBotCount();
        despawnAll();
        return activeBots;
    }

    /**
     * Alias for {@link #remove(UUID)}.
     *
     * @param ownerUUID owner UUID
     */
    void despawn(UUID ownerUUID);

    /**
     * Despawns all currently active bots.
     */
    void despawnAll();

    /**
     * Returns the number of active bots currently tracked by the manager.
     *
     * @return active bot count
     */
    int getActiveBotCount();
}
