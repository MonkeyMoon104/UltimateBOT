package com.monkey.ultimatebot.bot.ai.controllers.rapvp;

import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.bot.ai.controllers.combat.ExplosionDamageEstimator;
import com.monkey.ultimatebot.bot.ai.controllers.enderpearl.BotEnderpearlController;
import com.monkey.ultimatebot.bot.ai.controllers.inventory.BotInventoryController;
import com.monkey.ultimatebot.bot.ai.controllers.rapvp.helper.AnchorCharger;
import com.monkey.ultimatebot.bot.ai.controllers.rapvp.helper.AnchorExploder;
import com.monkey.ultimatebot.bot.ai.controllers.rapvp.helper.AnchorPlacer;
import com.monkey.ultimatebot.bot.ai.controllers.rapvp.helper.AnchorPositionFinder;
import com.monkey.ultimatebot.bot.ai.controllers.rapvp.helper.RAPVPState;
import com.monkey.ultimatebot.bot.ai.controllers.rotation.BotRotationController;
import com.monkey.ultimatebot.bot.ai.difficulty.DifficultyLevel;
import com.monkey.ultimatebot.bot.ai.difficulty.DifficultyProfileFactory;
import com.monkey.ultimatebot.bot.ai.difficulty.configs.RAPVPConfig;
import com.monkey.ultimatebot.common.model.PlatformCapability;
import com.monkey.ultimatebot.nms.NMSBridgeManager;
import java.util.Objects;
import java.util.Optional;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.RespawnAnchor;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockVector;
import org.jspecify.annotations.Nullable;

@SuppressWarnings("NullAway")
public final class BotRAPVPController {

    private final ITrainingBot bot;
    private final BotInventoryController inventory;
    private final BotEnderpearlController pearlController;
    private final AnchorPlacer anchorPlacer;
    private final AnchorCharger anchorCharger;
    private final AnchorExploder anchorExploder;
    private final AnchorPositionFinder positionFinder;

    private boolean enabled = false;
    private RAPVPState state = RAPVPState.IDLE;
    private @Nullable BlockVector anchorPos;
    private @Nullable Player currentTarget;
    private DifficultyLevel difficulty = DifficultyLevel.NORMAL;
    private RAPVPConfig config = DifficultyProfileFactory.buildRAPVPConfig(difficulty);
    private int failedExplosionAttempts = 0;
    private long lastAnchorExplosionTime = 0L;
    private boolean ownsAnchor;

    public BotRAPVPController(
            ITrainingBot bot,
            BotInventoryController inventory,
            BotRotationController rotation,
            BotEnderpearlController pearlController) {
        this.bot = bot;
        this.inventory = inventory;
        this.pearlController = pearlController;
        this.anchorPlacer = new AnchorPlacer(bot, inventory, rotation);
        this.anchorCharger = new AnchorCharger(bot, inventory, rotation);
        this.anchorExploder = new AnchorExploder(bot, inventory, rotation);
        this.positionFinder = new AnchorPositionFinder(bot);
        setDifficulty(DifficultyLevel.NORMAL);
    }

    public void enable(Player target) {
        if (!NMSBridgeManager.get().supports(PlatformCapability.RESPAWN_ANCHOR)) {
            disable();
            return;
        }
        this.enabled = true;
        this.state = RAPVPState.PLACING_ANCHOR;
        this.anchorPos = null;
        this.currentTarget = target;
        this.failedExplosionAttempts = 0;
        this.lastAnchorExplosionTime = 0L;
        this.ownsAnchor = false;
    }

    public void tick() {
        if (!bot.isAlive() || currentTarget == null || currentTarget.isDead() || !enabled) {
            return;
        }
        for (int i = 0; i < getActionCyclesForDifficulty(); i++) {
            if (!enabled || currentTarget == null || currentTarget.isDead()) break;
            switch (state) {
                case PLACING_ANCHOR -> handlePlacingAnchor();
                case CHARGING_ANCHOR -> handleChargingAnchor();
                case WAITING_EXPLOSION -> handleWaitingExplosion();
                case IDLE -> {}
            }
        }
    }

