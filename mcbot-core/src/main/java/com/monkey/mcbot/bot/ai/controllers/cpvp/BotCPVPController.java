package com.monkey.mcbot.bot.ai.controllers.cpvp;

import com.monkey.mcbot.bot.ai.controllers.cpvp.helper.crystal.CrystalAttacker;
import com.monkey.mcbot.bot.ai.controllers.cpvp.helper.crystal.CrystalManager;
import com.monkey.mcbot.bot.ai.controllers.cpvp.helper.crystal.CrystalPlacer;
import com.monkey.mcbot.bot.ai.controllers.cpvp.helper.crystal.CrystalPositionEvaluator;
import com.monkey.mcbot.bot.ai.controllers.cpvp.helper.obsidian.ObsidianPlacer;
import com.monkey.mcbot.bot.ai.controllers.cpvp.helper.obsidian.ObsidianPositionFinder;
import com.monkey.mcbot.bot.ai.controllers.cpvp.helper.obsidian.ObsidianScanner;
import com.monkey.mcbot.bot.ai.controllers.inventory.BotInventoryController;
import com.monkey.mcbot.bot.ai.rank.BotRank;
import com.monkey.mcbot.bot.ai.rank.RankCoordinator;
import com.monkey.mcbot.bot.ai.rank.configs.CPVPConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BotCPVPController {

    private final Player bot;
    private final Level level;
    private final BotInventoryController inventoryController;
    private final ObsidianPlacer obsidianPlacer;
    private final CrystalPlacer crystalPlacer;
    private final CrystalAttacker crystalAttacker;
    private final ObsidianPositionFinder obsidianPositionFinder;
    private final CrystalPositionEvaluator crystalPositionEvaluator;
    private final ObsidianScanner obsidianScanner;
    private final CrystalManager crystalManager;
    private BotRank rank;
    private CPVPConfig config;

    private int obsidianPlaceCooldown = 0;
    private int crystalPlaceCooldown = 0;
    private int attackCooldown = 0;

    private final Map<BlockPos, Long> recentPlacements = new ConcurrentHashMap<>();
    private final Set<EndCrystal> myPlacedCrystals = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final Map<BlockPos, Long> obsidianCache = new ConcurrentHashMap<>();
    private final Map<BlockPos, Integer> crystalCountAtPosition = new ConcurrentHashMap<>();
    private final Deque<BlockPos> queuedCrystalPlacements = new ArrayDeque<>();
    private final Map<BlockPos, Long> crystalRecentUsage = new ConcurrentHashMap<>();

    private long lastFullScan = 0;
    private final List<BlockPos> cachedValidPositions = new ArrayList<>();
    private long lastPositionCache = 0;

    private boolean isPreparingObsidian = false;
    private BlockPos pendingObsidianPos = null;
    private int obsidianPreparationTicks = 0;

    private boolean isPreparingCrystal = false;
    private BlockPos pendingCrystalPos = null;
    private int crystalPreparationTicks = 0;

    private boolean isPreparingAttack = false;
    private EndCrystal pendingAttackCrystal = null;
    private int attackPreparationTicks = 0;

    public BotCPVPController(Player bot, BotInventoryController inventoryController) {
        this.bot = bot;
        this.level = bot.level();
        this.inventoryController = inventoryController;

        this.obsidianPlacer = new ObsidianPlacer(bot, inventoryController, level);
        this.crystalPlacer = new CrystalPlacer(bot, inventoryController, level);
        this.crystalAttacker = new CrystalAttacker(bot, level);
        this.obsidianPositionFinder = new ObsidianPositionFinder(bot, level);
        this.crystalPositionEvaluator = new CrystalPositionEvaluator(bot, level);
        this.obsidianScanner = new ObsidianScanner(level);
        this.crystalManager = new CrystalManager();
    }

    public void tick(Player target) {
        if (obsidianPlaceCooldown > 0) obsidianPlaceCooldown--;
        if (crystalPlaceCooldown > 0) crystalPlaceCooldown--;
        if (attackCooldown > 0) attackCooldown--;

        myPlacedCrystals.removeIf(crystal -> !crystal.isAlive());
        crystalManager.cleanupCrystalCounts(crystalCountAtPosition, level);
        crystalManager.cleanupObsidianCache(obsidianCache);
        queuedCrystalPlacements.removeIf(pos -> !isStillValidCrystalPlacement(pos));
        crystalRecentUsage.entrySet().removeIf(entry ->
                currentTime() - entry.getValue() > getCrystalPositionReuseDelayMs());
        crystalAttacker.updateBotPosition();

        long currentTime = System.currentTimeMillis();

        if (currentTime - lastFullScan > config.getFullScanIntervalMs()) {
            obsidianScanner.scanForExistingObsidian(target, obsidianCache);
            lastFullScan = currentTime;
        }

        pruneObsidianCacheForTarget(target);

        handleObsidianPreparation();
        handleCrystalPreparation();
        handleAttackPreparation();

        int actionCycles = getCrystalActionCyclesPerTick();
        for (int i = 0; i < actionCycles; i++) {
            if (isPreparingObsidian || isPreparingCrystal || isPreparingAttack) {
                break;
            }

            tryAttackOptimalCrystal(target);

            if (shouldForceNewObsidianPlacement(target) || !isNearBedrockForCrystalPriority(target)) {
                tryPlaceObsidianForCrystal(target);
            }

            tryPlaceOptimalCrystals(target);
            tryAttackOptimalCrystal(target);
        }
    }

    public boolean tryPlaceObsidianForCrystal(Player target) {
        if (isPreparingObsidian || isPreparingCrystal || isPreparingAttack) return false;
        if (!canPlaceObsidian()) return false;

        double distance = bot.distanceTo(target);
        if (distance < config.getMinCrystalDistance() || distance > config.getMaxCrystalDistance()) return false;
        if (!hasObsidian()) return false;

        List<BlockPos> bestPositions = obsidianPositionFinder.findBestObsidianPositions(
                target,
                Math.max(1, config.getMaxPositionsToCheck()),
                recentPlacements,
                config.getPositionCooldownMs(),
                config.getMaxCrystalDistance()
        );
        if (bestPositions.isEmpty()) return false;

        if (!inventoryController.isHoldingObsidian()) {
            inventoryController.switchToObs();
            return false;
        }

        for (BlockPos pos : bestPositions) {
            startObsidianPreparation(pos);
            return true;
        }

        return false;
    }

    private void startObsidianPreparation(BlockPos pos) {
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

        obsidianPreparationTicks++;


        if (obsidianPreparationTicks >= config.getObsidianPreparationTime()) {
            if (!obsidianPlacer.hasLineOfSight(pendingObsidianPos)) {
                isPreparingObsidian = false;
                pendingObsidianPos = null;
                obsidianPreparationTicks = 0;
                return;
            }

            if (obsidianPlacer.placeObsidianAt(pendingObsidianPos)) {
                obsidianPlaceCooldown = config.getObsidianPlaceCooldownTicks();
                recentPlacements.put(pendingObsidianPos, System.currentTimeMillis());
                obsidianCache.put(pendingObsidianPos, System.currentTimeMillis());
            }

            isPreparingObsidian = false;
            pendingObsidianPos = null;
            obsidianPreparationTicks = 0;
        }
    }

    private void tryAttackOptimalCrystal(Player target) {
        if (isPreparingObsidian || isPreparingCrystal || isPreparingAttack) return;
        if (attackCooldown > 0) return;

        List<EndCrystal> candidates = collectCrystalAttackCandidates(target);
        if (candidates.isEmpty()) {
            return;
        }

        int maxAttacks = getCrystalAttackBurstAttempts();
        int performed = 0;

        for (EndCrystal crystal : candidates) {
            if (isPreparingObsidian || isPreparingCrystal || isPreparingAttack) {
                break;
            }
            if (attackCooldown > 0) {
                break;
            }

            double score = crystalPositionEvaluator.evaluateCrystalForAttack(
                    crystal,
                    target,
                    myPlacedCrystals,
                    config.getCrystalAttackRange(),
                    config.getMinCrystalDistance(),
                    config.getOptimalDamageRange()
            );
            if (score < config.getMinAttackScore()) {
                continue;
            }

            if (config.getAttackPreparationTime() <= 0) {
                if (executeCrystalAttackNow(crystal)) {
                    performed++;
                }
            } else {
                startAttackPreparation(crystal);
                performed++;
                break;
            }

            if (performed >= maxAttacks) {
                break;
            }
        }
    }

    private void startAttackPreparation(EndCrystal crystal) {
        if (config.getAttackPreparationTime() <= 0) {
            executeCrystalAttackNow(crystal);
            return;
        }
        isPreparingAttack = true;
        pendingAttackCrystal = crystal;
        attackPreparationTicks = 0;

    }

    private void handleAttackPreparation() {
        if (!isPreparingAttack || pendingAttackCrystal == null || !pendingAttackCrystal.isAlive()) {
            isPreparingAttack = false;
            pendingAttackCrystal = null;
            attackPreparationTicks = 0;
            return;
        }

        attackPreparationTicks++;


        if (attackPreparationTicks >= config.getAttackPreparationTime()) {
            if (crystalAttacker.canAttackCrystal(pendingAttackCrystal, config.getCrystalAttackRange())) {
                executeCrystalAttackNow(pendingAttackCrystal);
            }

            isPreparingAttack = false;
            pendingAttackCrystal = null;
            attackPreparationTicks = 0;
        }
    }

    public void tryPlaceOptimalCrystals(Player target) {
        if (isPreparingObsidian || isPreparingCrystal || isPreparingAttack) return;
        if (!canPlaceCrystal()) return;

        List<BlockPos> validObsidianPositions = crystalManager.getValidCrystalPositions(obsidianCache, target, bot, level, config.getMaxCrystalDistance());

        if (validObsidianPositions.isEmpty()) {
            tryPlaceObsidianForCrystal(target);
            return;
        }

        if (queuedCrystalPlacements.isEmpty()) {
            buildCrystalPlacementQueue(target, validObsidianPositions);
        }

        if (queuedCrystalPlacements.isEmpty()) {
            if (shouldForceNewObsidianPlacement(target)) {
                tryPlaceObsidianForCrystal(target);
            }
            return;
        }

        int maxPlacements = getCrystalPlacementBurstAttempts();
        for (int i = 0; i < maxPlacements; i++) {
            if (isPreparingObsidian || isPreparingCrystal || isPreparingAttack) {
                break;
            }
            if (!canPlaceCrystal() || queuedCrystalPlacements.isEmpty()) {
                break;
            }

            BlockPos nextCrystalPos = queuedCrystalPlacements.pollFirst();
            if (nextCrystalPos != null) {
                startCrystalPreparation(nextCrystalPos);
            }

            if (config.getCrystalPreparationTime() > 0) {
                break;
            }
        }
    }

    private void startCrystalPreparation(BlockPos pos) {
        if (config.getCrystalPreparationTime() <= 0) {
            executeCrystalPlacementNow(pos);
            return;
        }
        isPreparingCrystal = true;
        pendingCrystalPos = pos;
        crystalPreparationTicks = 0;

        net.minecraft.world.phys.Vec3 crystalPlacementPos = net.minecraft.world.phys.Vec3.atCenterOf(pos.above());
    }

    private void handleCrystalPreparation() {
        if (!isPreparingCrystal || pendingCrystalPos == null) return;

        crystalPreparationTicks++;

        net.minecraft.world.phys.Vec3 crystalPlacementPos = net.minecraft.world.phys.Vec3.atCenterOf(pendingCrystalPos.above());

        if (crystalPreparationTicks >= config.getCrystalPreparationTime()) {
            if (!crystalPlacer.hasLineOfSight(pendingCrystalPos)) {
                isPreparingCrystal = false;
                pendingCrystalPos = null;
                crystalPreparationTicks = 0;
                return;
            }

            if (crystalPlacer.placeCrystal(pendingCrystalPos)) {
                crystalPlaceCooldown = config.getCrystalPlaceCooldownTicks();
                crystalRecentUsage.put(pendingCrystalPos, currentTime());
                crystalCountAtPosition.put(
                        pendingCrystalPos,
                        crystalCountAtPosition.getOrDefault(pendingCrystalPos, 0) + 1
                );

                EndCrystal placedCrystal = crystalManager.findCrystalAt(pendingCrystalPos.above(), level);
                if (placedCrystal != null) {
                    myPlacedCrystals.add(placedCrystal);
                }
            } else if (isStillValidCrystalPlacement(pendingCrystalPos)) {
                crystalRecentUsage.put(pendingCrystalPos, currentTime());
                queuedCrystalPlacements.addLast(pendingCrystalPos);
            }

            isPreparingCrystal = false;
            pendingCrystalPos = null;
            crystalPreparationTicks = 0;
        }
    }

    public boolean canPlaceObsidian() {
        return obsidianPlaceCooldown <= 0 && hasObsidian() && bot.isAlive();
    }

    public boolean canPlaceCrystal() {
        return crystalPlaceCooldown <= 0 && inventoryController.hasItem(Items.END_CRYSTAL) && bot.isAlive();
    }

    public boolean hasObsidian() {
        return inventoryController.hasItem(Items.OBSIDIAN) &&
                inventoryController.getItemCount(Items.OBSIDIAN) > 0;
    }

    public boolean shouldPlaceObsidian(Player target) {
        double distance = bot.distanceTo(target);
        return distance >= config.getMinCrystalDistance() && distance <= config.getMaxCrystalDistance()
                && !obsidianPositionFinder.findBestObsidianPositions(
                target,
                1,
                recentPlacements,
                config.getPositionCooldownMs(),
                config.getMaxCrystalDistance()
        ).isEmpty();
    }

    public int getPlacedCrystalsCount() {
        return myPlacedCrystals.size();
    }

    public int getCachedObsidianCount() {
        return obsidianCache.size();
    }

    public int getTotalCrystalPlacements() {
        return crystalCountAtPosition.values().stream().mapToInt(Integer::intValue).sum();
    }

    public boolean isDoingCrystalAction() {
        return isPreparingObsidian || isPreparingCrystal || isPreparingAttack;
    }

    public boolean isPreparingObsidianPlacement() {
        return isPreparingObsidian;
    }

    public boolean isPreparingCrystalPlacement() {
        return isPreparingCrystal;
    }

    public boolean isPreparingCrystalAttack() {
        return isPreparingAttack;
    }

    public Optional<BlockPos> getBestObsidianForPearl(Player target) {
        List<BlockPos> bestPositions = obsidianPositionFinder.findBestObsidianPositions(
                target,
                1,
                recentPlacements,
                config.getPositionCooldownMs(),
                config.getMaxCrystalDistance()
        );
        if (bestPositions.isEmpty()) return Optional.empty();
        return Optional.of(bestPositions.get(0));
    }

    private void buildCrystalPlacementQueue(Player target, List<BlockPos> validObsidianPositions) {
        long now = currentTime();
        validObsidianPositions.sort((p1, p2) -> {
            double score1 = crystalPositionEvaluator.calculateCrystalScore(
                    p1,
                    target,
                    crystalCountAtPosition,
                    config.getOptimalDamageRange(),
                    config.getMinCrystalDistance()
            );
            double score2 = crystalPositionEvaluator.calculateCrystalScore(
                    p2,
                    target,
                    crystalCountAtPosition,
                    config.getOptimalDamageRange(),
                    config.getMinCrystalDistance()
            );
            return Double.compare(score2, score1);
        });

        int maxCandidates = Math.min(config.getMaxPositionsToCheck(), validObsidianPositions.size());
        int maxQueued = Math.max(1, config.getMaxCrystalsPerPosition());

        for (int i = 0; i < maxCandidates && queuedCrystalPlacements.size() < maxQueued; i++) {
            BlockPos pos = validObsidianPositions.get(i);
            if (isCrystalPositionCoolingDown(pos, now)) {
                continue;
            }
            double score = crystalPositionEvaluator.calculateCrystalScore(
                    pos,
                    target,
                    crystalCountAtPosition,
                    config.getOptimalDamageRange(),
                    config.getMinCrystalDistance()
            );

            if (score >= config.getMinCrystalScore()) {
                queuedCrystalPlacements.addLast(pos);
            }
        }

        if (queuedCrystalPlacements.isEmpty() && !validObsidianPositions.isEmpty()) {
            for (BlockPos pos : validObsidianPositions) {
                if (!isCrystalPositionCoolingDown(pos, now)) {
                    queuedCrystalPlacements.addLast(pos);
                    break;
                }
            }
        }
    }

    private boolean isStillValidCrystalPlacement(BlockPos pos) {
        return level.getBlockState(pos).getBlock() == Blocks.OBSIDIAN
                || level.getBlockState(pos).getBlock() == Blocks.BEDROCK;
    }

    private void executeObsidianPlacementNow(BlockPos pos) {
        if (!obsidianPlacer.hasLineOfSight(pos)) {
            return;
        }

        if (obsidianPlacer.placeObsidianAt(pos)) {
            obsidianPlaceCooldown = config.getObsidianPlaceCooldownTicks();
            recentPlacements.put(pos, System.currentTimeMillis());
            obsidianCache.put(pos, System.currentTimeMillis());
        }
    }

    private void executeCrystalPlacementNow(BlockPos pos) {
        if (!crystalPlacer.hasLineOfSight(pos)) {
            return;
        }

        if (crystalPlacer.placeCrystal(pos)) {
            crystalPlaceCooldown = config.getCrystalPlaceCooldownTicks();
            crystalRecentUsage.put(pos, currentTime());
            crystalCountAtPosition.put(pos, crystalCountAtPosition.getOrDefault(pos, 0) + 1);

            EndCrystal placedCrystal = crystalManager.findCrystalAt(pos.above(), level);
            if (placedCrystal != null) {
                myPlacedCrystals.add(placedCrystal);
            }
        } else if (isStillValidCrystalPlacement(pos)) {
            crystalRecentUsage.put(pos, currentTime());
            queuedCrystalPlacements.addLast(pos);
        }
    }

    private boolean executeCrystalAttackNow(EndCrystal crystal) {
        if (!crystalAttacker.canAttackCrystal(crystal, config.getCrystalAttackRange())) {
            return false;
        }

        if (crystalAttacker.attackCrystal(crystal)) {
            attackCooldown = config.getAttackCooldownTicks();
            myPlacedCrystals.remove(crystal);
            return true;
        }
        return false;
    }

    private boolean isNearBedrockForCrystalPriority(Player target) {
        BlockPos botPos = bot.blockPosition();
        int y = botPos.getY();

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                BlockPos checkPos = new BlockPos(botPos.getX() + x, y, botPos.getZ() + z);
                if (bot.level().getBlockState(checkPos).getBlock() == Blocks.BEDROCK) {
                    return true;
                }
            }
        }
        return false;
    }

    public void setRank(BotRank rank) {
        this.rank = rank;
        this.config = RankCoordinator.buildCPVPConfig(rank);

        this.obsidianScanner.setConfig(config);
        this.crystalManager.setConfig(config);
    }

    public BotRank getRank() {
        return rank;
    }

    public CPVPConfig getConfig() {
        return config;
    }

    private List<EndCrystal> collectCrystalAttackCandidates(Player target) {
        Set<EndCrystal> unique = new LinkedHashSet<>();
        for (EndCrystal crystal : myPlacedCrystals) {
            if (crystal != null && crystal.isAlive()) {
                unique.add(crystal);
            }
        }
        for (EndCrystal crystal : crystalManager.findNearbyCrystals(bot, level, config.getCrystalAttackRange())) {
            if (crystal != null && crystal.isAlive()) {
                unique.add(crystal);
            }
        }

        List<EndCrystal> candidates = new ArrayList<>(unique);
        candidates.sort((c1, c2) -> {
            double s1 = crystalPositionEvaluator.evaluateCrystalForAttack(
                    c1, target, myPlacedCrystals, config.getCrystalAttackRange(),
                    config.getMinCrystalDistance(), config.getOptimalDamageRange()
            );
            double s2 = crystalPositionEvaluator.evaluateCrystalForAttack(
                    c2, target, myPlacedCrystals, config.getCrystalAttackRange(),
                    config.getMinCrystalDistance(), config.getOptimalDamageRange()
            );
            return Double.compare(s2, s1);
        });
        return candidates;
    }

    private int getCrystalAttackBurstAttempts() {
        if (rank == BotRank.GOD) return 4;
        if (rank == BotRank.HARD) return 3;
        if (rank == BotRank.MEDIUM) return 2;
        if (rank == BotRank.NORMAL) return 2;
        return 1;
    }

    private int getCrystalPlacementBurstAttempts() {
        if (rank == BotRank.GOD) return 3;
        if (rank == BotRank.HARD) return 3;
        if (rank == BotRank.MEDIUM) return 2;
        if (rank == BotRank.NORMAL) return 2;
        return 1;
    }

    private int getCrystalActionCyclesPerTick() {
        if (rank == BotRank.GOD) return 2;
        if (rank == BotRank.HARD) return 2;
        if (rank == BotRank.MEDIUM) return 2;
        return 1;
    }

    private long getCrystalPositionReuseDelayMs() {
        if (rank == BotRank.GOD) return 300L;
        if (rank == BotRank.HARD) return 380L;
        if (rank == BotRank.MEDIUM) return 550L;
        if (rank == BotRank.NORMAL) return 700L;
        return 900L;
    }

    private boolean isCrystalPositionCoolingDown(BlockPos pos, long now) {
        Long lastUsed = crystalRecentUsage.get(pos);
        if (lastUsed == null) {
            return false;
        }
        return now - lastUsed < getCrystalPositionReuseDelayMs();
    }

    private boolean shouldForceNewObsidianPlacement(Player target) {
        if (!hasObsidian() || !canPlaceObsidian()) {
            return false;
        }

        List<BlockPos> validObsidianPositions = crystalManager.getValidCrystalPositions(
                obsidianCache, target, bot, level, config.getMaxCrystalDistance()
        );
        if (validObsidianPositions.isEmpty()) {
            return true;
        }

        long now = currentTime();
        int strongPositions = 0;
        double strongThreshold = config.getMinCrystalScore() + getStrongScoreOffset();

        for (BlockPos pos : validObsidianPositions) {
            if (isCrystalPositionCoolingDown(pos, now)) {
                continue;
            }

            double score = crystalPositionEvaluator.calculateCrystalScore(
                    pos, target, crystalCountAtPosition,
                    config.getOptimalDamageRange(), config.getMinCrystalDistance()
            );
            if (score >= strongThreshold) {
                strongPositions++;
                if (strongPositions >= getDesiredStrongPositionCount()) {
                    return false;
                }
            }
        }

        return true;
    }

    private double getStrongScoreOffset() {
        if (rank == BotRank.GOD) return 0.9D;
        if (rank == BotRank.HARD) return 1.1D;
        if (rank == BotRank.MEDIUM) return 1.25D;
        if (rank == BotRank.NORMAL) return 1.4D;
        return 1.65D;
    }

    private int getDesiredStrongPositionCount() {
        if (rank == BotRank.GOD) return 3;
        if (rank == BotRank.HARD) return 2;
        if (rank == BotRank.MEDIUM) return 2;
        return 1;
    }

    private void pruneObsidianCacheForTarget(Player target) {
        double maxUsefulTargetDistance = switch (rank) {
            case GOD -> 5.2D;
            case HARD -> 5.8D;
            case MEDIUM -> 6.3D;
            case NORMAL -> 6.6D;
            default -> 6.9D;
        };
        long now = currentTime();
        obsidianCache.entrySet().removeIf(entry -> {
            BlockPos pos = entry.getKey();
            double distanceToTarget = target.position().distanceTo(net.minecraft.world.phys.Vec3.atCenterOf(pos));
            if (distanceToTarget <= maxUsefulTargetDistance) {
                return false;
            }
            return now - entry.getValue() > 500L;
        });
    }

    private long currentTime() {
        return System.currentTimeMillis();
    }
}
