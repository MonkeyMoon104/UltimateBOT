package com.monkey.ultimatebot.integration.api;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.api.event.base.BotEventSource;
import com.monkey.ultimatebot.api.event.state.BotSettingKey;
import com.monkey.ultimatebot.api.managers.IBotManager;
import com.monkey.ultimatebot.api.model.configuration.BotEquipmentSlot;
import com.monkey.ultimatebot.api.model.configuration.BotEquipmentSlotMode;
import com.monkey.ultimatebot.api.model.configuration.BotEquipmentSlotSetting;
import com.monkey.ultimatebot.api.model.configuration.BotSettings;
import com.monkey.ultimatebot.api.model.identity.BotSkin;
import com.monkey.ultimatebot.api.model.identity.BotSkinSource;
import com.monkey.ultimatebot.api.model.runtime.BotOperationResult;
import com.monkey.ultimatebot.api.model.runtime.BotSnapshot;
import com.monkey.ultimatebot.api.model.runtime.BotSpawnRequest;
import com.monkey.ultimatebot.bot.*;
import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.common.model.AutoTargetSettings;
import com.monkey.ultimatebot.common.model.BlastProtectionSettings;
import com.monkey.ultimatebot.common.model.BotArmorTier;
import com.monkey.ultimatebot.common.model.BotMode;
import com.monkey.ultimatebot.common.model.BotSource;
import com.monkey.ultimatebot.common.model.BotTargetMode;
import com.monkey.ultimatebot.common.model.CombatMode;
import com.monkey.ultimatebot.common.model.CombatModeDefinition;
import com.monkey.ultimatebot.common.model.CombatTuning;
import com.monkey.ultimatebot.common.model.DifficultyTier;
import com.monkey.ultimatebot.common.model.IdleWanderSettings;
import com.monkey.ultimatebot.common.model.KillMessageSettings;
import com.monkey.ultimatebot.event.BotEventSourceContext;
import com.monkey.ultimatebot.event.BotSettingEvents;
import com.monkey.ultimatebot.utils.armor.PlayerOptions;
import java.util.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

public final class CoreBotManagerAdapter implements IBotManager {

    private final UltimateBot plugin;
    private final BotManager botManager;
    private final BotRegistry botRegistry;
    private final PlayerOptions playerOptions;
    private final ApiBotOptionsFactory optionsFactory;

