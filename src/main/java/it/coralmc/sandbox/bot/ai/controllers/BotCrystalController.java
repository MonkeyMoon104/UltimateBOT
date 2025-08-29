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

public class BotCrystalController {

    private final Player bot;
    private final Level level;
    private final BotInventoryController inventoryController;

    private int obsidianPlaceCooldown = 0;
    private int crystalPlaceCooldown = 0;
    private int attackCooldown = 0;

    private static final int OBSIDIAN_PLACE_COOLDOWN_TICKS = 6;
    private static final int CRYSTAL_PLACE_COOLDOWN_TICKS = 8;
    private static final int ATTACK_COOLDOWN_TICKS = 5;
    private static final double MAX_CRYSTAL_DISTANCE = 6.0;
    private static final double MIN_CRYSTAL_DISTANCE = 3.0;
    private static final double CRYSTAL_ATTACK_RANGE = 6.0;

    private final Map<BlockPos, Long> recentPlacements = new HashMap<>();
    private static final long POSITION_COOLDOWN_MS = 2000;

    private final Set<EndCrystal> myPlacedCrystals = new HashSet<>();

    private final Map<BlockPos, Long> obsidianCache = new HashMap<>();
    private static final long OBSIDIAN_CACHE_MS = 5000;

    public BotCrystalController(Player bot, BotInventoryController inventoryController) {
        this.bot = bot;
        this.level = bot.level();
        this.inventoryController = inventoryController;
    }

    public void tick(Player target) {
        if (obsidianPlaceCooldown > 0) obsidianPlaceCooldown--;
        if (crystalPlaceCooldown > 0) crystalPlaceCooldown--;
        if (attackCooldown > 0) attackCooldown--;

        myPlacedCrystals.removeIf(crystal -> !crystal.isAlive());

        cleanupObsidianCache();

        scanForExistingObsidian(target);

        tryPlaceObsidianForCrystal(target);
        tryPlaceOptimalCrystal(target);
        tryAttackOptimalCrystal(target);
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

        BlockPos bestObsidianPos = findBestObsidianPosition(target);
        if (bestObsidianPos == null) return false;

        if (!inventoryController.isHoldingObsidian()) {
            inventoryController.switchToObs();
            return false;
        }

        if (placeObsidianAt(bestObsidianPos)) {
            obsidianPlaceCooldown = OBSIDIAN_PLACE_COOLDOWN_TICKS;
            recentPlacements.put(bestObsidianPos, System.currentTimeMillis());
            obsidianCache.put(bestObsidianPos, System.currentTimeMillis());
            return true;
        }

        return false;
    }

    private BlockPos findBestObsidianPosition(Player target) {
        cleanupRecentPlacements();

        List<BlockPos> potentialPositions = new ArrayList<>();
        BlockPos targetBlockPos = target.blockPosition();

        for (int y = -3; y <= 2; y++) {
            for (int x = -5; x <= 5; x++) {
                for (int z = -5; z <= 5; z++) {
                    BlockPos checkPos = targetBlockPos.offset(x, y, z);
                    if (recentPlacements.containsKey(checkPos)) continue;
                    if (isValidObsidianPosition(checkPos, target)) potentialPositions.add(checkPos);
                }
            }
        }

        if (potentialPositions.isEmpty()) return null;
        return getBestObsidianPosition(potentialPositions, target);
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

        return distanceToBot <= 5.5 && distanceToTarget <= 6.0;
    }

    private BlockPos getBestObsidianPosition(List<BlockPos> positions, Player target) {
        BlockPos bestPos = null;
        double bestScore = -Double.MAX_VALUE;

        Vec3 targetPos = target.position();
        Vec3 botPos = bot.position();
        int targetY = target.blockPosition().getY();

        for (BlockPos pos : positions) {
            double score = calculatePositionScore(pos, target, targetPos, botPos, targetY);
            if (score > bestScore) {
                bestScore = score;
                bestPos = pos;
            }
        }

        return bestPos;
    }