    private void handlePlacingAnchor() {
        Optional<BlockVector> reusableAnchor = findReusableAnchorNearTarget();
        if (reusableAnchor.isPresent()) {
            anchorPos = reusableAnchor.get();
            ownsAnchor = false;
            RespawnAnchor anchorData = anchorData(anchorPos);
            int charges = anchorData == null ? 0 : anchorData.getCharges();
            state = charges > 0 ? RAPVPState.WAITING_EXPLOSION : RAPVPState.CHARGING_ANCHOR;
            return;
        }
        Player target = Objects.requireNonNull(currentTarget, "currentTarget");
        Optional<BlockVector> posOpt = positionFinder.findBestAnchorPos(target);
        if (posOpt.isEmpty()) {
            if (!isHyperAggressiveDifficulty() && pearlController.canUseEnderpearl() && bot.distanceTo(target) > 10.0D) {
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
        } else if (anchorData(anchorPos) != null) {
            state = RAPVPState.CHARGING_ANCHOR;
        }
    }

    private void handleChargingAnchor() {
        if (anchorPos == null) {
            state = RAPVPState.PLACING_ANCHOR;
            return;
        }
        RespawnAnchor anchorData = anchorData(anchorPos);
        if (anchorData == null) {
            state = RAPVPState.PLACING_ANCHOR;
            anchorPos = null;
            return;
        }
        if (anchorData.getCharges() > 0) {
            state = RAPVPState.WAITING_EXPLOSION;
            return;
        }
        if (!inventory.isHoldingGlow()) {
            inventory.switchToGlow();
            return;
        }
        if (anchorCharger.chargeAnchor(anchorPos)) {
            state = RAPVPState.WAITING_EXPLOSION;
        }
    }

    private void handleWaitingExplosion() {
        if (anchorPos == null) {
            state = RAPVPState.PLACING_ANCHOR;
            return;
        }
        RespawnAnchor anchorData = anchorData(anchorPos);
        if (anchorData == null) {
            resetAnchor();
            return;
        }
        if (anchorData.getCharges() <= 0) {
            state = RAPVPState.CHARGING_ANCHOR;
            return;
        }
        inventory.switchToEmptySlot();
        boolean exploded = anchorExploder.explodeAnchor(anchorPos);
        if (exploded) {
            failedExplosionAttempts = 0;
            lastAnchorExplosionTime = System.currentTimeMillis();
            resetAnchor();
        } else if (++failedExplosionAttempts >= (isHyperAggressiveDifficulty() ? 3 : 4)) {
            failedExplosionAttempts = 0;
            resetAnchor();
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

    private void resetAnchor() {
        state = RAPVPState.PLACING_ANCHOR;
        anchorPos = null;
        ownsAnchor = false;
    }

    private void removeOwnedAnchor() {
        if (!ownsAnchor || anchorPos == null) {
            ownsAnchor = false;
            return;
        }
        if (anchorData(anchorPos) != null) {
            blockAt(anchorPos).setType(Material.AIR, false);
        }
        ownsAnchor = false;
    }

    public boolean isActive() { return enabled; }
    public RAPVPState getState() { return state; }
    public @Nullable BlockVector getCurrentAnchorPos() { return anchorPos; }

    public void setDifficulty(DifficultyLevel difficulty) {
        this.difficulty = difficulty;
        this.config = DifficultyProfileFactory.buildRAPVPConfig(difficulty);
        this.positionFinder.setConfig(config);
    }

    public DifficultyLevel getDifficulty() { return difficulty; }
    public RAPVPConfig getConfig() { return config; }

    public boolean hadRecentAnchorExplosion(long windowMs) {
        return lastAnchorExplosionTime > 0L && System.currentTimeMillis() - lastAnchorExplosionTime <= windowMs;
    }

    private boolean isHyperAggressiveDifficulty() {
        return difficulty == DifficultyLevel.GOD || difficulty == DifficultyLevel.HARD;
    }

    private int getActionCyclesForDifficulty() {
        return difficulty == DifficultyLevel.GOD || difficulty == DifficultyLevel.HARD ? 2 : 1;
    }

    private Optional<BlockVector> findReusableAnchorNearTarget() {
        if (config == null || currentTarget == null) return Optional.empty();
        int targetX = currentTarget.getLocation().getBlockX();
        int targetY = currentTarget.getLocation().getBlockY();
        int targetZ = currentTarget.getLocation().getBlockZ();
        int horizontalRadius = Math.max(3, Math.min(config.getMaxDistance(), isHyperAggressiveDifficulty() ? 5 : 7));
        int verticalRadius = isHyperAggressiveDifficulty() ? 4 : 3;
        double maxTargetDistance = isHyperAggressiveDifficulty() ? 4.0D : 5.0D;
        BlockVector bestPos = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        for (int dx = -horizontalRadius; dx <= horizontalRadius; dx++) {
            for (int dz = -horizontalRadius; dz <= horizontalRadius; dz++) {
                for (int dy = -verticalRadius; dy <= verticalRadius; dy++) {
                    BlockVector check = new BlockVector(targetX + dx, targetY + dy, targetZ + dz);
                    RespawnAnchor data = anchorData(check);
                    if (data == null) continue;
                    org.bukkit.Location center = centerOf(check);
                    if (center.distance(bot.getLocation()) > config.getMaxDistance()) continue;
                    if (center.distance(currentTarget.getLocation()) > maxTargetDistance) continue;
                    if (data.getCharges() <= 0 && !inventory.hasItem(Material.GLOWSTONE)) continue;
                    double targetDamage = ExplosionDamageEstimator.estimateAnchorDamage(center, currentTarget);
                    double selfDamage = ExplosionDamageEstimator.estimateAnchorDamage(center, bot.asBukkitPlayer());
                    if (selfDamage >= bot.healthValue() - 1.0D) continue;
                    double score = (targetDamage * 3.2D) - (selfDamage * 2.8D) + (data.getCharges() > 0 ? 8.0D : 2.0D);
                    if (score > bestScore) {
                        bestScore = score;
                        bestPos = check;
                    }
                }
            }
        }
        return Optional.ofNullable(bestPos);
    }

    private @Nullable RespawnAnchor anchorData(BlockVector pos) {
        return blockAt(pos).getBlockData() instanceof RespawnAnchor anchor ? anchor : null;
    }

    private Block blockAt(BlockVector pos) {
        return bot.getWorld().getBlockAt(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ());
    }

    private org.bukkit.Location centerOf(BlockVector pos) {
        return new org.bukkit.Location(bot.getWorld(), pos.getBlockX() + 0.5D, pos.getBlockY() + 0.5D, pos.getBlockZ() + 0.5D);
    }
}
