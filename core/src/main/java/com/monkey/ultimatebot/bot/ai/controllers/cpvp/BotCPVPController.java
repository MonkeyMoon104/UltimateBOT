package com.monkey.ultimatebot.bot.ai.controllers.cpvp;

import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.bot.ai.controllers.cpvp.helper.crystal.CrystalAttacker;
import com.monkey.ultimatebot.bot.ai.controllers.cpvp.helper.crystal.CrystalManager;
import com.monkey.ultimatebot.bot.ai.controllers.cpvp.helper.crystal.CrystalPlacer;
import com.monkey.ultimatebot.bot.ai.controllers.cpvp.helper.crystal.CrystalPositionEvaluator;
import com.monkey.ultimatebot.bot.ai.controllers.cpvp.helper.obsidian.ObsidianPlacer;
import com.monkey.ultimatebot.bot.ai.controllers.cpvp.helper.obsidian.ObsidianPositionFinder;
import com.monkey.ultimatebot.bot.ai.controllers.cpvp.helper.obsidian.ObsidianScanner;
import com.monkey.ultimatebot.bot.ai.controllers.inventory.BotInventoryController;
import com.monkey.ultimatebot.bot.ai.difficulty.DifficultyLevel;
import com.monkey.ultimatebot.bot.ai.difficulty.DifficultyProfileFactory;
import com.monkey.ultimatebot.bot.ai.difficulty.configs.CPVPConfig;
import com.monkey.ultimatebot.common.model.PlatformCapability;
import com.monkey.ultimatebot.nms.NMSBridgeManager;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockVector;
import org.jspecify.annotations.Nullable;

@SuppressWarnings("NullAway")
public final class BotCPVPController {

    private final ITrainingBot bot;
    private final Player bukkitBot;
    private final World world;
    private final BotInventoryController inventoryController;
    private final ObsidianPlacer obsidianPlacer;
    private final CrystalPlacer crystalPlacer;
    private final CrystalAttacker crystalAttacker;
    private final ObsidianPositionFinder obsidianPositionFinder;
    private final CrystalPositionEvaluator crystalPositionEvaluator;
    private final ObsidianScanner obsidianScanner;
    private final CrystalManager crystalManager;
    private final CrystalCombatPolicy combatPolicy;
    private final CrystalTargetPlanner targetPlanner;
    private DifficultyLevel difficulty = DifficultyLevel.NORMAL;
    private CPVPConfig config = DifficultyProfileFactory.buildCPVPConfig(difficulty);

    private int obsidianPlaceCooldown = 0;
    private int crystalPlaceCooldown = 0;
    private int attackCooldown = 0;

    private final Map<BlockVector, Long> recentPlacements = new ConcurrentHashMap<>();
    private final Set<EnderCrystal> myPlacedCrystals = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final Map<BlockVector, Long> obsidianCache = new ConcurrentHashMap<>();
    private final Map<BlockVector, Integer> crystalCountAtPosition = new ConcurrentHashMap<>();
    private final Deque<BlockVector> queuedCrystalPlacements = new ArrayDeque<>();
    private final Map<BlockVector, Long> crystalRecentUsage = new ConcurrentHashMap<>();

    private long lastFullScan = 0;
    private boolean isPreparingObsidian = false;
    private @Nullable BlockVector pendingObsidianPos;
    private int obsidianPreparationTicks = 0;
    private boolean isPreparingCrystal = false;
    private @Nullable BlockVector pendingCrystalPos;
    private int crystalPreparationTicks = 0;
    private boolean isPreparingAttack = false;
    private @Nullable EnderCrystal pendingAttackCrystal;
    private int attackPreparationTicks = 0;
    private boolean enabled = true;

    public BotCPVPController(ITrainingBot bot, BotInventoryController inventoryController) {
        this.bot = bot;
        this.bukkitBot = bot.asBukkitPlayer();
        this.world = bot.getWorld();
        this.inventoryController = inventoryController;
        this.obsidianPlacer = new ObsidianPlacer(bot, inventoryController);
        this.crystalPlacer = new CrystalPlacer(bot, inventoryController);
        this.crystalAttacker = new CrystalAttacker(bot);
        this.obsidianPositionFinder = new ObsidianPositionFinder(bot);
        this.crystalPositionEvaluator = new CrystalPositionEvaluator(bukkitBot);
        this.obsidianScanner = new ObsidianScanner(world);
        this.crystalManager = new CrystalManager();
        this.combatPolicy = new CrystalCombatPolicy();
        this.targetPlanner = new CrystalTargetPlanner(bukkitBot, crystalManager, crystalPositionEvaluator);
        setDifficulty(DifficultyLevel.NORMAL);
    }

