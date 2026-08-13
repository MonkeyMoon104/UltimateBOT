package com.monkey.ultimatebot.api.managers;

import com.monkey.ultimatebot.api.model.configuration.BotEquipmentSlot;
import com.monkey.ultimatebot.api.model.configuration.BotEquipmentSlotSetting;
import com.monkey.ultimatebot.api.model.configuration.BotSettings;
import com.monkey.ultimatebot.api.model.runtime.BotOperationResult;
import com.monkey.ultimatebot.api.model.runtime.BotSnapshot;
import com.monkey.ultimatebot.api.model.runtime.BotSpawnRequest;
import com.monkey.ultimatebot.common.model.BlastProtectionSettings;
import com.monkey.ultimatebot.common.model.BotArmorTier;
import com.monkey.ultimatebot.common.model.BotMode;
import com.monkey.ultimatebot.common.model.BotSource;
import com.monkey.ultimatebot.common.model.BotTargetMode;
import com.monkey.ultimatebot.common.model.BrainDefinition;
import com.monkey.ultimatebot.common.model.BrainKey;
import com.monkey.ultimatebot.common.model.CombatMode;
import com.monkey.ultimatebot.common.model.CombatModeDefinition;
import com.monkey.ultimatebot.common.model.CombatTuning;
import com.monkey.ultimatebot.common.model.DifficultyTier;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * Public service interface for creating, managing and removing UltimateBot instances.
 *
 * <p>This contract is intended for third-party plugins that integrate with UltimateBot.
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

    /** Returns a snapshot using the runtime bot entity UUID. */
    Optional<BotSnapshot> getBotByBotUUID(UUID botUUID);

    /** Returns configured combat modes supported by this server platform (omits unsupported). */
    List<CombatModeDefinition> getCombatModes();

    /** Returns the complete server definition for one combat mode when supported. */
    Optional<CombatModeDefinition> getCombatMode(CombatMode combatMode);

    /** Returns every custom brain currently registered on the server. */
    List<BrainDefinition> getBrains();

    /** Returns one custom brain definition. */
    Optional<BrainDefinition> getBrain(BrainKey brainKey);

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
    BotOperationResult spawnByReferences(
            BotMode mode,
            @Nullable String ownerReference,
            @Nullable Collection<String> targetReferences,
            @Nullable Collection<String> teamOwnerReferences,
            @Nullable BotSettings settings);

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

    /** Updates the current totem count using the runtime bot UUID. */
    boolean updateTotemsByBotUUID(UUID botUUID, int totemCount);

    /**
     * Updates follow behavior for a managed bot.
     *
     * @param ownerUUID owner UUID
     * @param follow whether follow should be enabled
     * @return {@code true} if update was applied
     */
    boolean updateFollow(UUID ownerUUID, boolean follow);

    /** Updates follow behavior using the runtime bot UUID. */
    boolean updateFollowByBotUUID(UUID botUUID, boolean follow);

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

    /** Updates combat behavior using the runtime bot UUID. */
    boolean updateCombatByBotUUID(UUID botUUID, boolean combat);

    /**
     * Updates blast protection toggle for a managed bot.
     *
     * @param ownerUUID owner UUID
     * @param blastProtection desired global blast protection state
     * @return {@code true} if update was applied
     */
    boolean updateBlastProtection(UUID ownerUUID, boolean blastProtection);

    /** Updates blast protection independently for each standard armor piece. */
    boolean updateBlastProtection(UUID ownerUUID, BlastProtectionSettings blastProtection);

    /** Updates blast protection using the runtime bot UUID. */
    boolean updateBlastProtectionByBotUUID(UUID botUUID, boolean blastProtection);

    /** Updates per-piece blast protection using the runtime bot UUID. */
    boolean updateBlastProtectionByBotUUID(UUID botUUID, BlastProtectionSettings blastProtection);

    /**
     * Updates difficulty for a managed bot.
     *
     * @param ownerUUID owner UUID
     * @param difficulty desired bot difficulty
     * @return {@code true} if update was applied
     */
    boolean updateDifficulty(UUID ownerUUID, DifficultyTier difficulty);

    /** Updates difficulty using the runtime bot UUID. */
    boolean updateDifficultyByBotUUID(UUID botUUID, DifficultyTier difficulty);

    /** Changes the active combat mode for an existing bot. */
    boolean updateCombatMode(UUID ownerUUID, CombatMode combatMode);

    /** Changes the active combat mode using the runtime bot UUID. */
    boolean updateCombatModeByBotUUID(UUID botUUID, CombatMode combatMode);

    /** Assigns a custom brain to an active bot independently of its combat mode. */
    boolean updateBrain(UUID ownerUUID, BrainKey brainKey);

    /** Assigns a custom brain using the runtime bot UUID. */
    boolean updateBrainByBotUUID(UUID botUUID, BrainKey brainKey);

    /** Restores the brain declared by the selected combat mode, or the built-in brain. */
    boolean resetBrain(UUID ownerUUID);

    /** Restores the default brain using the runtime bot UUID. */
    boolean resetBrainByBotUUID(UUID botUUID);

    /** Overrides tuning for the bot's current combat mode and difficulty. */
    boolean updateCombatTuning(UUID ownerUUID, CombatTuning combatTuning);

    /** Overrides tuning using the runtime bot UUID. */
    boolean updateCombatTuningByBotUUID(UUID botUUID, CombatTuning combatTuning);

    /** Restores server tuning for the bot's current combat mode and difficulty. */
    boolean resetCombatTuning(UUID ownerUUID);

    /** Restores server tuning using the runtime bot UUID. */
    boolean resetCombatTuningByBotUUID(UUID botUUID);

    boolean updateArmor(
            UUID ownerUUID, Map<EquipmentSlot, ItemStack> armor, Map<EquipmentSlot, Boolean> blastProtection);

    /** Replaces all standard armor pieces with one validated armor tier. */
    boolean updateArmorType(UUID ownerUUID, BotArmorTier armorType);

    /** Replaces all standard armor pieces using the runtime bot UUID. */
    boolean updateArmorTypeByBotUUID(UUID botUUID, BotArmorTier armorType);

    /** Replaces armor contents using the runtime bot UUID. */
    boolean updateArmorByBotUUID(
            UUID botUUID, Map<EquipmentSlot, ItemStack> armor, Map<EquipmentSlot, Boolean> blastProtection);

    boolean updateEquipment(UUID ownerUUID, Map<Integer, ItemStack> equipment);

    /** Replaces inventory equipment using the runtime bot UUID. */
    boolean updateEquipmentByBotUUID(UUID botUUID, Map<Integer, ItemStack> equipment);

    boolean updateEquipmentSlot(UUID ownerUUID, int slot, ItemStack item);

    /** Updates an inventory slot using the runtime bot UUID. */
    boolean updateEquipmentSlotByBotUUID(UUID botUUID, int slot, ItemStack item);

    /** Applies or removes a persistent equipment-slot setting. */
    boolean updateEquipmentSlot(UUID ownerUUID, BotEquipmentSlot slot, BotEquipmentSlotSetting setting);

    /** Applies or removes a persistent equipment-slot setting using the bot UUID. */
    boolean updateEquipmentSlotByBotUUID(UUID botUUID, BotEquipmentSlot slot, BotEquipmentSlotSetting setting);

    boolean updateAutoTarget(UUID ownerUUID, boolean autoTarget, double range);

    boolean updateAutoTargetByBotUUID(UUID botUUID, boolean autoTarget, double range);

    boolean updateAttackBots(UUID ownerUUID, boolean attackBots);

    boolean updateAttackBotsByBotUUID(UUID botUUID, boolean attackBots);

    boolean updateTargetMode(UUID ownerUUID, BotTargetMode targetMode);

    boolean updateTargetModeByBotUUID(UUID botUUID, BotTargetMode targetMode);

    /** Replaces the complete target allow-list for a bot. */
    boolean updateTargets(UUID ownerUUID, Set<UUID> targetUUIDs);

    /** Replaces the complete target allow-list using the runtime bot UUID. */
    boolean updateTargetsByBotUUID(UUID botUUID, Set<UUID> targetUUIDs);

    /** Replaces the team-owner set used by a shared team bot. */
    boolean updateTeamOwners(UUID ownerUUID, Set<UUID> teamOwnerUUIDs);

    /** Replaces team owners using the runtime bot UUID. */
    boolean updateTeamOwnersByBotUUID(UUID botUUID, Set<UUID> teamOwnerUUIDs);

    boolean updateWorldGuardPvpRespect(UUID ownerUUID, boolean respectWorldGuardPvp);

    /** Updates WorldGuard PvP behavior using the runtime bot UUID. */
    boolean updateWorldGuardPvpRespectByBotUUID(UUID botUUID, boolean respectWorldGuardPvp);

    boolean updateStayAfterOwnerDeath(UUID ownerUUID, boolean stayAfterOwnerDeath);

    /** Updates owner-death persistence using the runtime bot UUID. */
    boolean updateStayAfterOwnerDeathByBotUUID(UUID botUUID, boolean stayAfterOwnerDeath);

    boolean updateIdleWander(
            UUID ownerUUID,
            boolean idleWander,
            double idleWanderRadius,
            double idleReturnDistance,
            long idleReturnDelayMs);

    /** Updates idle wandering using the runtime bot UUID. */
    boolean updateIdleWanderByBotUUID(
            UUID botUUID,
            boolean idleWander,
            double idleWanderRadius,
            double idleReturnDistance,
            long idleReturnDelayMs);

    boolean updateCrystalPvp(UUID ownerUUID, boolean crystalPvp);

    boolean updateCrystalPvpByBotUUID(UUID botUUID, boolean crystalPvp);

    boolean updateExplosions(UUID ownerUUID, boolean explosions);

    boolean updateExplosionsByBotUUID(UUID botUUID, boolean explosions);

    boolean updateExplosionBlockDamage(UUID ownerUUID, boolean explosionBlockDamage);

    boolean updateExplosionBlockDamageByBotUUID(UUID botUUID, boolean explosionBlockDamage);

    boolean updateEnderPearls(UUID ownerUUID, boolean enderPearls);

    boolean updateEnderPearlsByBotUUID(UUID botUUID, boolean enderPearls);

    boolean updateHealing(UUID ownerUUID, boolean healing);

    boolean updateHealingByBotUUID(UUID botUUID, boolean healing);

    boolean updateKillMessage(UUID ownerUUID, String killMessage);

    /** Updates the kill message using the runtime bot UUID. */
    boolean updateKillMessageByBotUUID(UUID botUUID, String killMessage);

    boolean disableKillMessage(UUID ownerUUID);

    /** Disables the kill message using the runtime bot UUID. */
    boolean disableKillMessageByBotUUID(UUID botUUID);

    /**
     * Removes/despawns the bot associated with the given owner.
     *
     * @param ownerUUID owner UUID
     * @return {@code true} if a bot was present and removed
     */
    boolean remove(UUID ownerUUID);

    boolean removeByBotUUID(UUID botUUID);

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
    int removeAll();

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
