package it.coralmc.sandbox.bot.ai.controllers.cpvp;

import it.coralmc.sandbox.bot.ai.controllers.inventory.BotInventoryController;
import it.coralmc.sandbox.bot.ai.controllers.rotation.BotRotationController;
import it.coralmc.sandbox.bot.ai.controllers.cpvp.helper.crystal.CrystalAttacker;
import it.coralmc.sandbox.bot.ai.controllers.cpvp.helper.crystal.CrystalManager;
import it.coralmc.sandbox.bot.ai.controllers.cpvp.helper.crystal.CrystalPlacer;
import it.coralmc.sandbox.bot.ai.controllers.cpvp.helper.crystal.CrystalPositionEvaluator;
import it.coralmc.sandbox.bot.ai.controllers.cpvp.helper.obsidian.ObsidianPlacer;
import it.coralmc.sandbox.bot.ai.controllers.cpvp.helper.obsidian.ObsidianPositionFinder;
import it.coralmc.sandbox.bot.ai.controllers.cpvp.helper.obsidian.ObsidianScanner;
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
    private final BotRotationController rotationController;

    private final ObsidianPlacer obsidianPlacer;
    private final CrystalPlacer crystalPlacer;
    private final CrystalAttacker crystalAttacker;
    private final ObsidianPositionFinder obsidianPositionFinder;
    private final CrystalPositionEvaluator crystalPositionEvaluator;
    private final ObsidianScanner obsidianScanner;
    private final CrystalManager crystalManager;

    private int obsidianPlaceCooldown = 0;
    private int crystalPlaceCooldown = 0;
    private int attackCooldown = 0;

    private static final int OBSIDIAN_PLACE_COOLDOWN_TICKS = 4;
    private static final int CRYSTAL_PLACE_COOLDOWN_TICKS = 5;
    private static final int ATTACK_COOLDOWN_TICKS = 3;

    private static final double MAX_CRYSTAL_DISTANCE = 10.0;
    private static final double MIN_CRYSTAL_DISTANCE = 2.5;
    private static final double CRYSTAL_ATTACK_RANGE = 8.0;
    private static final double OPTIMAL_DAMAGE_RANGE = 6.0;

    private final Map<BlockPos, Long> recentPlacements = new ConcurrentHashMap<>();
    private static final long POSITION_COOLDOWN_MS = 1500;

    private final Set<EndCrystal> myPlacedCrystals = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final Map<BlockPos, Long> obsidianCache = new ConcurrentHashMap<>();
    private static final long OBSIDIAN_CACHE_MS = 6000;

    private final Map<BlockPos, Integer> crystalCountAtPosition = new ConcurrentHashMap<>();
    private static final int MAX_CRYSTALS_PER_POSITION = 7;

    private long lastFullScan = 0;
    private static final long FULL_SCAN_INTERVAL_MS = 500;
    private final List<BlockPos> cachedValidPositions = new ArrayList<>();
    private long lastPositionCache = 0;
    private static final long POSITION_CACHE_MS = 1000;

    private boolean isPreparingObsidian = false;
    private BlockPos pendingObsidianPos = null;
    private int obsidianPreparationTicks = 0;
    private static final int OBSIDIAN_PREPARATION_TIME = 2;

    private boolean isPreparingCrystal = false;
    private BlockPos pendingCrystalPos = null;
    private int crystalPreparationTicks = 0;
    private static final int CRYSTAL_PREPARATION_TIME = 2;

    private boolean isPreparingAttack = false;
    private EndCrystal pendingAttackCrystal = null;
    private int attackPreparationTicks = 0;
    private static final int ATTACK_PREPARATION_TIME = 1;

    public BotCPVPController(Player bot, BotInventoryController inventoryController, BotRotationController rotationController) {
        this.bot = bot;
        this.level = bot.level();
        this.inventoryController = inventoryController;
        this.rotationController = rotationController;

        this.obsidianPlacer = new ObsidianPlacer(bot, inventoryController, rotationController, level);
        this.crystalPlacer = new CrystalPlacer(bot, inventoryController, rotationController, level);
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

        if (currentTime - lastFullScan > FULL_SCAN_INTERVAL_MS) {
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
        if (distance < MIN_CRYSTAL_DISTANCE || distance > MAX_CRYSTAL_DISTANCE) return false;
        if (!hasObsidian()) return false;

        List<BlockPos> bestPositions = obsidianPositionFinder.findBestObsidianPositions(
                target, 3, recentPlacements, cachedValidPositions,
                lastPositionCache, POSITION_CACHE_MS
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

        rotationController.lookAt(net.minecraft.world.phys.Vec3.atCenterOf(pos));
    }

    private void handleObsidianPreparation() {
        if (!isPreparingObsidian || pendingObsidianPos == null) return;

        obsidianPreparationTicks++;

        rotationController.lookAt(net.minecraft.world.phys.Vec3.atCenterOf(pendingObsidianPos));

        if (obsidianPreparationTicks >= OBSIDIAN_PREPARATION_TIME) {
            if (!obsidianPlacer.hasLineOfSight(pendingObsidianPos)) {
                isPreparingObsidian = false;
                pendingObsidianPos = null;
                obsidianPreparationTicks = 0;
                return;
            }

            if (obsidianPlacer.placeObsidianAt(pendingObsidianPos)) {
                obsidianPlaceCooldown = OBSIDIAN_PLACE_COOLDOWN_TICKS;
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

            double score = crystalPositionEvaluator.evaluateCrystalForAttack(crystal, target, myPlacedCrystals, CRYSTAL_ATTACK_RANGE, MIN_CRYSTAL_DISTANCE, OPTIMAL_DAMAGE_RANGE);
            if (score > bestScore) {
                bestScore = score;
                bestCrystal = crystal;
            }
        }

        if (bestCrystal == null) {
            List<EndCrystal> allNearbyCrystals = crystalManager.findNearbyCrystals(bot, level, CRYSTAL_ATTACK_RANGE);

            for (EndCrystal crystal : allNearbyCrystals) {
                if (!crystal.isAlive()) continue;
                if (myPlacedCrystals.contains(crystal)) continue;

                double score = crystalPositionEvaluator.evaluateCrystalForAttack(crystal, target, myPlacedCrystals, CRYSTAL_ATTACK_RANGE, MIN_CRYSTAL_DISTANCE, OPTIMAL_DAMAGE_RANGE);
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

        rotationController.lookAt(crystal.position());
    }

    private void handleAttackPreparation() {
        if (!isPreparingAttack || pendingAttackCrystal == null || !pendingAttackCrystal.isAlive()) {
            isPreparingAttack = false;
            pendingAttackCrystal = null;
            attackPreparationTicks = 0;
            return;
        }

        attackPreparationTicks++;

        rotationController.lookAt(pendingAttackCrystal.position());

        if (attackPreparationTicks >= ATTACK_PREPARATION_TIME) {
            if (crystalAttacker.canAttackCrystal(pendingAttackCrystal, CRYSTAL_ATTACK_RANGE)) {
                if (crystalAttacker.attackCrystal(pendingAttackCrystal)) {
                    attackCooldown = ATTACK_COOLDOWN_TICKS;
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

        List<BlockPos> validObsidianPositions = crystalManager.getValidCrystalPositions(obsidianCache, target, bot, level, MAX_CRYSTAL_DISTANCE);

        if (validObsidianPositions.isEmpty()) return;

        int crystalsToPlace = Math.min(2, validObsidianPositions.size());
        for (int i = 0; i < crystalsToPlace; i++) {
            BlockPos pos = validObsidianPositions.get(i);
            if (crystalPositionEvaluator.calculateCrystalScore(pos, target, crystalCountAtPosition, OPTIMAL_DAMAGE_RANGE, MIN_CRYSTAL_DISTANCE) > 5.0) {
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
        rotationController.lookAt(crystalPlacementPos);
    }

    private void handleCrystalPreparation() {
        if (!isPreparingCrystal || pendingCrystalPos == null) return;

        crystalPreparationTicks++;

        net.minecraft.world.phys.Vec3 crystalPlacementPos = net.minecraft.world.phys.Vec3.atCenterOf(pendingCrystalPos.above());
        rotationController.lookAt(crystalPlacementPos);

        if (crystalPreparationTicks >= CRYSTAL_PREPARATION_TIME) {
            if (!crystalPlacer.hasLineOfSight(pendingCrystalPos)) {
                isPreparingCrystal = false;
                pendingCrystalPos = null;
                crystalPreparationTicks = 0;
                return;
            }

            if (crystalPlacer.placeCrystal(pendingCrystalPos)) {
                crystalPlaceCooldown = CRYSTAL_PLACE_COOLDOWN_TICKS;

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
        return distance >= MIN_CRYSTAL_DISTANCE && distance <= MAX_CRYSTAL_DISTANCE
                && !obsidianPositionFinder.findBestObsidianPositions(target, 1, recentPlacements, cachedValidPositions, lastPositionCache, POSITION_CACHE_MS).isEmpty();
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
        List<BlockPos> bestPositions = obsidianPositionFinder.findBestObsidianPositions(target, 1, recentPlacements, cachedValidPositions, lastPositionCache, POSITION_CACHE_MS);
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
}