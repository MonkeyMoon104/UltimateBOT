package com.monkey.mcbot.bot.ai.controllers.cpvp;

import com.monkey.mcbot.bot.ai.controllers.inventory.BotInventoryController;
import com.monkey.mcbot.bot.ai.controllers.cpvp.helper.crystal.CrystalAttacker;
import com.monkey.mcbot.bot.ai.controllers.cpvp.helper.crystal.CrystalManager;
import com.monkey.mcbot.bot.ai.controllers.cpvp.helper.crystal.CrystalPlacer;
import com.monkey.mcbot.bot.ai.controllers.cpvp.helper.crystal.CrystalPositionEvaluator;
import com.monkey.mcbot.bot.ai.controllers.cpvp.helper.obsidian.ObsidianPlacer;
import com.monkey.mcbot.bot.ai.controllers.cpvp.helper.obsidian.ObsidianPositionFinder;
import com.monkey.mcbot.bot.ai.controllers.cpvp.helper.obsidian.ObsidianScanner;
import com.monkey.mcbot.bot.ai.rank.BotRank;
import com.monkey.mcbot.bot.ai.rank.configs.CPVPConfig;
import com.monkey.mcbot.bot.ai.rank.RankCoordinator;
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
        this.crystalPositionEvaluator = new CrystalPositionEvaluator(bot);
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
        crystalAttacker.updateBotPosition();

        long currentTime = System.currentTimeMillis();

        if (currentTime - lastFullScan > config.getFullScanIntervalMs()) {
            obsidianScanner.scanForExistingObsidian(target, obsidianCache);
            lastFullScan = currentTime;
        }

        handleObsidianPreparation();
        handleCrystalPreparation();
        handleAttackPreparation();

        if (!isPreparingObsidian && !isPreparingCrystal && !isPreparingAttack) {
            if (isNearBedrockForCrystalPriority(target)) {
                tryPlaceOptimalCrystals(target);
                tryAttackOptimalCrystal(target);
            } else {
                tryPlaceObsidianForCrystal(target);
                tryPlaceOptimalCrystals(target);
                tryAttackOptimalCrystal(target);
            }
        }
    }

    public boolean tryPlaceObsidianForCrystal(Player target) {
        if (!canPlaceObsidian()) return false;

        double distance = bot.distanceTo(target);
        if (distance < config.getMinCrystalDistance() || distance > config.getMaxCrystalDistance()) return false;
        if (!hasObsidian()) return false;

        List<BlockPos> bestPositions = obsidianPositionFinder.findBestObsidianPositions(
                target, 3, recentPlacements, cachedValidPositions,
                lastPositionCache, config.getPositionCacheMs()
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
        if (attackCooldown > 0) return;

        EndCrystal bestCrystal = null;
        double bestScore = 0.3;

        for (EndCrystal crystal : myPlacedCrystals) {
            if (!crystal.isAlive()) continue;

            double score = crystalPositionEvaluator.evaluateCrystalForAttack(crystal, target, myPlacedCrystals, config.getCrystalAttackRange(), config.getMinCrystalDistance(), config.getOptimalDamageRange());
            if (score > bestScore) {
                bestScore = score;
                bestCrystal = crystal;
            }
        }

        if (bestCrystal == null) {
            List<EndCrystal> allNearbyCrystals = crystalManager.findNearbyCrystals(bot, level, config.getCrystalAttackRange());

            for (EndCrystal crystal : allNearbyCrystals) {
                if (!crystal.isAlive()) continue;
                if (myPlacedCrystals.contains(crystal)) continue;

                double score = crystalPositionEvaluator.evaluateCrystalForAttack(crystal, target, myPlacedCrystals, config.getCrystalAttackRange(), config.getMinCrystalDistance(), config.getOptimalDamageRange());
                if (score > bestScore) {
                    bestScore = score;
                    bestCrystal = crystal;
                }
            }
        }

        if (bestCrystal != null) {
            startAttackPreparation(bestCrystal);
        }
    }

    private void startAttackPreparation(EndCrystal crystal) {
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
                if (crystalAttacker.attackCrystal(pendingAttackCrystal)) {
                    attackCooldown = config.getAttackCooldownTicks();
                    myPlacedCrystals.remove(pendingAttackCrystal);
                }
            }

            isPreparingAttack = false;
            pendingAttackCrystal = null;
            attackPreparationTicks = 0;
        }
    }

    public void tryPlaceOptimalCrystals(Player target) {
        if (!canPlaceCrystal()) return;

        List<BlockPos> validObsidianPositions = crystalManager.getValidCrystalPositions(obsidianCache, target, bot, level, config.getMaxCrystalDistance());

        if (validObsidianPositions.isEmpty()) return;

        int crystalsToPlace = Math.min(2, validObsidianPositions.size());
        for (int i = 0; i < crystalsToPlace; i++) {
            BlockPos pos = validObsidianPositions.get(i);
            if (crystalPositionEvaluator.calculateCrystalScore(pos, target, crystalCountAtPosition, config.getOptimalDamageRange(), config.getMinCrystalDistance()) > 5.0) {
                startCrystalPreparation(pos);
                break;
            }
        }
    }

    private void startCrystalPreparation(BlockPos pos) {
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

                EndCrystal placedCrystal = crystalManager.findCrystalAt(pendingCrystalPos.above(), level);
                if (placedCrystal != null) {
                    myPlacedCrystals.add(placedCrystal);
                    crystalCountAtPosition.put(pendingCrystalPos, crystalCountAtPosition.getOrDefault(pendingCrystalPos, 0) + 1);
                }
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
                && !obsidianPositionFinder.findBestObsidianPositions(target, 1, recentPlacements, cachedValidPositions, lastPositionCache, config.getPositionCacheMs()).isEmpty();
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
        List<BlockPos> bestPositions = obsidianPositionFinder.findBestObsidianPositions(target, 1, recentPlacements, cachedValidPositions, lastPositionCache, config.getPositionCacheMs());
        if (bestPositions.isEmpty()) return Optional.empty();
        return Optional.of(bestPositions.get(0));
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
}