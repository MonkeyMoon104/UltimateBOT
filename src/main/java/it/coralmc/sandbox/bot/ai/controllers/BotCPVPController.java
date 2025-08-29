package it.coralmc.sandbox.bot.ai.controllers;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BotCPVPController {

    private final Player bot;
    private final Level level;
    private final BotInventoryController inventoryController;
    private final BotRotationController rotationController;

    private int obsidianPlaceCooldown = 0;
    private int crystalPlaceCooldown = 0;
    private int attackCooldown = 0;

    private static final int OBSIDIAN_PLACE_COOLDOWN_TICKS = 4;
    private static final int CRYSTAL_PLACE_COOLDOWN_TICKS = 5;
    private static final int ATTACK_COOLDOWN_TICKS = 3;

    private static final double MAX_CRYSTAL_DISTANCE = 8.0;
    private static final double MIN_CRYSTAL_DISTANCE = 2.5;
    private static final double CRYSTAL_ATTACK_RANGE = 8.0;
    private static final double OPTIMAL_DAMAGE_RANGE = 6.0;

    private final Map<BlockPos, Long> recentPlacements = new ConcurrentHashMap<>();
    private static final long POSITION_COOLDOWN_MS = 1500;

    private final Set<EndCrystal> myPlacedCrystals = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final Map<BlockPos, Long> obsidianCache = new ConcurrentHashMap<>();
    private static final long OBSIDIAN_CACHE_MS = 7000;

    private final Map<BlockPos, Integer> crystalCountAtPosition = new ConcurrentHashMap<>();
    private static final int MAX_CRYSTALS_PER_POSITION = 3;

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
    }

    public void tick(Player target) {
        if (obsidianPlaceCooldown > 0) obsidianPlaceCooldown--;
        if (crystalPlaceCooldown > 0) crystalPlaceCooldown--;
        if (attackCooldown > 0) attackCooldown--;

        myPlacedCrystals.removeIf(crystal -> !crystal.isAlive());
        cleanupCrystalCounts();
        cleanupObsidianCache();

        long currentTime = System.currentTimeMillis();

        if (currentTime - lastFullScan > FULL_SCAN_INTERVAL_MS) {
            scanForExistingObsidian(target);
            lastFullScan = currentTime;
        }

        handleObsidianPreparation();
        handleCrystalPreparation();
        handleAttackPreparation();

        if (!isPreparingObsidian && !isPreparingCrystal && !isPreparingAttack) {
            tryPlaceObsidianForCrystal(target);
            tryPlaceOptimalCrystals(target);
            tryAttackOptimalCrystal(target);
        }
    }

    private void cleanupCrystalCounts() {
        crystalCountAtPosition.entrySet().removeIf(entry -> {
            BlockPos pos = entry.getKey();
            return findCrystalAt(pos.above()) == null;
        });
    }

    private void cleanupObsidianCache() {
        long currentTime = System.currentTimeMillis();
        obsidianCache.entrySet().removeIf(entry ->
                currentTime - entry.getValue() > OBSIDIAN_CACHE_MS);
    }

    public boolean tryPlaceObsidianForCrystal(Player target) {
        if (!canPlaceObsidian()) return false;

        double distance = bot.distanceTo(target);
        if (distance < MIN_CRYSTAL_DISTANCE || distance > MAX_CRYSTAL_DISTANCE) return false;
        if (!hasObsidian()) return false;

        List<BlockPos> bestPositions = findBestObsidianPositions(target, 3);
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

        rotationController.lookAt(Vec3.atCenterOf(pos));
    }

    private void handleObsidianPreparation() {
        if (!isPreparingObsidian || pendingObsidianPos == null) return;

        obsidianPreparationTicks++;

        rotationController.lookAt(Vec3.atCenterOf(pendingObsidianPos));

        if (obsidianPreparationTicks >= OBSIDIAN_PREPARATION_TIME) {
            if (placeObsidianAt(pendingObsidianPos)) {
                obsidianPlaceCooldown = OBSIDIAN_PLACE_COOLDOWN_TICKS;
                recentPlacements.put(pendingObsidianPos, System.currentTimeMillis());
                obsidianCache.put(pendingObsidianPos, System.currentTimeMillis());
            }

            isPreparingObsidian = false;
            pendingObsidianPos = null;
            obsidianPreparationTicks = 0;
        }
    }

    private List<BlockPos> findBestObsidianPositions(Player target, int maxPositions) {
        cleanupRecentPlacements();

        long currentTime = System.currentTimeMillis();

        if (currentTime - lastPositionCache > POSITION_CACHE_MS) {
            cachedValidPositions.clear();
            BlockPos targetBlockPos = target.blockPosition();

            for (int y = -4; y <= 3; y++) {
                for (int x = -7; x <= 7; x++) {
                    for (int z = -7; z <= 7; z++) {
                        BlockPos checkPos = targetBlockPos.offset(x, y, z);
                        if (recentPlacements.containsKey(checkPos)) continue;
                        if (isValidObsidianPosition(checkPos, target)) {
                            cachedValidPositions.add(checkPos);
                        }
                    }
                }
            }
            lastPositionCache = currentTime;
        }

        if (cachedValidPositions.isEmpty()) return new ArrayList<>();

        return cachedValidPositions.stream()
                .sorted((pos1, pos2) -> Double.compare(
                        calculatePositionScore(pos2, target),
                        calculatePositionScore(pos1, target)
                ))
                .limit(maxPositions)
                .toList();
    }

    private void cleanupRecentPlacements() {
        long currentTime = System.currentTimeMillis();
        recentPlacements.entrySet().removeIf(entry ->
                currentTime - entry.getValue() > POSITION_COOLDOWN_MS);
    }

    private boolean isValidObsidianPosition(BlockPos pos, Player target) {
        BlockState currentState = level.getBlockState(pos);
        if (!currentState.canBeReplaced()) return false;

        BlockState below = level.getBlockState(pos.below());
        if (!below.isSolid()) return false;

        if (!level.getBlockState(pos.above()).isAir() || !level.getBlockState(pos.above(2)).isAir())
            return false;

        double distanceToBot = bot.position().distanceTo(Vec3.atCenterOf(pos));
        double distanceToTarget = target.position().distanceTo(Vec3.atCenterOf(pos.above()));

        return distanceToBot <= 6.5 && distanceToTarget <= MAX_CRYSTAL_DISTANCE;
    }

    private double calculatePositionScore(BlockPos pos, Player target) {
        Vec3 targetPos = target.position();
        Vec3 botPos = bot.position();
        int targetY = target.blockPosition().getY();

        double score = 0;

        Vec3 crystalPos = Vec3.atCenterOf(pos.above());
        double distanceToTarget = targetPos.distanceTo(crystalPos);
        double distanceToBot = botPos.distanceTo(crystalPos);

        if (distanceToTarget <= OPTIMAL_DAMAGE_RANGE) {
            score += (OPTIMAL_DAMAGE_RANGE - distanceToTarget) * 30;
        }

        if (distanceToBot > MIN_CRYSTAL_DISTANCE) {
            if (distanceToBot > 6.0) score -= (distanceToBot - 6.0) * 20;
        } else {
            score -= 100;
        }

        int yDiff = pos.getY() - targetY;
        if (yDiff < -1) score += Math.abs(yDiff) * 40;
        else if (yDiff == -1) score += 80;
        else if (yDiff == 0) score += 60;
        else if (yDiff == 1) score += 20;
        else score -= Math.abs(yDiff) * 25;

        if (hasNearbySupport(pos)) score += 25;

        if (!target.onGround() && yDiff >= 0) score += 30;
        if (target.onGround() && yDiff < 0) score += 40;

        return score;
    }

    private boolean hasNearbySupport(BlockPos pos) {
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos checkPos = pos.relative(dir);
            if (level.getBlockState(checkPos).isSolid()) return true;
        }
        return false;
    }

    private void scanForExistingObsidian(Player target) {
        BlockPos targetPos = target.blockPosition();
        long currentTime = System.currentTimeMillis();

        for (int y = -5; y <= 4; y++) {
            for (int x = -9; x <= 9; x++) {
                for (int z = -9; z <= 9; z++) {
                    BlockPos checkPos = targetPos.offset(x, y, z);

                    if (obsidianCache.containsKey(checkPos) &&
                            currentTime - obsidianCache.get(checkPos) < OBSIDIAN_CACHE_MS) {
                        continue;
                    }

                    BlockState state = level.getBlockState(checkPos);
                    if (state.is(Blocks.OBSIDIAN) || state.is(Blocks.BEDROCK)) {
                        obsidianCache.put(checkPos, currentTime);
                    }
                }
            }
        }
    }

    private boolean isValidCrystalPos(BlockPos pos, Player target) {
        BlockState state = level.getBlockState(pos);
        if (!(state.is(Blocks.OBSIDIAN) || state.is(Blocks.BEDROCK))) return false;
        if (!level.getBlockState(pos.above()).isAir() || !level.getBlockState(pos.above(2)).isAir()) return false;

        double distanceToBot = bot.position().distanceTo(Vec3.atCenterOf(pos));
        double distanceToTarget = target.position().distanceTo(Vec3.atCenterOf(pos.above()));

        return distanceToBot <= 6.5 && distanceToTarget <= MAX_CRYSTAL_DISTANCE;
    }

    private double calculateCrystalScore(BlockPos crystalPos, Player target) {
        Vec3 targetPos = target.position();
        Vec3 botPos = bot.position();
        Vec3 crystalCenter = Vec3.atCenterOf(crystalPos.above());

        double distanceToTarget = crystalCenter.distanceTo(targetPos);
        double distanceToBot = crystalCenter.distanceTo(botPos);

        if (distanceToBot < MIN_CRYSTAL_DISTANCE) {
            return -1000.0;
        }

        double score = 0;

        double targetDamage = calculatePredictedDamage(distanceToTarget);
        double botDamage = calculatePredictedDamage(distanceToBot);

        score += targetDamage * 40;
        score -= botDamage * 60;

        if (distanceToTarget <= OPTIMAL_DAMAGE_RANGE) {
            score += (OPTIMAL_DAMAGE_RANGE - distanceToTarget) * 20;
        }

        if (distanceToBot < 4.0) {
            score -= (4.0 - distanceToBot) * 40;
        }

        int yDiff = crystalPos.getY() - target.blockPosition().getY();
        if (yDiff == -1) score += 35;
        else if (yDiff == 0) score += 25;
        else if (yDiff < -1) score += Math.abs(yDiff) * 12;

        int existingCount = crystalCountAtPosition.getOrDefault(crystalPos, 0);
        if (existingCount > 0 && distanceToBot > 4.0) {
            score += existingCount * 15;
        }

        return score;
    }

    private double calculatePredictedDamage(double distance) {
        if (distance > 12.0) return 0.0;

        double maxDamage = 14.0;
        double falloff = Math.max(0.0, 1.0 - (distance / 12.0));

        return maxDamage * falloff * falloff;
    }

    private boolean placeCrystal(BlockPos pos) {
        if (!canPlaceCrystal()) return false;
        if (!inventoryController.hasItem(Items.END_CRYSTAL)) return false;

        if (!inventoryController.isHoldingCrystal()) {
            inventoryController.switchToCrystal();
            return false;
        }

        try {
            ItemStack crystalStack = inventoryController.getCurrentItem();
            if (crystalStack.getItem() != Items.END_CRYSTAL) return false;

            BlockHitResult hitResult = new BlockHitResult(
                    Vec3.atCenterOf(pos).add(0, 0.5, 0),
                    Direction.UP,
                    pos,
                    false
            );

            UseOnContext context = new UseOnContext(bot, InteractionHand.MAIN_HAND, hitResult);
            InteractionResult result = crystalStack.useOn(context);

            if (result.consumesAction()) {
                bot.swing(InteractionHand.MAIN_HAND);
                crystalPlaceCooldown = CRYSTAL_PLACE_COOLDOWN_TICKS;

                inventoryController.onItemUsed(BotInventoryController.CRYSTAL_SLOT);

                EndCrystal placedCrystal = findCrystalAt(pos.above());
                if (placedCrystal != null) {
                    myPlacedCrystals.add(placedCrystal);
                    crystalCountAtPosition.put(pos, crystalCountAtPosition.getOrDefault(pos, 0) + 1);
                }
                return true;
            }
        } catch (Exception e) {
            System.err.println("Errore nel piazzamento crystal: " + e.getMessage());
        }

        return false;
    }

    private EndCrystal findCrystalAt(BlockPos pos) {
        return level.getEntitiesOfClass(EndCrystal.class,
                        new AABB(pos).inflate(1.5))
                .stream()
                .findFirst()
                .orElse(null);
    }

    private boolean attackCrystal(EndCrystal crystal) {
        if (crystal == null || !crystal.isAlive()) return false;
        if (attackCooldown > 0) return false;

        double distance = bot.position().distanceTo(crystal.position());
        if (distance > CRYSTAL_ATTACK_RANGE) return false;

        try {
            bot.attack(crystal);
            bot.swing(InteractionHand.MAIN_HAND);

            attackCooldown = ATTACK_COOLDOWN_TICKS;
            myPlacedCrystals.remove(crystal);

            return true;
        } catch (Exception e) {
            System.err.println("Errore nell'attacco al crystal: " + e.getMessage());
            return false;
        }
    }

    private void tryAttackOptimalCrystal(Player target) {
        if (attackCooldown > 0) return;

        EndCrystal bestCrystal = null;
        double bestScore = 0.3;

        for (EndCrystal crystal : myPlacedCrystals) {
            if (!crystal.isAlive()) continue;

            double score = evaluateCrystalForAttack(crystal, target);
            if (score > bestScore) {
                bestScore = score;
                bestCrystal = crystal;
            }
        }

        if (bestCrystal == null) {
            List<EndCrystal> allNearbyCrystals = level.getEntitiesOfClass(EndCrystal.class,
                    new AABB(bot.blockPosition()).inflate(CRYSTAL_ATTACK_RANGE));

            for (EndCrystal crystal : allNearbyCrystals) {
                if (!crystal.isAlive()) continue;
                if (myPlacedCrystals.contains(crystal)) continue;

                double score = evaluateCrystalForAttack(crystal, target);
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
            attackCrystal(pendingAttackCrystal);

            isPreparingAttack = false;
            pendingAttackCrystal = null;
            attackPreparationTicks = 0;
        }
    }

    private double evaluateCrystalForAttack(EndCrystal crystal, Player target) {
        Vec3 crystalPos = crystal.position();
        double distanceToTarget = target.position().distanceTo(crystalPos);
        double distanceToBot = bot.position().distanceTo(crystalPos);

        if (distanceToBot > CRYSTAL_ATTACK_RANGE) return -1;
        if (distanceToBot < MIN_CRYSTAL_DISTANCE) return -1;

        double score = 0;

        if (distanceToTarget <= OPTIMAL_DAMAGE_RANGE) {
            score = (OPTIMAL_DAMAGE_RANGE - distanceToTarget) / OPTIMAL_DAMAGE_RANGE;
        }

        if (myPlacedCrystals.contains(crystal)) {
            score += 0.4;
        }

        if (distanceToTarget <= 3.0) {
            score += 0.3;
        }

        return score;
    }

    private void tryPlaceOptimalCrystals(Player target) {
        if (!canPlaceCrystal()) return;

        List<BlockPos> validObsidianPositions = new ArrayList<>();

        for (BlockPos obsidianPos : obsidianCache.keySet()) {
            if (isValidCrystalPos(obsidianPos, target)) {
                int currentCount = crystalCountAtPosition.getOrDefault(obsidianPos, 0);
                if (currentCount < MAX_CRYSTALS_PER_POSITION) {
                    validObsidianPositions.add(obsidianPos);
                }
            }
        }

        if (validObsidianPositions.isEmpty()) return;

        validObsidianPositions.sort((pos1, pos2) -> Double.compare(
                calculateCrystalScore(pos2, target),
                calculateCrystalScore(pos1, target)
        ));

        BlockPos bestPos = validObsidianPositions.get(0);
        double bestScore = calculateCrystalScore(bestPos, target);

        if (bestScore > 15.0) {
            startCrystalPreparation(bestPos);
        }
    }

    private void startCrystalPreparation(BlockPos pos) {
        isPreparingCrystal = true;
        pendingCrystalPos = pos;
        crystalPreparationTicks = 0;

        Vec3 crystalPlacementPos = Vec3.atCenterOf(pos.above());
        rotationController.lookAt(crystalPlacementPos);
    }

    private void handleCrystalPreparation() {
        if (!isPreparingCrystal || pendingCrystalPos == null) return;

        crystalPreparationTicks++;

        Vec3 crystalPlacementPos = Vec3.atCenterOf(pendingCrystalPos.above());
        rotationController.lookAt(crystalPlacementPos);

        if (crystalPreparationTicks >= CRYSTAL_PREPARATION_TIME) {
            placeCrystal(pendingCrystalPos);

            isPreparingCrystal = false;
            pendingCrystalPos = null;
            crystalPreparationTicks = 0;
        }
    }

    private Direction findBestPlacementFace(BlockPos targetPos) {
        Vec3 botPos = bot.position();

        for (Direction direction : Direction.values()) {
            BlockPos adjacentPos = targetPos.relative(direction.getOpposite());
            BlockState adjacentState = level.getBlockState(adjacentPos);

            if (adjacentState.isSolid() && !adjacentState.isAir()) {
                double distance = botPos.distanceTo(Vec3.atCenterOf(adjacentPos));
                if (distance <= 6.5) return direction;
            }
        }

        return null;
    }

    private boolean placeObsidianAt(BlockPos pos) {
        try {
            ItemStack obsidianStack = inventoryController.getCurrentItem();
            if (obsidianStack.getItem() != Items.OBSIDIAN) return false;

            Direction bestFace = findBestPlacementFace(pos);
            if (bestFace == null) return false;

            BlockPos adjacentPos = pos.relative(bestFace.getOpposite());

            BlockHitResult hitResult = new BlockHitResult(
                    Vec3.atCenterOf(adjacentPos).relative(bestFace, 0.5),
                    bestFace,
                    adjacentPos,
                    false
            );

            UseOnContext context = new UseOnContext(bot, InteractionHand.MAIN_HAND, hitResult);
            InteractionResult result = obsidianStack.useOn(context);

            if (result.consumesAction()) {
                bot.swing(InteractionHand.MAIN_HAND);

                inventoryController.onItemUsed(BotInventoryController.OBSIDIAN_SLOT);

                return true;
            }

        } catch (Exception e) {
            System.err.println("Errore nel piazzamento ossidiana: " + e.getMessage());
        }

        return false;
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
                && !findBestObsidianPositions(target, 1).isEmpty();
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
        List<BlockPos> bestPositions = findBestObsidianPositions(target, 1);
        if (bestPositions.isEmpty()) return Optional.empty();
        return Optional.of(bestPositions.get(0));
    }
}