    public void tick(Player target) {
        if (!enabled) {
            resetPendingActions();
            return;
        }
        if (obsidianPlaceCooldown > 0) obsidianPlaceCooldown--;
        if (crystalPlaceCooldown > 0) crystalPlaceCooldown--;
        if (attackCooldown > 0) attackCooldown--;
        myPlacedCrystals.removeIf(crystal -> crystal.isDead() || !crystal.isValid());
        crystalManager.cleanupCrystalCounts(crystalCountAtPosition, world);
        crystalManager.cleanupObsidianCache(obsidianCache);
        queuedCrystalPlacements.removeIf(pos -> !isStillValidCrystalPlacement(pos));
        crystalRecentUsage.entrySet().removeIf(entry -> currentTime() - entry.getValue() > combatPolicy.positionReuseDelayMs());
        crystalAttacker.updateBotPosition();

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastFullScan > config.getFullScanIntervalMs()) {
            obsidianScanner.scanForExistingObsidian(target, obsidianCache);
            lastFullScan = currentTime;
        }
        targetPlanner.pruneObsidianCache(target, obsidianCache, combatPolicy);
        handleObsidianPreparation();
        handleCrystalPreparation();
        handleAttackPreparation();

        for (int i = 0; i < combatPolicy.actionCyclesPerTick(); i++) {
            if (isDoingCrystalAction()) break;
            tryAttackOptimalCrystal(target);
            if (shouldForceNewObsidianPlacement(target) || !isNearBedrockForCrystalPriority()) {
                tryPlaceObsidianForCrystal(target);
            }
            tryPlaceOptimalCrystals(target);
            tryAttackOptimalCrystal(target);
        }
    }

    public boolean tryPlaceObsidianForCrystal(Player target) {
        if (!enabled || isDoingCrystalAction() || !canPlaceObsidian()) return false;
        double distance = bot.distanceTo(target);
        if (distance < config.getMinCrystalDistance() || distance > config.getMaxCrystalDistance()) return false;
        if (!hasObsidian()) return false;
        List<BlockVector> bestPositions = obsidianPositionFinder.findBestObsidianPositions(
                target, Math.max(1, config.getMaxPositionsToCheck()), recentPlacements, config.getPositionCooldownMs(), config.getMaxCrystalDistance());
        if (bestPositions.isEmpty()) return false;
        if (!inventoryController.isHoldingObsidian()) {
            inventoryController.switchToObs();
            return false;
        }
        startObsidianPreparation(bestPositions.get(0));
        return true;
    }

    private void startObsidianPreparation(BlockVector pos) {
        if (config.getObsidianPreparationTime() <= 0) {
            executeObsidianPlacementNow(pos);
            return;
        }
        isPreparingObsidian = true;
        pendingObsidianPos = pos;
        obsidianPreparationTicks = 0;
    }

    private void handleObsidianPreparation() {
        if (!isPreparingObsidian || pendingObsidianPos == null) return;
        if (++obsidianPreparationTicks >= config.getObsidianPreparationTime()) {
            executeObsidianPlacementNow(pendingObsidianPos);
            isPreparingObsidian = false;
            pendingObsidianPos = null;
            obsidianPreparationTicks = 0;
        }
    }

    private void tryAttackOptimalCrystal(Player target) {
        if (isDoingCrystalAction() || attackCooldown > 0) return;
        List<EnderCrystal> candidates = targetPlanner.collectAttackCandidates(target, myPlacedCrystals, config);
        for (EnderCrystal crystal : candidates) {
            if (attackCooldown > 0) break;
            double score = crystalPositionEvaluator.evaluateCrystalForAttack(
                    crystal, target, myPlacedCrystals, config.getCrystalAttackRange(), config.getMinCrystalDistance(), config.getOptimalDamageRange());
            if (score < config.getMinAttackScore()) continue;
            if (config.getAttackPreparationTime() <= 0) {
                executeCrystalAttackNow(crystal);
            } else {
                isPreparingAttack = true;
                pendingAttackCrystal = crystal;
                attackPreparationTicks = 0;
            }
            break;
        }
    }

    private void handleAttackPreparation() {
        if (!isPreparingAttack || pendingAttackCrystal == null || pendingAttackCrystal.isDead() || !pendingAttackCrystal.isValid()) {
            isPreparingAttack = false;
            pendingAttackCrystal = null;
            attackPreparationTicks = 0;
            return;
        }
        if (++attackPreparationTicks >= config.getAttackPreparationTime()) {
            executeCrystalAttackNow(pendingAttackCrystal);
            isPreparingAttack = false;
            pendingAttackCrystal = null;
            attackPreparationTicks = 0;
        }
    }

    public void tryPlaceOptimalCrystals(Player target) {
        if (isDoingCrystalAction() || !canPlaceCrystal()) return;
        List<BlockVector> validObsidianPositions = crystalManager.getValidCrystalPositions(
                obsidianCache, target, bukkitBot, world, config.getMaxCrystalDistance());
        if (validObsidianPositions.isEmpty()) {
            tryPlaceObsidianForCrystal(target);
            return;
        }
        if (queuedCrystalPlacements.isEmpty()) {
            buildCrystalPlacementQueue(target, validObsidianPositions);
        }
        if (queuedCrystalPlacements.isEmpty()) return;
        BlockVector nextCrystalPos = queuedCrystalPlacements.pollFirst();
        if (nextCrystalPos != null) startCrystalPreparation(nextCrystalPos);
    }

    private void startCrystalPreparation(BlockVector pos) {
        if (config.getCrystalPreparationTime() <= 0) {
            executeCrystalPlacementNow(pos);
            return;
        }
        isPreparingCrystal = true;
        pendingCrystalPos = pos;
        crystalPreparationTicks = 0;
    }

    private void handleCrystalPreparation() {
        if (!isPreparingCrystal || pendingCrystalPos == null) return;
        if (++crystalPreparationTicks >= config.getCrystalPreparationTime()) {
            executeCrystalPlacementNow(pendingCrystalPos);
            isPreparingCrystal = false;
            pendingCrystalPos = null;
            crystalPreparationTicks = 0;
        }
    }

    public boolean canPlaceObsidian() {
        return enabled && obsidianPlaceCooldown <= 0 && hasObsidian() && bot.isAlive();
    }

    public boolean canPlaceCrystal() {
        return enabled && crystalPlaceCooldown <= 0 && inventoryController.hasItem(Material.END_CRYSTAL) && bot.isAlive();
    }

    public boolean hasObsidian() {
        return inventoryController.hasItem(Material.OBSIDIAN) && inventoryController.getItemCount(Material.OBSIDIAN) > 0;
    }

    public boolean shouldPlaceObsidian(Player target) {
        return bot.distanceTo(target) >= config.getMinCrystalDistance()
                && bot.distanceTo(target) <= config.getMaxCrystalDistance()
                && !obsidianPositionFinder.findBestObsidianPositions(target, 1, recentPlacements, config.getPositionCooldownMs(), config.getMaxCrystalDistance()).isEmpty();
    }

    public int getPlacedCrystalsCount() { return myPlacedCrystals.size(); }
    public int getCachedObsidianCount() { return obsidianCache.size(); }
    public int getTotalCrystalPlacements() { return crystalCountAtPosition.values().stream().mapToInt(Integer::intValue).sum(); }
    public boolean isDoingCrystalAction() { return isPreparingObsidian || isPreparingCrystal || isPreparingAttack; }
    public boolean isPreparingObsidianPlacement() { return isPreparingObsidian; }
    public boolean isPreparingCrystalPlacement() { return isPreparingCrystal; }
    public boolean isPreparingCrystalAttack() { return isPreparingAttack; }

    public Optional<BlockVector> getBestObsidianForPearl(Player target) {
        if (!enabled) return Optional.empty();
        List<BlockVector> bestPositions = obsidianPositionFinder.findBestObsidianPositions(
                target, 1, recentPlacements, config.getPositionCooldownMs(), config.getMaxCrystalDistance());
        return bestPositions.isEmpty() ? Optional.empty() : Optional.of(bestPositions.get(0));
    }

    private void buildCrystalPlacementQueue(Player target, List<BlockVector> validObsidianPositions) {
        long now = currentTime();
        validObsidianPositions.sort((p1, p2) -> Double.compare(
                crystalPositionEvaluator.calculateCrystalScore(p2, target, crystalCountAtPosition, config.getOptimalDamageRange(), config.getMinCrystalDistance()),
                crystalPositionEvaluator.calculateCrystalScore(p1, target, crystalCountAtPosition, config.getOptimalDamageRange(), config.getMinCrystalDistance())));
        for (BlockVector pos : validObsidianPositions) {
            if (!targetPlanner.isCoolingDown(pos, crystalRecentUsage, combatPolicy, now)) {
                queuedCrystalPlacements.addLast(pos);
                break;
            }
        }
    }

    private boolean isStillValidCrystalPlacement(BlockVector pos) {
        Material type = world.getBlockAt(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ()).getType();
        return type == Material.OBSIDIAN || type == Material.BEDROCK;
    }

    private void executeObsidianPlacementNow(BlockVector pos) {
        if (!obsidianPlacer.hasLineOfSight(pos)) return;
        if (obsidianPlacer.placeObsidianAt(pos)) {
            obsidianPlaceCooldown = config.getObsidianPlaceCooldownTicks();
            recentPlacements.put(pos, System.currentTimeMillis());
            obsidianCache.put(pos, System.currentTimeMillis());
        }
    }

    private void executeCrystalPlacementNow(BlockVector pos) {
        if (!crystalPlacer.hasLineOfSight(pos)) return;
        if (crystalPlacer.placeCrystal(pos)) {
            crystalPlaceCooldown = config.getCrystalPlaceCooldownTicks();
            crystalRecentUsage.put(pos, currentTime());
            crystalCountAtPosition.put(pos, crystalCountAtPosition.getOrDefault(pos, 0) + 1);
            EnderCrystal placedCrystal = crystalManager.findCrystalAt(new BlockVector(pos.getBlockX(), pos.getBlockY() + 1, pos.getBlockZ()), world);
            if (placedCrystal != null) myPlacedCrystals.add(placedCrystal);
        } else if (isStillValidCrystalPlacement(pos)) {
            crystalRecentUsage.put(pos, currentTime());
            queuedCrystalPlacements.addLast(pos);
        }
    }

    private boolean executeCrystalAttackNow(EnderCrystal crystal) {
        if (!crystalAttacker.canAttackCrystal(crystal, config.getCrystalAttackRange())) return false;
        if (crystalAttacker.attackCrystal(crystal)) {
            attackCooldown = config.getAttackCooldownTicks();
            myPlacedCrystals.remove(crystal);
            return true;
        }
        return false;
    }

    private boolean isNearBedrockForCrystalPriority() {
        int y = bot.getLocation().getBlockY();
        int bx = bot.getLocation().getBlockX();
        int bz = bot.getLocation().getBlockZ();
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (world.getBlockAt(bx + x, y, bz + z).getType() == Material.BEDROCK) return true;
            }
        }
        return false;
    }

    public void setDifficulty(DifficultyLevel difficulty) {
        this.difficulty = difficulty;
        this.config = DifficultyProfileFactory.buildCPVPConfig(difficulty);
        this.combatPolicy.setDifficulty(difficulty);
        this.obsidianScanner.setConfig(config);
        this.crystalManager.setConfig(config);
    }

    public DifficultyLevel getDifficulty() { return difficulty; }
    public CPVPConfig getConfig() { return config; }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled && NMSBridgeManager.get().supports(PlatformCapability.END_CRYSTAL);
        if (!this.enabled) clearRuntimeState();
    }

    public boolean isEnabled() { return enabled; }

    private void resetPendingActions() {
        isPreparingObsidian = false;
        pendingObsidianPos = null;
        obsidianPreparationTicks = 0;
        isPreparingCrystal = false;
        pendingCrystalPos = null;
        crystalPreparationTicks = 0;
        isPreparingAttack = false;
        pendingAttackCrystal = null;
        attackPreparationTicks = 0;
        queuedCrystalPlacements.clear();
    }

    private void clearRuntimeState() {
        resetPendingActions();
        myPlacedCrystals.removeIf(crystal -> {
            if (crystal.isValid()) crystal.remove();
            return true;
        });
        recentPlacements.clear();
        obsidianCache.clear();
        crystalCountAtPosition.clear();
        crystalRecentUsage.clear();
    }

    private boolean shouldForceNewObsidianPlacement(Player target) {
        return hasObsidian()
                && canPlaceObsidian()
                && targetPlanner.shouldForceObsidianPlacement(target, obsidianCache, crystalCountAtPosition, crystalRecentUsage, config, combatPolicy);
    }

    private long currentTime() {
        return System.currentTimeMillis();
    }
}
