package com.monkey.ultimatebot.bot.ai.controllers.rapvp;

import com.monkey.ultimatebot.bot.ai.controllers.combat.ExplosionDamageEstimator;
import com.monkey.ultimatebot.bot.ai.controllers.enderpearl.BotEnderpearlController;
import com.monkey.ultimatebot.bot.ai.controllers.inventory.BotInventoryController;
import com.monkey.ultimatebot.bot.ai.controllers.rapvp.helper.*;
import com.monkey.ultimatebot.bot.ai.controllers.rotation.BotRotationController;
import com.monkey.ultimatebot.bot.ai.difficulty.DifficultyLevel;
import com.monkey.ultimatebot.bot.ai.difficulty.DifficultyProfileFactory;
import com.monkey.ultimatebot.bot.ai.difficulty.configs.RAPVPConfig;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class BotRAPVPController {

    private final Player bot;
    private final Level level;
    private final BotInventoryController inventory;
    private final BotEnderpearlController pearlController;
    private final AnchorPlacer anchorPlacer;
    private final AnchorCharger anchorCharger;
    private final AnchorExploder anchorExploder;
    private final AnchorPositionFinder positionFinder;

    private boolean enabled = false;
    private RAPVPState state = RAPVPState.IDLE;

    private @Nullable BlockPos anchorPos;
    private @Nullable Player currentTarget;
    private DifficultyLevel difficulty = DifficultyLevel.NORMAL;
    private RAPVPConfig config = DifficultyProfileFactory.buildRAPVPConfig(difficulty);
    private int failedExplosionAttempts = 0;
    private long lastAnchorExplosionTime = 0L;
    private boolean ownsAnchor;

    public BotRAPVPController(
            Player bot,
            BotInventoryController inventory,
            BotRotationController rotation,
            BotEnderpearlController pearlController) {
        this.bot = bot;
        this.inventory = inventory;
        this.pearlController = pearlController;
        this.level = bot.level();

        this.anchorPlacer = new AnchorPlacer(bot, inventory, rotation, level);
        this.anchorCharger = new AnchorCharger(bot, inventory, rotation, level);
        this.anchorExploder = new AnchorExploder(bot, inventory, rotation, level);
        this.positionFinder = new AnchorPositionFinder(bot, level);
        setDifficulty(DifficultyLevel.NORMAL);
    }

    public void enable(Player target) {
        this.enabled = true;
        this.state = RAPVPState.PLACING_ANCHOR;
        this.anchorPos = null;
        this.currentTarget = target;
        this.failedExplosionAttempts = 0;
        this.lastAnchorExplosionTime = 0L;
        this.ownsAnchor = false;
    }

    public void tick() {
        if (!bot.isAlive() || currentTarget == null || !currentTarget.isAlive() || !enabled) {
            return;
        }

        int actionCycles = getActionCyclesForDifficulty();
        for (int i = 0; i < actionCycles; i++) {
            if (!enabled || currentTarget == null || !currentTarget.isAlive()) {
                break;
            }
            switch (state) {
                case PLACING_ANCHOR -> handlePlacingAnchor();
                case CHARGING_ANCHOR -> handleChargingAnchor();
                case WAITING_EXPLOSION -> handleWaitingExplosion();
                case IDLE -> {}
            }
        }
    }

    private void handlePlacingAnchor() {
        Optional<BlockPos> reusableAnchor = findReusableAnchorNearTarget();
        if (reusableAnchor.isPresent()) {
            anchorPos = reusableAnchor.get();
            ownsAnchor = false;
            BlockState reusableState = bot.level().getBlockState(anchorPos);
            int charges = reusableState.getValue(RespawnAnchorBlock.CHARGE);
            state = charges > 0 ? RAPVPState.WAITING_EXPLOSION : RAPVPState.CHARGING_ANCHOR;
            return;
        }

        Player target = Objects.requireNonNull(currentTarget, "currentTarget");
        Optional<BlockPos> posOpt = positionFinder.findBestAnchorPos(target);
        if (posOpt.isEmpty()) {
            if (!isHyperAggressiveDifficulty()
                    && pearlController.canUseEnderpearl()
                    && bot.distanceTo(currentTarget) > 10.0D) {
                pearlController.tryUseEnderpearl(target);
            }
            return;
        }

        anchorPos = posOpt.get();

        if (!inventory.isHoldingAnchor()) {
            inventory.switchToAnchor();
            return;
        }

        if (anchorPlacer.placeAnchor(anchorPos)) {
            ownsAnchor = true;
            state = RAPVPState.CHARGING_ANCHOR;
            failedExplosionAttempts = 0;
        } else if (bot.level().getBlockState(anchorPos).getBlock() instanceof RespawnAnchorBlock) {
            state = RAPVPState.CHARGING_ANCHOR;
        }
    }

    private void handleChargingAnchor() {
        if (anchorPos == null) {
            state = RAPVPState.PLACING_ANCHOR;
            return;
        }

        BlockState stateBlock = bot.level().getBlockState(anchorPos);
        if (!(stateBlock.getBlock() instanceof RespawnAnchorBlock)) {
            state = RAPVPState.PLACING_ANCHOR;
            anchorPos = null;
            return;
        }

        int charges = stateBlock.getValue(RespawnAnchorBlock.CHARGE);
        if (charges > 0) {
            state = RAPVPState.WAITING_EXPLOSION;
            return;
        }

        if (!inventory.isHoldingGlow()) {
            inventory.switchToGlow();
            return;
        }

        if (anchorCharger.chargeAnchor(anchorPos)) {
            state = RAPVPState.WAITING_EXPLOSION;
            return;
        }

        BlockState afterCharge = bot.level().getBlockState(anchorPos);
        if (afterCharge.getBlock() instanceof RespawnAnchorBlock
                && afterCharge.getValue(RespawnAnchorBlock.CHARGE) > 0) {
            state = RAPVPState.WAITING_EXPLOSION;
        }
    }

    private void handleWaitingExplosion() {
        if (anchorPos == null) {
            state = RAPVPState.PLACING_ANCHOR;
            return;
        }

        BlockState stateBlock = bot.level().getBlockState(anchorPos);
        if (stateBlock.getBlock() instanceof RespawnAnchorBlock) {
            int charges = stateBlock.getValue(RespawnAnchorBlock.CHARGE);
            if (charges <= 0) {
                state = RAPVPState.CHARGING_ANCHOR;
                return;
            }

            inventory.switchToEmptySlot();
            boolean exploded = anchorExploder.explodeAnchor(anchorPos);
            if (exploded) {
                failedExplosionAttempts = 0;
                lastAnchorExplosionTime = System.currentTimeMillis();
                state = RAPVPState.PLACING_ANCHOR;
                anchorPos = null;
                ownsAnchor = false;
            } else {
                failedExplosionAttempts++;
                int retryLimit = isHyperAggressiveDifficulty() ? 3 : 4;
                if (failedExplosionAttempts >= retryLimit) {
                    failedExplosionAttempts = 0;
                    state = RAPVPState.PLACING_ANCHOR;
                    anchorPos = null;
                    ownsAnchor = false;
                }
            }
        } else {
            failedExplosionAttempts = 0;
            state = RAPVPState.PLACING_ANCHOR;
            anchorPos = null;
            ownsAnchor = false;
        }
    }

    public void disable() {
        removeOwnedAnchor();
        this.enabled = false;
        this.state = RAPVPState.IDLE;
        this.anchorPos = null;
        this.currentTarget = null;
        this.failedExplosionAttempts = 0;
        this.lastAnchorExplosionTime = 0L;
    }

    private void removeOwnedAnchor() {
        if (!ownsAnchor || anchorPos == null) {
            ownsAnchor = false;
            return;
        }
        if (level.getBlockState(anchorPos).getBlock() instanceof RespawnAnchorBlock) {
            level.setBlockAndUpdate(anchorPos, Blocks.AIR.defaultBlockState());
        }
        ownsAnchor = false;
    }

    public boolean isActive() {
        return enabled;
    }

    public RAPVPState getState() {
        return state;
    }

    public @Nullable BlockPos getCurrentAnchorPos() {
        return anchorPos;
    }

    public void setDifficulty(DifficultyLevel difficulty) {
        this.difficulty = difficulty;
        this.config = DifficultyProfileFactory.buildRAPVPConfig(difficulty);

        this.positionFinder.setConfig(config);
    }

    public DifficultyLevel getDifficulty() {
        return difficulty;
    }

    public RAPVPConfig getConfig() {
        return config;
    }

    public boolean hadRecentAnchorExplosion(long windowMs) {
        if (lastAnchorExplosionTime <= 0L) {
            return false;
        }
        return System.currentTimeMillis() - lastAnchorExplosionTime <= windowMs;
    }

    private boolean isHyperAggressiveDifficulty() {
        return difficulty == DifficultyLevel.GOD || difficulty == DifficultyLevel.HARD;
    }

    private int getActionCyclesForDifficulty() {
        if (difficulty == DifficultyLevel.GOD) {
            return 2;
        }
        if (difficulty == DifficultyLevel.HARD) {
            return 2;
        }
        return 1;
    }

    private Optional<BlockPos> findReusableAnchorNearTarget() {
        if (config == null || currentTarget == null) {
            return Optional.empty();
        }

        BlockPos targetPos = currentTarget.blockPosition();
        int horizontalRadius = Math.max(3, Math.min(config.getMaxDistance(), isHyperAggressiveDifficulty() ? 5 : 7));
        int verticalRadius = isHyperAggressiveDifficulty() ? 4 : 3;
        double maxTargetDistance = isHyperAggressiveDifficulty() ? 4.0D : 5.0D;

        BlockPos bestPos = null;
        double bestScore = Double.NEGATIVE_INFINITY;

        for (int dx = -horizontalRadius; dx <= horizontalRadius; dx++) {
            for (int dz = -horizontalRadius; dz <= horizontalRadius; dz++) {
                for (int dy = -verticalRadius; dy <= verticalRadius; dy++) {
                    BlockPos check = targetPos.offset(dx, dy, dz);
                    BlockState state = bot.level().getBlockState(check);
                    if (!(state.getBlock() instanceof RespawnAnchorBlock)) {
                        continue;
                    }

                    Vec3 anchorCenter = Vec3.atCenterOf(check);
                    double distanceToBot = anchorCenter.distanceTo(bot.position());
                    if (distanceToBot > config.getMaxDistance()) {
                        continue;
                    }
                    double distanceToTarget = anchorCenter.distanceTo(currentTarget.position());
                    if (distanceToTarget > maxTargetDistance) {
                        continue;
                    }

                    int charges = state.getValue(RespawnAnchorBlock.CHARGE);
                    if (charges <= 0 && !inventory.hasItem(net.minecraft.world.item.Items.GLOWSTONE)) {
                        continue;
                    }

                    double targetDamage =
                            ExplosionDamageEstimator.estimateAnchorDamage(level, anchorCenter, currentTarget);
                    double selfDamage = ExplosionDamageEstimator.estimateAnchorDamage(level, anchorCenter, bot);
                    if (selfDamage >= bot.getHealth() - 1.0F) {
                        continue;
                    }

                    double score = (targetDamage * 3.2D) - (selfDamage * 2.8D);
                    score += charges > 0 ? 8.0D : 2.0D;
                    score -= distanceToBot * 0.45D;
                    score -= Math.max(0.0D, distanceToTarget - 3.0D) * 3.2D;

                    if (score > bestScore) {
                        bestScore = score;
                        bestPos = check;
                    }
                }
            }
        }

        return Optional.ofNullable(bestPos);
    }
}
