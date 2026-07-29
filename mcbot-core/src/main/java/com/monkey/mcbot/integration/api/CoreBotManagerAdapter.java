package com.monkey.mcbot.integration.api;

import com.monkey.mcbot.MinecraftBot;
import com.monkey.mcbot.api.managers.IBotManager;
import com.monkey.mcbot.api.model.*;
import com.monkey.mcbot.bot.*;
import com.monkey.mcbot.bot.ai.ITrainingBot;
import com.monkey.mcbot.utils.armor.PlayerOptions;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class CoreBotManagerAdapter implements IBotManager {

    private final MinecraftBot plugin;
    private final BotManager botManager;
    private final BotRegistry botRegistry;
    private final PlayerOptions playerOptions;
    private final ApiBotOptionsFactory optionsFactory;

    public CoreBotManagerAdapter(MinecraftBot plugin, BotManager botManager, BotRegistry botRegistry, PlayerOptions playerOptions) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.botManager = Objects.requireNonNull(botManager, "botManager");
        this.botRegistry = Objects.requireNonNull(botRegistry, "botRegistry");
        this.playerOptions = Objects.requireNonNull(playerOptions, "playerOptions");
        this.optionsFactory = new ApiBotOptionsFactory(plugin);
    }

    @Override
    public boolean isBotSpawned(UUID ownerUUID) {
        return ownerUUID != null
                && (botManager.isBotSpawned(ownerUUID) || botManager.hasActiveTeamAlly(ownerUUID));
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
            return spawnFailure(botType == BotType.EVENT
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

        BotOptions options = optionsFactory.create(
                botType, primaryOwnerUUID, targetUUID, targetUUIDs, teamOwners, settings
        );
        botManager.spawn(
                ownerPlayer,
                targetPlayer,
                options.getArmor(),
                options.getBlast(),
                options.isFollow(),
                options.getTotems(),
                options
        );

        cacheOptions(options, primaryOwnerUUID, teamOwners);

        BotSnapshot snapshot = getBot(primaryOwnerUUID).orElse(null);
        return BotOperationResult.success("Bot spawned successfully.", snapshot);
    }

    @Override
    public BotOperationResult spawnByReferences(BotMode mode,
                                                String ownerReference,
                                                Collection<String> targetReferences,
                                                Collection<String> teamOwnerReferences,
                                                BotSettings settings) {
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
    public Optional<UUID> parsePlayerReference(String playerReference) {
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

        options.setTotems(totemCount);
        botManager.updateTotem(managedOwner, totemCount);
        return true;
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

        options.setFollow(follow);
        botManager.updateFollow(managedOwner, follow);
        return true;
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

        options.setCombat(combat);
        botManager.updateCombat(managedOwner, combat);
        if (combat) {
            botManager.switchBotToSword(managedOwner);
        }
        return true;
    }

    @Override
    public boolean updateBlastProtection(UUID ownerUUID, boolean blastProtection) {
        UUID managedOwner = resolveManagedOwner(ownerUUID);
        ITrainingBot bot = getLiveBot(managedOwner);
        if (bot == null || bot.getBrainController() == null) {
            return false;
        }

        BotOptions options = bot.getBrainController().getBotOptions();
        if (options == null) {
            return false;
        }

        if (!options.isChangeableBlast()) {
            return false;
        }

        options.setBlastProtection(blastProtection);
        botManager.updateArmor(managedOwner, options.getArmor(), options.getBlast());
        return true;
    }

    @Override
    public boolean updateRank(UUID ownerUUID, BotRank rank) {
        if (rank == null) {
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

        if (!options.isChangeableRank()) {
            return false;
        }

        com.monkey.mcbot.bot.ai.rank.BotRank coreRank = ApiBotOptionsFactory.toCoreRank(rank);
        if (!options.isRankAllowed(coreRank)) {
            return false;
        }
        options.setRank(coreRank);
        botManager.setBotRank(managedOwner, coreRank);
        return true;
    }

    @Override
    public boolean updateArmor(UUID ownerUUID,
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
    public boolean updateEquipmentSlot(UUID ownerUUID, int slot, org.bukkit.inventory.ItemStack item) {
        UUID managedOwner = resolveManagedOwner(ownerUUID);
        BotOptions options = getLiveOptions(managedOwner);
        if (options == null || slot < 0 || slot > 40) {
            return false;
        }
        if (item == null) {
            options.getEquipmentContents().remove(slot);
            botManager.updateBotInventorySlot(managedOwner, slot, new org.bukkit.inventory.ItemStack(org.bukkit.Material.AIR));
            return true;
        }
        options.getEquipmentContents().put(slot, item.clone());
        botManager.updateBotInventorySlot(managedOwner, slot, item);
        return true;
    }

    @Override
    public boolean updateAutoTarget(UUID ownerUUID, boolean autoTarget, double range) {
        BotOptions options = getLiveOptions(resolveManagedOwner(ownerUUID));
        if (options == null || range <= 0.0D) {
            return false;
        }
        options.setAutoTarget(autoTarget);
        options.setAutoTargetRange(range);
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
        options.setAttackBots(attackBots);
        return true;
    }

    @Override
    public boolean updateAttackBotsByBotUUID(UUID botUUID, boolean attackBots) {
        UUID ownerUUID = botRegistry.getOwnerUUIDByBotUUID(botUUID);
        return ownerUUID != null && updateAttackBots(ownerUUID, attackBots);
    }

    @Override
    public boolean updateTargetMode(UUID ownerUUID, com.monkey.mcbot.api.model.BotTargetMode targetMode) {
        BotOptions options = getLiveOptions(resolveManagedOwner(ownerUUID));
        if (options == null || targetMode == null) {
            return false;
        }
        options.setTargetMode(targetMode);
        return true;
    }

    @Override
    public boolean updateTargetModeByBotUUID(UUID botUUID, com.monkey.mcbot.api.model.BotTargetMode targetMode) {
        UUID ownerUUID = botRegistry.getOwnerUUIDByBotUUID(botUUID);
        return ownerUUID != null && updateTargetMode(ownerUUID, targetMode);
    }

    @Override
    public boolean updateWorldGuardPvpRespect(UUID ownerUUID, boolean respectWorldGuardPvp) {
        BotOptions options = getLiveOptions(resolveManagedOwner(ownerUUID));
        if (options == null) {
            return false;
        }
        options.setRespectWorldGuardPvp(respectWorldGuardPvp);
        return true;
    }

    @Override
    public boolean updateStayAfterOwnerDeath(UUID ownerUUID, boolean stayAfterOwnerDeath) {
        BotOptions options = getLiveOptions(resolveManagedOwner(ownerUUID));
        if (options == null) {
            return false;
        }
        options.setStayAfterOwnerDeath(stayAfterOwnerDeath);
        return true;
    }

    @Override
    public boolean updateIdleWander(UUID ownerUUID,
                                    boolean idleWander,
                                    double idleWanderRadius,
                                    double idleReturnDistance,
                                    long idleReturnDelayMs) {
        BotOptions options = getLiveOptions(resolveManagedOwner(ownerUUID));
        if (options == null || idleWanderRadius <= 0.0D || idleReturnDistance <= 0.0D || idleReturnDelayMs < 0L) {
            return false;
        }
        options.setIdleWander(idleWander);
        options.setIdleWanderRadius(idleWanderRadius);
        options.setIdleReturnDistance(idleReturnDistance);
        options.setIdleReturnDelayMs(idleReturnDelayMs);
        return true;
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
        options.setCrystalPvp(crystalPvp);
        bot.getBotAI().getCPVPController().setEnabled(crystalPvp);
        bot.getBotAI().getInventoryController().setItem(
                com.monkey.mcbot.bot.ai.controllers.inventory.BotInventoryController.CRYSTAL_SLOT,
                crystalPvp
                        ? new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.END_CRYSTAL, 64)
                        : net.minecraft.world.item.ItemStack.EMPTY
        );
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
    public boolean updateEnderPearls(UUID ownerUUID, boolean enderPearls) {
        ITrainingBot bot = getLiveBot(resolveManagedOwner(ownerUUID));
        if (bot == null || bot.getBrainController() == null) {
            return false;
        }
        BotOptions options = bot.getBrainController().getBotOptions();
        if (options == null) {
            return false;
        }
        options.setEnderPearls(enderPearls);
        bot.getBotAI().getEnderpearlController().setEnabled(enderPearls);
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
        options.setHealing(healing);
        if (!healing) {
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
        BotOptions options = getLiveOptions(resolveManagedOwner(ownerUUID));
        if (options == null) {
            return false;
        }
        options.setKillMessageEnabled(true);
        options.setCustomKillMessage(killMessage);
        return true;
    }

    @Override
    public boolean disableKillMessage(UUID ownerUUID) {
        BotOptions options = getLiveOptions(resolveManagedOwner(ownerUUID));
        if (options == null) {
            return false;
        }
        options.setKillMessageEnabled(false);
        options.setCustomKillMessage(null);
        return true;
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

        botManager.despawnByOwnerUUID(managedOwner);
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

            BotSource currentSource = options.getCreationSource() == BotCreationSource.API
                    ? BotSource.API
                    : BotSource.CORE;
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

    private ITrainingBot getLiveBot(UUID ownerUUID) {
        if (ownerUUID == null) {
            return null;
        }
        return botManager.getBotSafe(ownerUUID);
    }

    private BotOptions getLiveOptions(UUID ownerUUID) {
        ITrainingBot bot = getLiveBot(ownerUUID);
        if (bot == null || bot.getBrainController() == null) {
            return null;
        }
        return bot.getBrainController().getBotOptions();
    }

    private UUID resolveManagedOwner(UUID ownerUUID) {
        if (ownerUUID == null) {
            return null;
        }
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

    private UUID resolvePrimaryOwnerUUID(BotType type,
                                         @Nullable UUID requestedOwnerUUID,
                                         Set<UUID> teamOwners,
                                         Set<UUID> targetUUIDs) {
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

    private UUID resolveOnlineUUID(@Nullable UUID uuid) {
        if (uuid == null) {
            return null;
        }

        Player player = Bukkit.getPlayer(uuid);
        return player != null && player.isOnline() ? uuid : null;
    }

    private boolean isEventBotActive() {
        for (ITrainingBot bot : botRegistry.getAllBots().values()) {
            if (bot == null || bot.getBrainController() == null) {
                continue;
            }
            BotOptions options = bot.getBrainController().getBotOptions();
            if (options != null && options.getBotType() == BotType.EVENT) {
                return true;
            }
        }
        return false;
    }

    private void clearExplosiveItems(ITrainingBot bot) {
        bot.getBotAI().getInventoryController().setItem(
                com.monkey.mcbot.bot.ai.controllers.inventory.BotInventoryController.CRYSTAL_SLOT,
                net.minecraft.world.item.ItemStack.EMPTY
        );
        bot.getBotAI().getInventoryController().setItem(
                com.monkey.mcbot.bot.ai.controllers.inventory.BotInventoryController.ANCHOR_SLOT,
                net.minecraft.world.item.ItemStack.EMPTY
        );
        bot.getBotAI().getInventoryController().setItem(
                com.monkey.mcbot.bot.ai.controllers.inventory.BotInventoryController.GLOW_SLOT,
                net.minecraft.world.item.ItemStack.EMPTY
        );
    }

    private @Nullable BotType getOwnerBusyType(UUID ownerUUID, @Nullable UUID allowedTeamPrimaryOwner) {
        if (ownerUUID == null) {
            return null;
        }

        if (botManager.isBotSpawned(ownerUUID)) {
            ITrainingBot bot = botManager.getBotSafe(ownerUUID);
            if (bot != null && bot.getBrainController() != null && bot.getBrainController().getBotOptions() != null) {
                BotType type = bot.getBrainController().getBotOptions().getBotType();
                if (type == BotType.TEAM_ALLY && allowedTeamPrimaryOwner != null && ownerUUID.equals(allowedTeamPrimaryOwner)) {
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
                .armorValue(BotArmorType.LEATHER, BotArmorType.NETHERITE)
                .armor(BotArmorType.NETHERITE)
                .setChangeableArmor(true)
                .totemValue(-1, Integer.MAX_VALUE)
                .totemCount(-1)
                .setChangeableTotem(true)
                .rankValue(BotRank.EASY, BotRank.GOD)
                .rank(BotRank.EASY)
                .setChangeableRank(true)
                .healing(plugin.getConfig().getBoolean("bot.combat.healing", true))
                .build();
    }

    private String validateSkinForMode(BotType botType, BotSettings settings) {
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
            if (reference == null || reference.isBlank() || parsePlayerReference(reference).isEmpty()) {
                return "BotSkin.player(\"...\") requires an online valid player reference.";
            }
        }

        return null;
    }

    private BotOperationResult spawnFailure(String reason) {
        return BotOperationResult.failure(reason);
    }

    private static BotType toCoreType(BotMode mode) {
        return switch (mode) {
            case SINGLE -> BotType.SINGLE;
            case EVENT -> BotType.EVENT;
            case ALLY -> BotType.ALLY;
            case TEAM_ALLY -> BotType.TEAM_ALLY;
        };
    }

}
