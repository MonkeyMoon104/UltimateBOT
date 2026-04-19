package com.monkey.mcbot.bot.ai.controllers.rapvp;

import com.monkey.mcbot.bot.ai.controllers.combat.ExplosionDamageEstimator;
import com.monkey.mcbot.bot.ai.controllers.enderpearl.BotEnderpearlController;
import com.monkey.mcbot.bot.ai.controllers.inventory.BotInventoryController;
import com.monkey.mcbot.bot.ai.controllers.rapvp.helper.*;
import com.monkey.mcbot.bot.ai.controllers.rotation.BotRotationController;
import com.monkey.mcbot.bot.ai.rank.BotRank;
import com.monkey.mcbot.bot.ai.rank.RankCoordinator;
import com.monkey.mcbot.bot.ai.rank.configs.RAPVPConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class BotRAPVPController {

    private final Player bot;
    private final Level level;
    private final BotInventoryController inventory;
    private final BotRotationController rotation;
    private final BotEnderpearlController pearlController;
    private final AnchorPlacer anchorPlacer;
    private final AnchorCharger anchorCharger;
    private final AnchorExploder anchorExploder;
    private final AnchorPositionFinder positionFinder;

    private boolean enabled = false;
    private RAPVPState state = RAPVPState.IDLE;

    private BlockPos anchorPos = null;
    private Player currentTarget = null;
    private BotRank rank;
    private RAPVPConfig config;
    private int failedExplosionAttempts = 0;
    private long lastAnchorExplosionTime = 0L;

    public BotRAPVPController(Player bot,
                              BotInventoryController inventory,
                              BotRotationController rotation,
                              BotEnderpearlController pearlController) {
        this.bot = bot;
        this.inventory = inventory;
        this.rotation = rotation;
        this.pearlController = pearlController;
        this.level = bot.level();

        this.anchorPlacer = new AnchorPlacer(bot, inventory, rotation, level);
        this.anchorCharger = new AnchorCharger(bot, inventory, rotation, level);
        this.anchorExploder = new AnchorExploder(bot, inventory, rotation, level);
        this.positionFinder = new AnchorPositionFinder(bot, level);
    }

    public void enable(Player target) {
        this.enabled = true;
        this.state = RAPVPState.PLACING_ANCHOR;
        this.anchorPos = null;
        this.currentTarget = target;
        this.failedExplosionAttempts = 0;
        this.lastAnchorExplosionTime = 0L;
    }

    public void tick() {
        if (!bot.isAlive() || currentTarget == null || !currentTarget.isAlive() || !enabled) {
            return;
        }

        int actionCycles = getActionCyclesForRank();
        for (int i = 0; i < actionCycles; i++) {
            if (!enabled || currentTarget == null || !currentTarget.isAlive()) {
                break;
            }
            switch (state) {
                case PLACING_ANCHOR -> handlePlacingAnchor();
                case CHARGING_ANCHOR -> handleChargingAnchor();
                case WAITING_EXPLOSION -> handleWaitingExplosion();
                case IDLE -> {
                }
            }
        }
    }

    private void handlePlacingAnchor() {
        Optional<BlockPos> reusableAnchor = findReusableAnchorNearTarget();
        if (reusableAnchor.isPresent()) {
            anchorPos = reusableAnchor.get();
            BlockState reusableState = bot.level().getBlockState(anchorPos);
            int charges = reusableState.getValue(RespawnAnchorBlock.CHARGE);
            state = charges > 0 ? RAPVPState.WAITING_EXPLOSION : RAPVPState.CHARGING_ANCHOR;
            return;
        }

        Optional<BlockPos> posOpt = positionFinder.findBestAnchorPos(currentTarget);
        if (posOpt.isEmpty()) {
            if (!isHyperAggressiveRank()
                    && pearlController.canUseEnderpearl()
                    && bot.distanceTo(currentTarget) > 10.0D) {
                pearlController.tryUseEnderpearl(currentTarget);
            }
            return;
        }

        anchorPos = posOpt.get();

        if (!inventory.isHoldingAnchor()) {
            inventory.switchToAnchor();
            return;
        }

        if (anchorPlacer.placeAnchor(anchorPos)) {
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
            } else {
                failedExplosionAttempts++;
                int retryLimit = isHyperAggressiveRank() ? 3 : 4;
                if (failedExplosionAttempts >= retryLimit) {
                    failedExplosionAttempts = 0;
                    state = RAPVPState.PLACING_ANCHOR;
                    anchorPos = null;
                }
            }
        } else {
            failedExplosionAttempts = 0;
            state = RAPVPState.PLACING_ANCHOR;
            anchorPos = null;
        }
    }

    public void disable() {
        this.enabled = false;
        this.state = RAPVPState.IDLE;
        this.anchorPos = null;
        this.currentTarget = null;
        this.failedExplosionAttempts = 0;
        this.lastAnchorExplosionTime = 0L;
    }

    public boolean isActive() {
        return enabled;
    }

    public RAPVPState getState() {
        return state;
    }

    public BlockPos getCurrentAnchorPos() {
        return anchorPos;
    }

    public void setRank(BotRank rank) {
        this.rank = rank;
        this.config = RankCoordinator.buildRAPVPConfig(rank);

        this.positionFinder.setConfig(config);
    }

    public BotRank getRank() {
        return rank;
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

    private boolean isHyperAggressiveRank() {
        return rank == BotRank.GOD || rank == BotRank.HARD;
    }

    private int getActionCyclesForRank() {
        if (rank == BotRank.GOD) {
            return 2;
        }
        if (rank == BotRank.HARD) {
            return 2;
        }
        return 1;
    }

    private Optional<BlockPos> findReusableAnchorNearTarget() {
        if (config == null || currentTarget == null) {
            return Optional.empty();
        }

        BlockPos targetPos = currentTarget.blockPosition();
        int horizontalRadius = Math.max(3, Math.min(config.getMaxDistance(), isHyperAggressiveRank() ? 5 : 7));
        int verticalRadius = isHyperAggressiveRank() ? 4 : 3;
        double maxTargetDistance = isHyperAggressiveRank() ? 4.0D : 5.0D;

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

                    double targetDamage = ExplosionDamageEstimator.estimateAnchorDamage(level, anchorCenter, currentTarget);
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