    public CoreBotManagerAdapter(
            UltimateBot plugin, BotManager botManager, BotRegistry botRegistry, PlayerOptions playerOptions) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.botManager = Objects.requireNonNull(botManager, "botManager");
        this.botRegistry = Objects.requireNonNull(botRegistry, "botRegistry");
        this.playerOptions = Objects.requireNonNull(playerOptions, "playerOptions");
        this.optionsFactory = new ApiBotOptionsFactory(plugin);
    }

    @Override
    public boolean isBotSpawned(UUID ownerUUID) {
        return ownerUUID != null && (botManager.isBotSpawned(ownerUUID) || botManager.hasActiveTeamAlly(ownerUUID));
    }

    @Override
    public Optional<BotSnapshot> getBot(UUID ownerUUID) {
        if (ownerUUID == null) {
            return Optional.empty();
        }

        UUID managedOwner = resolveManagedOwner(ownerUUID);
        ITrainingBot bot = botManager.getBotSafe(managedOwner);
        return Optional.ofNullable(BotSnapshotMapper.toSnapshot(managedOwner, bot));
    }

    @Override
    public Optional<BotSnapshot> getBotByBotUUID(UUID botUUID) {
        UUID ownerUUID = botRegistry.getOwnerUUIDByBotUUID(botUUID);
        return ownerUUID == null ? Optional.empty() : getBot(ownerUUID);
    }

    @Override
    public List<CombatModeDefinition> getCombatModes() {
        return Arrays.stream(CombatMode.values())
                .map(this::combatModeDefinition)
                .toList();
    }

    @Override
    public Optional<CombatModeDefinition> getCombatMode(CombatMode combatMode) {
        return combatMode == null ? Optional.empty() : Optional.of(combatModeDefinition(combatMode));
    }

    @Override
    public Optional<BotSnapshot> getTeamAllyBot(UUID teamOwnerUUID) {
        if (teamOwnerUUID == null) {
            return Optional.empty();
        }

        UUID primaryOwner = botManager.findTeamAllyPrimaryOwner(teamOwnerUUID);
        if (primaryOwner == null) {
            return Optional.empty();
        }

        return getBot(primaryOwner);
    }

    @Override
    public Optional<UUID> findTeamAllyPrimaryOwner(UUID teamOwnerUUID) {
        if (teamOwnerUUID == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(botManager.findTeamAllyPrimaryOwner(teamOwnerUUID));
    }

    @Override
    public BotOperationResult spawn(BotSpawnRequest request) {
        if (request == null) {
            return BotOperationResult.failure("request cannot be null");
        }

        BotType botType = toCoreType(request.mode());
        BotSettings settings = request.settings() == null ? defaultSettings() : request.settings();

        Set<UUID> targetUUIDs = new LinkedHashSet<>(request.targetUUIDs());
        Set<UUID> teamOwners = new LinkedHashSet<>(request.teamOwnerUUIDs());
        if (botType == BotType.TEAM_ALLY && request.ownerUUID() != null) {
            teamOwners.add(request.ownerUUID());
        }

        String skinValidationError = validateSkinForMode(botType, settings);
        if (skinValidationError != null) {
            return spawnFailure(skinValidationError);
        }

        if (botType != BotType.TEAM_ALLY && teamOwners.size() > 1) {
            return spawnFailure(botType + " supports exactly one owner.");
        }

        UUID contextOwnerUUID = resolvePrimaryOwnerUUID(botType, request.ownerUUID(), teamOwners, targetUUIDs);
        if (contextOwnerUUID == null) {
            return spawnFailure(
                    botType == BotType.EVENT
                            ? "Cannot resolve an online context player for event bot spawn."
                            : "Cannot resolve an online owner for spawn.");
        }

        Player ownerPlayer = Bukkit.getPlayer(contextOwnerUUID);
        if (ownerPlayer == null || !ownerPlayer.isOnline()) {
            return spawnFailure("Owner must be online.");
        }

        UUID primaryOwnerUUID = resolveManagedOwnerUUID(botType, request.ownerUUID(), contextOwnerUUID);

        if (botType == BotType.TEAM_ALLY && teamOwners.size() < 2) {
            return spawnFailure("TEAM_ALLY requires at least two owners.");
        }

        if (botType == BotType.SINGLE) {
            targetUUIDs.clear();
            targetUUIDs.add(primaryOwnerUUID);
        }

        if (botType == BotType.TEAM_ALLY) {
            for (UUID teamOwner : teamOwners) {
                BotType busyType = getOwnerBusyType(teamOwner, primaryOwnerUUID);
                if (busyType != null) {
                    return spawnFailure("Owner " + teamOwner + " already has an active " + busyType + " bot.");
                }
            }
        }

        UUID targetUUID = targetUUIDs.stream().findFirst().orElse(primaryOwnerUUID);
        Player targetPlayer = Bukkit.getPlayer(targetUUID);
        if (targetPlayer == null || !targetPlayer.isOnline()) {
            targetPlayer = ownerPlayer;
            targetUUID = primaryOwnerUUID;
        }

        String uuidValidationError = validateRequestedBotUUID(request.botUUID());
        if (uuidValidationError != null) {
            return spawnFailure(uuidValidationError);
        }

        BotOptions options = optionsFactory.create(
                botType,
                primaryOwnerUUID,
                targetUUID,
                targetUUIDs,
                teamOwners,
                request.botUUID(),
                request.equipmentSlots(),
                settings);
        boolean spawned = botManager.spawn(
                ownerPlayer,
                targetPlayer,
                options.getArmor(),
                options.getBlast(),
                options.isFollow(),
                options.getTotems(),
                options);

        if (!spawned) {
            return spawnFailure("Bot spawn was cancelled by an event listener.");
        }

        cacheOptions(options, primaryOwnerUUID, teamOwners);

        BotSnapshot snapshot = getBot(primaryOwnerUUID).orElse(null);
        return BotOperationResult.success("Bot spawned successfully.", snapshot);
    }

    private @Nullable String validateRequestedBotUUID(@Nullable UUID requestedBotUUID) {
        if (requestedBotUUID == null) {
            return null;
        }
        if (requestedBotUUID.equals(new UUID(0L, 0L))) {
            return "Bot UUID cannot be the nil UUID.";
        }
        if (botRegistry.getOwnerUUIDByBotUUID(requestedBotUUID) != null) {
            return "Bot UUID is already assigned to an active UltimateBot.";
        }
        if (Bukkit.getPlayer(requestedBotUUID) != null || Bukkit.getEntity(requestedBotUUID) != null) {
            return "Bot UUID is already used by a loaded player or entity.";
        }
        return null;
    }

    @Override
    public BotOperationResult spawnByReferences(
            BotMode mode,
            @Nullable String ownerReference,
            @Nullable Collection<String> targetReferences,
            @Nullable Collection<String> teamOwnerReferences,
            @Nullable BotSettings settings) {
        if (mode == null) {
            return BotOperationResult.failure("mode cannot be null");
        }

        UUID ownerUUID = null;
        if (ownerReference != null && !ownerReference.isBlank()) {
            ownerUUID = parsePlayerReference(ownerReference).orElse(null);
            if (ownerUUID == null) {
                return spawnFailure("Cannot parse owner reference: " + ownerReference);
            }
        }

        Set<UUID> targetUUIDs = new LinkedHashSet<>();
        if (targetReferences != null) {
            for (String targetReference : targetReferences) {
                UUID parsedTarget = parsePlayerReference(targetReference).orElse(null);
                if (parsedTarget == null) {
                    return spawnFailure("Cannot parse target reference: " + targetReference);
                }
                targetUUIDs.add(parsedTarget);
            }
        }

        Set<UUID> teamOwners = new LinkedHashSet<>();
        if (teamOwnerReferences != null) {
            for (String reference : teamOwnerReferences) {
                UUID parsed = parsePlayerReference(reference).orElse(null);
                if (parsed == null) {
                    return spawnFailure("Cannot parse team owner reference: " + reference);
                }
                teamOwners.add(parsed);
            }
        }

        try {
            BotSpawnRequest request = BotSpawnRequest.builder(mode)
                    .owner(ownerUUID)
                    .targets(targetUUIDs)
                    .teamOwners(teamOwners)
                    .settings(settings == null ? defaultSettings() : settings)
                    .build();
            return spawn(request);
        } catch (IllegalArgumentException ex) {
            return spawnFailure(ex.getMessage());
        }
    }

    @Override
    public Optional<UUID> parsePlayerReference(@Nullable String playerReference) {
        if (playerReference == null || playerReference.isBlank()) {
            return Optional.empty();
        }

        Player exact = Bukkit.getPlayerExact(playerReference);
        if (exact != null && exact.isOnline()) {
            return Optional.of(exact.getUniqueId());
        }

        try {
            UUID uuid = UUID.fromString(playerReference);
            Player byUuid = Bukkit.getPlayer(uuid);
            if (byUuid != null && byUuid.isOnline()) {
                return Optional.of(byUuid.getUniqueId());
            }
        } catch (IllegalArgumentException ignored) {
            Bukkit.getLogger().finest(() -> "Player reference is not a UUID: " + playerReference);
        }

        Player fuzzy = Bukkit.getPlayer(playerReference);
        if (fuzzy != null && fuzzy.isOnline()) {
            return Optional.of(fuzzy.getUniqueId());
        }

        return Optional.empty();
    }

    @Override
    public Map<String, UUID> parsePlayerReferences(Collection<String> playerReferences) {
        Map<String, UUID> parsed = new LinkedHashMap<>();
        if (playerReferences == null) {
            return parsed;
        }

        for (String reference : playerReferences) {
            parsePlayerReference(reference).ifPresent(uuid -> parsed.put(reference, uuid));
        }

        return parsed;
    }

    @Override
    public boolean updateTotems(UUID ownerUUID, int totemCount) {
        UUID managedOwner = resolveManagedOwner(ownerUUID);
        ITrainingBot bot = getLiveBot(managedOwner);
        if (bot == null || bot.getBrainController() == null) {
            return false;
        }

        BotOptions options = bot.getBrainController().getBotOptions();
        if (options == null) {
            return false;
        }

        if (!options.isChangeableTotem()) {
            return false;
        }

        int clamped = options.clampTotemCount(totemCount);
        if (clamped != totemCount) {
            return false;
        }

        Optional<Integer> proposed =
                proposedChange(managedOwner, BotSettingKey.TOTEM_COUNT, options.getTotems(), totemCount, Integer.class);
        if (proposed.isEmpty()) return false;
        options.setTotems(proposed.get());
        botManager.updateTotem(managedOwner, proposed.get());
        return true;
    }

    @Override
    public boolean updateTotemsByBotUUID(UUID botUUID, int totemCount) {
        UUID ownerUUID = resolveOwnerByBotUUID(botUUID);
        return ownerUUID != null && updateTotems(ownerUUID, totemCount);
    }

    @Override
    public boolean updateFollow(UUID ownerUUID, boolean follow) {
        UUID managedOwner = resolveManagedOwner(ownerUUID);
        ITrainingBot bot = getLiveBot(managedOwner);
        if (bot == null || bot.getBrainController() == null) {
            return false;
        }

        BotOptions options = bot.getBrainController().getBotOptions();
        if (options == null) {
            return false;
        }

        if (!options.isChangeableFollow()) {
            return false;
        }

        Optional<Boolean> proposed =
                proposedChange(managedOwner, BotSettingKey.FOLLOW, options.isFollow(), follow, Boolean.class);
        if (proposed.isEmpty()) return false;
        options.setFollow(proposed.get());
        botManager.updateFollow(managedOwner, proposed.get());
        return true;
    }

    @Override
    public boolean updateFollowByBotUUID(UUID botUUID, boolean follow) {
        UUID ownerUUID = resolveOwnerByBotUUID(botUUID);
        return ownerUUID != null && updateFollow(ownerUUID, follow);
    }

    @Override
    public boolean updateCombat(UUID ownerUUID, boolean combat) {
        UUID managedOwner = resolveManagedOwner(ownerUUID);
        ITrainingBot bot = getLiveBot(managedOwner);
        if (bot == null || bot.getBrainController() == null) {
            return false;
        }

        BotOptions options = bot.getBrainController().getBotOptions();
        if (options == null) {
            return false;
        }

        if (!options.isChangeableCombat()) {
            return false;
        }

        Optional<Boolean> proposed =
                proposedChange(managedOwner, BotSettingKey.COMBAT, options.isCombat(), combat, Boolean.class);
        if (proposed.isEmpty()) return false;
        options.setCombat(proposed.get());
        botManager.updateCombat(managedOwner, proposed.get());
        if (proposed.get()) {
            botManager.switchBotToSword(managedOwner);
        }
        return true;
    }

    @Override
    public boolean updateCombatByBotUUID(UUID botUUID, boolean combat) {
        UUID ownerUUID = resolveOwnerByBotUUID(botUUID);
        return ownerUUID != null && updateCombat(ownerUUID, combat);
    }

    @Override
    public boolean updateBlastProtection(UUID ownerUUID, boolean blastProtection) {
        return updateBlastProtection(ownerUUID, BlastProtectionSettings.all(blastProtection));
    }

    @Override
    public boolean updateBlastProtection(UUID ownerUUID, BlastProtectionSettings blastProtection) {
        UUID managedOwner = resolveManagedOwner(ownerUUID);
        ITrainingBot bot = getLiveBot(managedOwner);
        if (bot == null || bot.getBrainController() == null) {
            return false;
        }

        BotOptions options = bot.getBrainController().getBotOptions();
        if (options == null) {
            return false;
        }

        if (!options.isChangeableBlast() || blastProtection == null) {
            return false;
        }
        BlastProtectionSettings current = new BlastProtectionSettings(
                options.getBlast().getOrDefault(org.bukkit.inventory.EquipmentSlot.FEET, false),
                options.getBlast().getOrDefault(org.bukkit.inventory.EquipmentSlot.LEGS, false),
                options.getBlast().getOrDefault(org.bukkit.inventory.EquipmentSlot.CHEST, false),
                options.getBlast().getOrDefault(org.bukkit.inventory.EquipmentSlot.HEAD, false));
        Optional<BlastProtectionSettings> proposed = proposedChange(
                managedOwner, BotSettingKey.BLAST_PROTECTION, current, blastProtection, BlastProtectionSettings.class);
        if (proposed.isEmpty()) {
            return false;
        }
        BlastProtectionSettings accepted = proposed.get();
        options.setBlastProtection(accepted.boots(), accepted.leggings(), accepted.chestplate(), accepted.helmet());
        botManager.updateArmor(managedOwner, options.getArmor(), options.getBlast());
        return true;
    }

    @Override
    public boolean updateBlastProtectionByBotUUID(UUID botUUID, boolean blastProtection) {
        UUID ownerUUID = resolveOwnerByBotUUID(botUUID);
        return ownerUUID != null && updateBlastProtection(ownerUUID, blastProtection);
    }

    @Override
    public boolean updateBlastProtectionByBotUUID(UUID botUUID, BlastProtectionSettings blastProtection) {
        UUID ownerUUID = resolveOwnerByBotUUID(botUUID);
        return ownerUUID != null && updateBlastProtection(ownerUUID, blastProtection);
    }

    @Override
    public boolean updateDifficulty(UUID ownerUUID, DifficultyTier difficulty) {
        if (difficulty == null) {
            return false;
        }

        UUID managedOwner = resolveManagedOwner(ownerUUID);
        ITrainingBot bot = getLiveBot(managedOwner);
        if (bot == null || bot.getBrainController() == null) {
            return false;
        }

        BotOptions options = bot.getBrainController().getBotOptions();
        if (options == null) {
            return false;
        }

        if (!options.isChangeableDifficulty()) {
            return false;
        }

        com.monkey.ultimatebot.bot.ai.difficulty.DifficultyLevel coreDifficulty =
                ApiBotOptionsFactory.toCoreDifficulty(difficulty);
        if (!options.isDifficultyAllowed(coreDifficulty)) {
            return false;
        }
        Optional<com.monkey.ultimatebot.bot.ai.difficulty.DifficultyLevel> proposed = proposedChange(
                managedOwner,
                BotSettingKey.DIFFICULTY,
                options.getDifficulty(),
                coreDifficulty,
                com.monkey.ultimatebot.bot.ai.difficulty.DifficultyLevel.class);
        if (proposed.isEmpty() || !options.isDifficultyAllowed(proposed.get())) return false;
        options.setDifficulty(proposed.get());
        botManager.setDifficultyLevel(managedOwner, proposed.get());
        return true;
    }

    @Override
    public boolean updateDifficultyByBotUUID(UUID botUUID, DifficultyTier difficulty) {
        UUID ownerUUID = resolveOwnerByBotUUID(botUUID);
        return ownerUUID != null && updateDifficulty(ownerUUID, difficulty);
    }

    @Override
    public boolean updateCombatMode(UUID ownerUUID, CombatMode combatMode) {
        UUID managedOwner = resolveManagedOwner(ownerUUID);
        BotOptions options = getLiveOptions(managedOwner);
        if (options == null || combatMode == null || !options.isChangeableCombatMode()) {
            return false;
        }
        if (!plugin.getCombatProfileCatalog().configuration(combatMode).enabled()) {
            return false;
        }
        Optional<CombatMode> proposed = proposedChange(
                managedOwner, BotSettingKey.COMBAT_MODE, options.getCombatMode(), combatMode, CombatMode.class);
        if (proposed.isEmpty()) {
            return false;
        }
        options.setCombatMode(proposed.get());
        return true;
    }

    @Override
    public boolean updateCombatModeByBotUUID(UUID botUUID, CombatMode combatMode) {
        UUID ownerUUID = botRegistry.getOwnerUUIDByBotUUID(botUUID);
        return ownerUUID != null && updateCombatMode(ownerUUID, combatMode);
    }

    @Override
    public boolean updateCombatTuning(UUID ownerUUID, CombatTuning combatTuning) {
        UUID managedOwner = resolveManagedOwner(ownerUUID);
        BotOptions options = getLiveOptions(managedOwner);
        if (options == null || combatTuning == null || !options.isChangeableCombatMode()) {
            return false;
        }
        Optional<CombatTuning> proposed = proposedChange(
                managedOwner, BotSettingKey.COMBAT_TUNING, options.getCombatTuning(), combatTuning, CombatTuning.class);
        if (proposed.isEmpty()) {
            return false;
        }
        options.setCustomCombatTuning(proposed.get());
        return true;
    }

    @Override
    public boolean updateCombatTuningByBotUUID(UUID botUUID, CombatTuning combatTuning) {
        UUID ownerUUID = botRegistry.getOwnerUUIDByBotUUID(botUUID);
        return ownerUUID != null && updateCombatTuning(ownerUUID, combatTuning);
    }

    @Override
    public boolean resetCombatTuning(UUID ownerUUID) {
        BotOptions options = getLiveOptions(resolveManagedOwner(ownerUUID));
        if (options == null || !options.isChangeableCombatMode()) {
            return false;
        }
        options.resetCombatTuning();
        return true;
    }

    @Override
    public boolean resetCombatTuningByBotUUID(UUID botUUID) {
        UUID ownerUUID = botRegistry.getOwnerUUIDByBotUUID(botUUID);
        return ownerUUID != null && resetCombatTuning(ownerUUID);
    }

    @Override
    public boolean updateArmor(
            UUID ownerUUID,
            Map<org.bukkit.inventory.EquipmentSlot, org.bukkit.inventory.ItemStack> armor,
            Map<org.bukkit.inventory.EquipmentSlot, Boolean> blastProtection) {
        UUID managedOwner = resolveManagedOwner(ownerUUID);
        BotOptions options = getLiveOptions(managedOwner);
        if (options == null || !options.isChangeableArmor()) {
            return false;
        }
        if (armor != null) {
            options.getArmor().putAll(armor);
        }
        if (blastProtection != null) {
            options.getBlast().putAll(blastProtection);
        }
        botManager.updateArmor(managedOwner, options.getArmor(), options.getBlast());
        return true;
    }

    @Override
    public boolean updateArmorType(UUID ownerUUID, BotArmorTier armorType) {
        UUID managedOwner = resolveManagedOwner(ownerUUID);
        BotOptions options = getLiveOptions(managedOwner);
        if (options == null || armorType == null || !options.isChangeableArmor()) {
            return false;
        }
        com.monkey.ultimatebot.utils.armor.ArmorTier requestedTier =
                com.monkey.ultimatebot.utils.armor.ArmorTier.valueOf(armorType.name());
        if (!options.isArmorTierAllowed(requestedTier)) {
            return false;
        }
        Optional<com.monkey.ultimatebot.utils.armor.ArmorTier> proposed = proposedChange(
                managedOwner,
                BotSettingKey.ARMOR,
                currentArmorTier(options),
                requestedTier,
                com.monkey.ultimatebot.utils.armor.ArmorTier.class);
        if (proposed.isEmpty() || !options.isArmorTierAllowed(proposed.get())) {
            return false;
        }
        options.setArmorType(proposed.get());
        botManager.updateArmor(managedOwner, options.getArmor(), options.getBlast());
        return true;
    }

    @Override
    public boolean updateArmorTypeByBotUUID(UUID botUUID, BotArmorTier armorType) {
        UUID ownerUUID = resolveOwnerByBotUUID(botUUID);
        return ownerUUID != null && updateArmorType(ownerUUID, armorType);
    }

    @Override
    public boolean updateArmorByBotUUID(
            UUID botUUID,
            Map<org.bukkit.inventory.EquipmentSlot, org.bukkit.inventory.ItemStack> armor,
            Map<org.bukkit.inventory.EquipmentSlot, Boolean> blastProtection) {
        UUID ownerUUID = resolveOwnerByBotUUID(botUUID);
        return ownerUUID != null && updateArmor(ownerUUID, armor, blastProtection);
    }

    @Override
    public boolean updateEquipment(UUID ownerUUID, Map<Integer, org.bukkit.inventory.ItemStack> equipment) {
        UUID managedOwner = resolveManagedOwner(ownerUUID);
        BotOptions options = getLiveOptions(managedOwner);
        if (options == null) {
            return false;
        }
        options.setEquipmentContents(equipment);
        if (equipment != null) {
            for (Map.Entry<Integer, org.bukkit.inventory.ItemStack> entry : equipment.entrySet()) {
                updateEquipmentSlot(managedOwner, entry.getKey(), entry.getValue());
            }
        }
        return true;
    }

    @Override
    public boolean updateEquipmentByBotUUID(UUID botUUID, Map<Integer, org.bukkit.inventory.ItemStack> equipment) {
        UUID ownerUUID = resolveOwnerByBotUUID(botUUID);
        return ownerUUID != null && updateEquipment(ownerUUID, equipment);
    }

    @Override
    public boolean updateEquipmentSlot(UUID ownerUUID, int slot, org.bukkit.inventory.ItemStack item) {
        UUID managedOwner = resolveManagedOwner(ownerUUID);
        BotOptions options = getLiveOptions(managedOwner);
        if (options == null || slot < 0 || slot > 40) {
            return false;
        }
        if (item == null) {
            options.getEquipmentContents().remove(slot);
            botManager.updateBotInventorySlot(
                    managedOwner, slot, new org.bukkit.inventory.ItemStack(org.bukkit.Material.AIR));
            return true;
        }
        options.getEquipmentContents().put(slot, item.clone());
        botManager.updateBotInventorySlot(managedOwner, slot, item);
        return true;
    }

    @Override
    public boolean updateEquipmentSlotByBotUUID(UUID botUUID, int slot, org.bukkit.inventory.ItemStack item) {
        UUID ownerUUID = resolveOwnerByBotUUID(botUUID);
        return ownerUUID != null && updateEquipmentSlot(ownerUUID, slot, item);
    }

    @Override
    public boolean updateEquipmentSlot(UUID ownerUUID, BotEquipmentSlot slot, BotEquipmentSlotSetting setting) {
        UUID managedOwner = resolveManagedOwner(ownerUUID);
        ITrainingBot bot = getLiveBot(managedOwner);
        if (bot == null || bot.getBrainController() == null || slot == null || setting == null) {
            return false;
        }
        BotOptions options = bot.getBrainController().getBotOptions();
        if (options == null) {
            return false;
        }
        options.setEquipmentSlotSetting(slot, setting);
        if (setting.mode() == BotEquipmentSlotMode.DEFAULT) {
            BotEquipmentPolicy.restoreDefault(bot, options, slot);
        } else {
            BotEquipmentPolicy.enforce(bot, options);
        }
        return true;
    }

    @Override
    public boolean updateEquipmentSlotByBotUUID(UUID botUUID, BotEquipmentSlot slot, BotEquipmentSlotSetting setting) {
        UUID ownerUUID = botRegistry.getOwnerUUIDByBotUUID(botUUID);
        return ownerUUID != null && updateEquipmentSlot(ownerUUID, slot, setting);
    }

    @Override
    public boolean updateAutoTarget(UUID ownerUUID, boolean autoTarget, double range) {
        UUID managedOwner = resolveManagedOwner(ownerUUID);
        BotOptions options = getLiveOptions(managedOwner);
        if (options == null || range <= 0.0D) {
            return false;
        }
        AutoTargetSettings current = new AutoTargetSettings(options.isAutoTarget(), options.getAutoTargetRange());
        Optional<AutoTargetSettings> proposed = proposedChange(
                managedOwner,
                BotSettingKey.AUTO_TARGET,
                current,
                new AutoTargetSettings(autoTarget, range),
                AutoTargetSettings.class);
        if (proposed.isEmpty()) {
            return false;
        }
        options.setAutoTarget(proposed.get().enabled());
        options.setAutoTargetRange(proposed.get().range());
        return true;
    }

    @Override
    public boolean updateAutoTargetByBotUUID(UUID botUUID, boolean autoTarget, double range) {
        UUID ownerUUID = botRegistry.getOwnerUUIDByBotUUID(botUUID);
        return ownerUUID != null && updateAutoTarget(ownerUUID, autoTarget, range);
    }

    @Override
    public boolean updateAttackBots(UUID ownerUUID, boolean attackBots) {
        BotOptions options = getLiveOptions(resolveManagedOwner(ownerUUID));
        if (options == null) {
            return false;
        }
        Optional<Boolean> proposed = proposedChange(
                resolveManagedOwner(ownerUUID),
                BotSettingKey.ATTACK_BOTS,
                options.isAttackBots(),
                attackBots,
                Boolean.class);
        if (proposed.isEmpty()) return false;
        options.setAttackBots(proposed.get());
        return true;
    }

    @Override
    public boolean updateAttackBotsByBotUUID(UUID botUUID, boolean attackBots) {
        UUID ownerUUID = botRegistry.getOwnerUUIDByBotUUID(botUUID);
        return ownerUUID != null && updateAttackBots(ownerUUID, attackBots);
    }

    @Override
    public boolean updateTargetMode(UUID ownerUUID, BotTargetMode targetMode) {
        BotOptions options = getLiveOptions(resolveManagedOwner(ownerUUID));
        if (options == null || targetMode == null) {
            return false;
        }
        UUID managedOwner = resolveManagedOwner(ownerUUID);
        Optional<BotTargetMode> proposed = proposedChange(
                managedOwner, BotSettingKey.TARGET_MODE, options.getTargetMode(), targetMode, BotTargetMode.class);
        if (proposed.isEmpty()) return false;
        options.setTargetMode(proposed.get());
        return true;
    }

    @Override
    public boolean updateTargetModeByBotUUID(UUID botUUID, BotTargetMode targetMode) {
        UUID ownerUUID = botRegistry.getOwnerUUIDByBotUUID(botUUID);
        return ownerUUID != null && updateTargetMode(ownerUUID, targetMode);
    }

    @Override
    public boolean updateTargets(UUID ownerUUID, Set<UUID> targetUUIDs) {
        UUID managedOwner = resolveManagedOwner(ownerUUID);
        BotOptions options = getLiveOptions(managedOwner);
        if (options == null || targetUUIDs == null) {
            return false;
        }
        Set<UUID> requestedTargets = Set.copyOf(targetUUIDs);
        Optional<Set<UUID>> proposed = BotSettingEvents.proposeUuidSet(
                plugin,
                managedOwner,
                BotEventSourceContext.currentOr(BotEventSource.API),
                BotSettingKey.TARGETS,
                options.getTargetUUIDs(),
                requestedTargets);
        if (proposed.isEmpty()) {
            return false;
        }
        options.setTargetUUIDs(proposed.get());
        return true;
    }

    @Override
    public boolean updateTargetsByBotUUID(UUID botUUID, Set<UUID> targetUUIDs) {
        UUID ownerUUID = resolveOwnerByBotUUID(botUUID);
        return ownerUUID != null && updateTargets(ownerUUID, targetUUIDs);
    }

    @Override
    public boolean updateTeamOwners(UUID ownerUUID, Set<UUID> teamOwnerUUIDs) {
        UUID managedOwner = resolveManagedOwner(ownerUUID);
        BotOptions options = getLiveOptions(managedOwner);
        if (options == null || options.getBotType() != BotType.TEAM_ALLY || teamOwnerUUIDs == null) {
            return false;
        }
        Set<UUID> requestedOwners = Set.copyOf(teamOwnerUUIDs);
        if (requestedOwners.isEmpty()) {
            return false;
        }
        Optional<Set<UUID>> proposed = BotSettingEvents.proposeUuidSet(
                plugin,
                managedOwner,
                BotEventSourceContext.currentOr(BotEventSource.API),
                BotSettingKey.TEAM_OWNERS,
                options.getTeamOwnerUUIDs(),
                requestedOwners);
        if (proposed.isEmpty()) {
            return false;
        }
        LinkedHashSet<UUID> validatedOwners = new LinkedHashSet<>();
        validatedOwners.addAll(proposed.get());
        if (validatedOwners.isEmpty()) {
            return false;
        }
        for (UUID previousOwner : options.getTeamOwnerUUIDs()) {
            playerOptions.remove(previousOwner);
        }
        options.setTeamOwnerUUIDs(validatedOwners);
        for (UUID teamOwner : validatedOwners) {
            playerOptions.put(teamOwner, options);
        }
        return true;
    }

    @Override
    public boolean updateTeamOwnersByBotUUID(UUID botUUID, Set<UUID> teamOwnerUUIDs) {
        UUID ownerUUID = resolveOwnerByBotUUID(botUUID);
        return ownerUUID != null && updateTeamOwners(ownerUUID, teamOwnerUUIDs);
    }

    @Override
    public boolean updateWorldGuardPvpRespect(UUID ownerUUID, boolean respectWorldGuardPvp) {
        UUID managedOwner = resolveManagedOwner(ownerUUID);
        BotOptions options = getLiveOptions(managedOwner);
        if (options == null) {
            return false;
        }
        Optional<Boolean> proposed = proposedChange(
                managedOwner,
                BotSettingKey.WORLD_GUARD_PVP,
                options.isRespectWorldGuardPvp(),
                respectWorldGuardPvp,
                Boolean.class);
        if (proposed.isEmpty()) {
            return false;
        }
        options.setRespectWorldGuardPvp(proposed.get());
        return true;
    }

    @Override
    public boolean updateWorldGuardPvpRespectByBotUUID(UUID botUUID, boolean respectWorldGuardPvp) {
        UUID ownerUUID = resolveOwnerByBotUUID(botUUID);
        return ownerUUID != null && updateWorldGuardPvpRespect(ownerUUID, respectWorldGuardPvp);
    }

    @Override
    public boolean updateStayAfterOwnerDeath(UUID ownerUUID, boolean stayAfterOwnerDeath) {
        UUID managedOwner = resolveManagedOwner(ownerUUID);
        BotOptions options = getLiveOptions(managedOwner);
        if (options == null) {
            return false;
        }
        Optional<Boolean> proposed = proposedChange(
                managedOwner,
                BotSettingKey.STAY_AFTER_OWNER_DEATH,
                options.isStayAfterOwnerDeath(),
                stayAfterOwnerDeath,
                Boolean.class);
        if (proposed.isEmpty()) {
            return false;
        }
        options.setStayAfterOwnerDeath(proposed.get());
        return true;
    }

    @Override
    public boolean updateStayAfterOwnerDeathByBotUUID(UUID botUUID, boolean stayAfterOwnerDeath) {
        UUID ownerUUID = resolveOwnerByBotUUID(botUUID);
        return ownerUUID != null && updateStayAfterOwnerDeath(ownerUUID, stayAfterOwnerDeath);
    }

    @Override
    public boolean updateIdleWander(
            UUID ownerUUID,
            boolean idleWander,
            double idleWanderRadius,
            double idleReturnDistance,
            long idleReturnDelayMs) {
        UUID managedOwner = resolveManagedOwner(ownerUUID);
        BotOptions options = getLiveOptions(managedOwner);
        if (options == null || idleWanderRadius <= 0.0D || idleReturnDistance <= 0.0D || idleReturnDelayMs < 0L) {
            return false;
        }
        IdleWanderSettings current = new IdleWanderSettings(
                options.isIdleWander(),
                options.getIdleWanderRadius(),
                options.getIdleReturnDistance(),
                options.getIdleReturnDelayMs());
        Optional<IdleWanderSettings> proposed = proposedChange(
                managedOwner,
                BotSettingKey.IDLE_WANDER,
                current,
                new IdleWanderSettings(idleWander, idleWanderRadius, idleReturnDistance, idleReturnDelayMs),
                IdleWanderSettings.class);
        if (proposed.isEmpty()) {
            return false;
        }
        options.setIdleWander(proposed.get().enabled());
        options.setIdleWanderRadius(proposed.get().radius());
        options.setIdleReturnDistance(proposed.get().returnDistance());
        options.setIdleReturnDelayMs(proposed.get().returnDelayMs());
        return true;
    }

    @Override
    public boolean updateIdleWanderByBotUUID(
            UUID botUUID,
            boolean idleWander,
            double idleWanderRadius,
            double idleReturnDistance,
            long idleReturnDelayMs) {
        UUID ownerUUID = resolveOwnerByBotUUID(botUUID);
        return ownerUUID != null
                && updateIdleWander(ownerUUID, idleWander, idleWanderRadius, idleReturnDistance, idleReturnDelayMs);
    }

    @Override
    public boolean updateCrystalPvp(UUID ownerUUID, boolean crystalPvp) {
        ITrainingBot bot = getLiveBot(resolveManagedOwner(ownerUUID));
        if (bot == null || bot.getBrainController() == null) {
            return false;
        }
        BotOptions options = bot.getBrainController().getBotOptions();
        if (options == null) {
            return false;
        }
        if (crystalPvp && !options.isExplosions()) {
            return false;
        }
        UUID managedOwner = resolveManagedOwner(ownerUUID);
        Optional<Boolean> proposed = proposedChange(
                managedOwner, BotSettingKey.CRYSTAL_PVP, options.isCrystalPvp(), crystalPvp, Boolean.class);
        if (proposed.isEmpty()) return false;
        crystalPvp = proposed.get();
        options.setCrystalPvp(crystalPvp);
        bot.getBotAI().getCPVPController().setEnabled(crystalPvp);
        bot.getBotAI()
                .getInventoryController()
                .setItem(
                        com.monkey.ultimatebot.bot.ai.controllers.inventory.BotInventoryController.CRYSTAL_SLOT,
                        crystalPvp
                                ? new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.END_CRYSTAL, 64)
                                : net.minecraft.world.item.ItemStack.EMPTY);
        return true;
    }

    @Override
    public boolean updateCrystalPvpByBotUUID(UUID botUUID, boolean crystalPvp) {
        UUID ownerUUID = botRegistry.getOwnerUUIDByBotUUID(botUUID);
        return ownerUUID != null && updateCrystalPvp(ownerUUID, crystalPvp);
    }

    @Override
    public boolean updateExplosions(UUID ownerUUID, boolean explosions) {
        ITrainingBot bot = getLiveBot(resolveManagedOwner(ownerUUID));
        if (bot == null || bot.getBrainController() == null) {
            return false;
        }
        BotOptions options = bot.getBrainController().getBotOptions();
        if (options == null) {
            return false;
        }

        UUID managedOwner = resolveManagedOwner(ownerUUID);
        Optional<Boolean> proposed = proposedChange(
                managedOwner, BotSettingKey.EXPLOSIONS, options.isExplosions(), explosions, Boolean.class);
        if (proposed.isEmpty()) return false;
        explosions = proposed.get();
        options.setExplosions(explosions);
        if (!explosions) {
            options.setCrystalPvp(false);
            bot.getBotAI().getCPVPController().setEnabled(false);
            bot.getBotAI().getRAPVPController().disable();
            clearExplosiveItems(bot);
        }
        return true;
    }

    @Override
    public boolean updateExplosionsByBotUUID(UUID botUUID, boolean explosions) {
        UUID ownerUUID = botRegistry.getOwnerUUIDByBotUUID(botUUID);
        return ownerUUID != null && updateExplosions(ownerUUID, explosions);
    }

    @Override
    public boolean updateExplosionBlockDamage(UUID ownerUUID, boolean explosionBlockDamage) {
        BotOptions options = getLiveOptions(resolveManagedOwner(ownerUUID));
        if (options == null) {
            return false;
        }
        UUID managedOwner = resolveManagedOwner(ownerUUID);
        Optional<Boolean> proposed = proposedChange(
                managedOwner,
                BotSettingKey.EXPLOSION_BLOCK_DAMAGE,
                options.isExplosionBlockDamage(),
                explosionBlockDamage,
                Boolean.class);
        if (proposed.isEmpty()) return false;
        options.setExplosionBlockDamage(proposed.get());
        return true;
    }

    @Override
    public boolean updateExplosionBlockDamageByBotUUID(UUID botUUID, boolean explosionBlockDamage) {
        UUID ownerUUID = botRegistry.getOwnerUUIDByBotUUID(botUUID);
        return ownerUUID != null && updateExplosionBlockDamage(ownerUUID, explosionBlockDamage);
    }

    @Override
    public boolean updateEnderPearls(UUID ownerUUID, boolean enderPearls) {
        ITrainingBot bot = getLiveBot(resolveManagedOwner(ownerUUID));
        if (bot == null || bot.getBrainController() == null) {
            return false;
        }
        BotOptions options = bot.getBrainController().getBotOptions();
        if (options == null) {
            return false;
        }
        UUID managedOwner = resolveManagedOwner(ownerUUID);
        Optional<Boolean> proposed = proposedChange(
                managedOwner, BotSettingKey.ENDER_PEARLS, options.isEnderPearls(), enderPearls, Boolean.class);
        if (proposed.isEmpty()) return false;
        options.setEnderPearls(proposed.get());
        bot.getBotAI().getEnderpearlController().setEnabled(proposed.get());
        return true;
    }

    @Override
    public boolean updateEnderPearlsByBotUUID(UUID botUUID, boolean enderPearls) {
        UUID ownerUUID = botRegistry.getOwnerUUIDByBotUUID(botUUID);
        return ownerUUID != null && updateEnderPearls(ownerUUID, enderPearls);
    }

    @Override
    public boolean updateHealing(UUID ownerUUID, boolean healing) {
        ITrainingBot bot = getLiveBot(resolveManagedOwner(ownerUUID));
        if (bot == null || bot.getBrainController() == null) {
            return false;
        }
        BotOptions options = bot.getBrainController().getBotOptions();
        if (options == null) {
            return false;
        }
        UUID managedOwner = resolveManagedOwner(ownerUUID);
        Optional<Boolean> proposed =
                proposedChange(managedOwner, BotSettingKey.HEALING, options.isHealing(), healing, Boolean.class);
        if (proposed.isEmpty()) return false;
        options.setHealing(proposed.get());
        if (!proposed.get()) {
            bot.getBotAI().getHealController().resetHealState();
        }
        return true;
    }

    @Override
    public boolean updateHealingByBotUUID(UUID botUUID, boolean healing) {
        UUID ownerUUID = botRegistry.getOwnerUUIDByBotUUID(botUUID);
        return ownerUUID != null && updateHealing(ownerUUID, healing);
    }

    @Override
    public boolean updateKillMessage(UUID ownerUUID, String killMessage) {
        UUID managedOwner = resolveManagedOwner(ownerUUID);
        BotOptions options = getLiveOptions(managedOwner);
        if (options == null || killMessage == null || killMessage.isBlank()) {
            return false;
        }
        Optional<KillMessageSettings> proposed = proposedChange(
                managedOwner,
                BotSettingKey.KILL_MESSAGE,
                new KillMessageSettings(options.isKillMessageEnabled(), options.getCustomKillMessage()),
                new KillMessageSettings(true, killMessage),
                KillMessageSettings.class);
        if (proposed.isEmpty()) {
            return false;
        }
        options.setKillMessageEnabled(proposed.get().enabled());
        options.setCustomKillMessage(proposed.get().message());
        return true;
    }

    @Override
    public boolean updateKillMessageByBotUUID(UUID botUUID, String killMessage) {
        UUID ownerUUID = resolveOwnerByBotUUID(botUUID);
        return ownerUUID != null && updateKillMessage(ownerUUID, killMessage);
    }

    @Override
    public boolean disableKillMessage(UUID ownerUUID) {
        UUID managedOwner = resolveManagedOwner(ownerUUID);
        BotOptions options = getLiveOptions(managedOwner);
        if (options == null) {
            return false;
        }
        Optional<KillMessageSettings> proposed = proposedChange(
                managedOwner,
                BotSettingKey.KILL_MESSAGE,
                new KillMessageSettings(options.isKillMessageEnabled(), options.getCustomKillMessage()),
                KillMessageSettings.disabled(),
                KillMessageSettings.class);
        if (proposed.isEmpty()) {
            return false;
        }
        options.setKillMessageEnabled(proposed.get().enabled());
        options.setCustomKillMessage(proposed.get().message());
        return true;
    }

    @Override
    public boolean disableKillMessageByBotUUID(UUID botUUID) {
        UUID ownerUUID = resolveOwnerByBotUUID(botUUID);
        return ownerUUID != null && disableKillMessage(ownerUUID);
    }

    @Override
    public boolean remove(UUID ownerUUID) {
        if (ownerUUID == null) {
            return false;
        }

        UUID managedOwner = resolveManagedOwner(ownerUUID);
        if (!botManager.isBotSpawned(managedOwner)) {
            return false;
        }

        ITrainingBot bot = botManager.getBotSafe(managedOwner);
        BotOptions options = bot != null && bot.getBrainController() != null
                ? bot.getBrainController().getBotOptions()
                : null;

        if (!botManager.despawnByOwnerUUID(managedOwner)) {
            return false;
        }
        removeCachedOptions(managedOwner, options);
        return true;
    }

    @Override
    public boolean removeByBotUUID(UUID botUUID) {
        UUID ownerUUID = botRegistry.getOwnerUUIDByBotUUID(botUUID);
        return ownerUUID != null && remove(ownerUUID);
    }

    @Override
    public int removeBySource(BotSource source) {
        if (source == null) {
            return 0;
        }

        int removed = 0;
        Map<UUID, ITrainingBot> snapshot = botRegistry.getAllBots();
        for (Map.Entry<UUID, ITrainingBot> entry : snapshot.entrySet()) {
            ITrainingBot bot = entry.getValue();
            if (bot == null || bot.getBrainController() == null) {
                continue;
            }

            BotOptions options = bot.getBrainController().getBotOptions();
            if (options == null) {
                continue;
            }

            BotSource currentSource = options.getCreationSource().toCommon();
            if (currentSource == source && remove(entry.getKey())) {
                removed++;
            }
        }
        return removed;
    }

    @Override
    public int removeAll() {
        int activeBots = getActiveBotCount();
        despawnAll();
        return activeBots;
    }

    @Override
    public void despawn(UUID ownerUUID) {
        remove(ownerUUID);
    }

    @Override
    public void despawnAll() {
        botManager.despawnAll();
        playerOptions.clear();
    }

    @Override
    public int getActiveBotCount() {
        return botRegistry.getAllBots().size();
    }

    private @Nullable ITrainingBot getLiveBot(@Nullable UUID ownerUUID) {
        if (ownerUUID == null) {
            return null;
        }
        return botManager.getBotSafe(ownerUUID);
    }

    private CombatModeDefinition combatModeDefinition(CombatMode combatMode) {
        var configuration = plugin.getCombatProfileCatalog().configuration(combatMode);
        return new CombatModeDefinition(
                combatMode,
                combatMode.displayName(),
                configuration.enabled(),
                configuration.iconMaterial(),
                combatMode.capabilities(),
                configuration.profiles());
    }

    private @Nullable UUID resolveOwnerByBotUUID(@Nullable UUID botUUID) {
        return botRegistry.getOwnerUUIDByBotUUID(botUUID);
    }

    private <T> Optional<T> proposedChange(
            UUID ownerUUID, BotSettingKey key, T oldValue, T newValue, Class<T> valueType) {
        return BotSettingEvents.propose(
                plugin,
                ownerUUID,
                BotEventSourceContext.currentOr(BotEventSource.API),
                key,
                oldValue,
                newValue,
                valueType);
    }

    private @Nullable BotOptions getLiveOptions(@Nullable UUID ownerUUID) {
        ITrainingBot bot = getLiveBot(ownerUUID);
        if (bot == null || bot.getBrainController() == null) {
            return null;
        }
        return bot.getBrainController().getBotOptions();
    }

    private static com.monkey.ultimatebot.utils.armor.ArmorTier currentArmorTier(BotOptions options) {
        org.bukkit.inventory.ItemStack chestplate = options.getArmor().get(org.bukkit.inventory.EquipmentSlot.CHEST);
        com.monkey.ultimatebot.utils.armor.ArmorTier current = chestplate == null
                ? null
                : com.monkey.ultimatebot.utils.armor.ArmorTier.fromMaterial(
                        chestplate.getType(), org.bukkit.inventory.EquipmentSlot.CHEST);
        return current == null ? options.getMinArmorTier() : current;
    }

    private UUID resolveManagedOwner(UUID ownerUUID) {
        if (botManager.isBotSpawned(ownerUUID)) {
            return ownerUUID;
        }
        UUID teamPrimaryOwner = botManager.findTeamAllyPrimaryOwner(ownerUUID);
        return teamPrimaryOwner == null ? ownerUUID : teamPrimaryOwner;
    }

    private void cacheOptions(BotOptions options, UUID ownerUUID, Set<UUID> teamOwners) {
        playerOptions.put(ownerUUID, options);
        if (options.getBotType() == BotType.TEAM_ALLY) {
            for (UUID teamOwner : teamOwners) {
                playerOptions.put(teamOwner, options);
            }
        }
    }

    private UUID resolveManagedOwnerUUID(BotType type, @Nullable UUID requestedOwnerUUID, UUID contextOwnerUUID) {
        if (type == BotType.EVENT && requestedOwnerUUID == null) {
            return UUID.randomUUID();
        }
        return contextOwnerUUID;
    }

    private void removeCachedOptions(UUID ownerUUID, @Nullable BotOptions options) {
        playerOptions.remove(ownerUUID);
        if (options != null && options.getBotType() == BotType.TEAM_ALLY) {
            for (UUID teamOwner : options.getTeamOwnerUUIDs()) {
                playerOptions.remove(teamOwner);
            }
        }
    }

    private @Nullable UUID resolvePrimaryOwnerUUID(
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

    private @Nullable UUID resolveOnlineUUID(@Nullable UUID uuid) {
        if (uuid == null) {
            return null;
        }

        Player player = Bukkit.getPlayer(uuid);
        return player != null && player.isOnline() ? uuid : null;
    }

    private void clearExplosiveItems(ITrainingBot bot) {
        bot.getBotAI()
                .getInventoryController()
                .setItem(
                        com.monkey.ultimatebot.bot.ai.controllers.inventory.BotInventoryController.CRYSTAL_SLOT,
                        net.minecraft.world.item.ItemStack.EMPTY);
        bot.getBotAI()
                .getInventoryController()
                .setItem(
                        com.monkey.ultimatebot.bot.ai.controllers.inventory.BotInventoryController.ANCHOR_SLOT,
                        net.minecraft.world.item.ItemStack.EMPTY);
        bot.getBotAI()
                .getInventoryController()
                .setItem(
                        com.monkey.ultimatebot.bot.ai.controllers.inventory.BotInventoryController.GLOW_SLOT,
                        net.minecraft.world.item.ItemStack.EMPTY);
    }

    private @Nullable BotType getOwnerBusyType(UUID ownerUUID, @Nullable UUID allowedTeamPrimaryOwner) {
        if (ownerUUID == null) {
            return null;
        }

        if (botManager.isBotSpawned(ownerUUID)) {
            ITrainingBot bot = botManager.getBotSafe(ownerUUID);
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

        if (botManager.hasActiveTeamAlly(ownerUUID)) {
            UUID teamPrimaryOwner = botManager.findTeamAllyPrimaryOwner(ownerUUID);
            if (allowedTeamPrimaryOwner != null && allowedTeamPrimaryOwner.equals(teamPrimaryOwner)) {
                return null;
            }
            return BotType.TEAM_ALLY;
        }

        return null;
    }

    private BotSettings defaultSettings() {
        return BotSettings.builder()
                .setBotNameTemplate(plugin.getConfig().getString("bot.name", "CrystalBot"))
                .setBotSkinOwner()
                .follow(false)
                .setChangeableFollow(true)
                .combat(false)
                .setChangeableCombat(true)
                .blastProtection(0, 0, 0, 0)
                .setChangeableBlast(true)
                .armorValue(BotArmorTier.LEATHER, BotArmorTier.NETHERITE)
                .armor(BotArmorTier.NETHERITE)
                .setChangeableArmor(true)
                .totemValue(-1, Integer.MAX_VALUE)
                .totemCount(-1)
                .setChangeableTotem(true)
                .difficultyValue(DifficultyTier.EASY, DifficultyTier.GOD)
                .difficulty(DifficultyTier.EASY)
                .setChangeableDifficulty(true)
                .healing(plugin.getConfig().getBoolean("bot.combat.healing", true))
                .build();
    }

    private @Nullable String validateSkinForMode(BotType botType, @Nullable BotSettings settings) {
        if (settings == null || settings.botSkin() == null || settings.botSkin().source() == null) {
            return null;
        }

        BotSkin skin = settings.botSkin();
        BotSkinSource source = skin.source();

        if (source == BotSkinSource.FIRST_TEAM_OWNER && botType != BotType.TEAM_ALLY) {
            return "BotSkin.firstTeamOwner() is valid only for TEAM_ALLY mode.";
        }

        if (source == BotSkinSource.PLAYER_REFERENCE) {
            String reference = skin.playerReference();
            if (reference == null
                    || reference.isBlank()
                    || parsePlayerReference(reference).isEmpty()) {
                return "BotSkin.player(\"...\") requires an online valid player reference.";
            }
        }

        return null;
    }

    private BotOperationResult spawnFailure(@Nullable String reason) {
        return BotOperationResult.failure(Objects.requireNonNullElse(reason, "Bot operation failed."));
    }

    private static BotType toCoreType(BotMode mode) {
        return BotType.fromCommon(mode);
    }
}