    private double calculatePositionScore(BlockPos pos, Player target, Vec3 targetPos, Vec3 botPos, int targetY) {
        double score = 0;

        Vec3 crystalPos = Vec3.atCenterOf(pos.above());
        double distanceToTarget = targetPos.distanceTo(crystalPos);
        double distanceToBot = botPos.distanceTo(crystalPos);

        if (distanceToTarget <= 4.0) score += (4.0 - distanceToTarget) * 25;

        if (distanceToBot > 5.0) score -= (distanceToBot - 5.0) * 30;

        int yDiff = pos.getY() - targetY;

        if (yDiff < -1) score += Math.abs(yDiff) * 50;
        else if (yDiff == -1) score += 60;
        else if (yDiff == 0) score += 40;
        else if (yDiff == 1) score -= 5;
        else score -= Math.abs(yDiff) * 30;

        if (hasNearbySupport(pos)) score += 15;
        if (!target.onGround() && yDiff >= 0) score -= 20;
        if (target.onGround() && yDiff < 0) score += 20;

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

        for (int y = -4; y <= 3; y++) {
            for (int x = -7; x <= 7; x++) {
                for (int z = -7; z <= 7; z++) {
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

        if (findCrystalAt(pos.above()) != null) return false;

        double distanceToBot = bot.position().distanceTo(Vec3.atCenterOf(pos));
        double distanceToTarget = target.position().distanceTo(Vec3.atCenterOf(pos.above()));

        return distanceToBot <= 5.5 && distanceToTarget <= MAX_CRYSTAL_DISTANCE;
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

        score += targetDamage * 30;
        score -= botDamage * 50;

        if (distanceToTarget <= 4.0) {
            score += (4.0 - distanceToTarget) * 15;
        }

        if (distanceToBot < 4.0) {
            score -= (4.0 - distanceToBot) * 25;
        }

        int yDiff = crystalPos.getY() - target.blockPosition().getY();
        if (yDiff == -1) score += 20;
        else if (yDiff == 0) score += 15;
        else if (yDiff < -1) score += Math.abs(yDiff) * 8;

        return score;
    }

    private double calculatePredictedDamage(double distance) {
        if (distance > 12.0) return 0.0;

        double maxDamage = 12.0;
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

                EndCrystal placedCrystal = findCrystalAt(pos.above());
                if (placedCrystal != null) {
                    myPlacedCrystals.add(placedCrystal);
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
        double bestScore = -1;

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

        if (bestCrystal != null && bestScore > 0.5) {
            attackCrystal(bestCrystal);
        }
    }

    private double evaluateCrystalForAttack(EndCrystal crystal, Player target) {
        Vec3 crystalPos = crystal.position();
        double distanceToTarget = target.position().distanceTo(crystalPos);
        double distanceToBot = bot.position().distanceTo(crystalPos);

        if (distanceToBot > CRYSTAL_ATTACK_RANGE) return -1;

        if (distanceToBot < MIN_CRYSTAL_DISTANCE) return -1;

        double score = 0;

        if (distanceToTarget <= 6.0) {
            score = (6.0 - distanceToTarget) / 6.0;
        }

        if (myPlacedCrystals.contains(crystal)) {
            score += 0.3;
        }

        return score;
    }

    private void tryPlaceOptimalCrystal(Player target) {
        if (!canPlaceCrystal()) return;

        List<BlockPos> validObsidianPositions = new ArrayList<>();

        for (BlockPos obsidianPos : obsidianCache.keySet()) {
            if (isValidCrystalPos(obsidianPos, target)) {
                validObsidianPositions.add(obsidianPos);
            }
        }

        if (validObsidianPositions.isEmpty()) return;

        BlockPos bestPos = null;
        double bestScore = 10.0;

        for (BlockPos pos : validObsidianPositions) {
            double score = calculateCrystalScore(pos, target);
            if (score > bestScore) {
                bestScore = score;
                bestPos = pos;
            }
        }

        if (bestPos != null) {
            System.out.println("Tentativo di piazzare crystal su obsidian esistente a: " + bestPos + " con score: " + bestScore);
            placeCrystal(bestPos);
        }
    }

    private Direction findBestPlacementFace(BlockPos targetPos) {
        Vec3 botPos = bot.position();

        for (Direction direction : Direction.values()) {
            BlockPos adjacentPos = targetPos.relative(direction.getOpposite());
            BlockState adjacentState = level.getBlockState(adjacentPos);

            if (adjacentState.isSolid() && !adjacentState.isAir()) {
                double distance = botPos.distanceTo(Vec3.atCenterOf(adjacentPos));
                if (distance <= 5.5) return direction;
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
                && findBestObsidianPosition(target) != null;
    }

    public int getPlacedCrystalsCount() {
        return myPlacedCrystals.size();
    }

    public int getCachedObsidianCount() {
        return obsidianCache.size();
    }
}