package it.coralmc.sandbox.bot.ai.controllers.rapvp;

import it.coralmc.sandbox.bot.ai.controllers.enderpearl.BotEnderpearlController;
import it.coralmc.sandbox.bot.ai.controllers.inventory.BotInventoryController;
import it.coralmc.sandbox.bot.ai.controllers.rapvp.helper.*;
import it.coralmc.sandbox.bot.ai.controllers.rotation.BotRotationController;
import it.coralmc.sandbox.bot.ai.controllers.cpvp.BotCPVPController;
import it.coralmc.sandbox.bot.ai.rank.BotRank;
import it.coralmc.sandbox.bot.ai.rank.RAPVPConfig;
import it.coralmc.sandbox.bot.ai.rank.RankCoordinator;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

public class BotRAPVPController {

    private final Player bot;
    private final Level level;
    private final BotInventoryController inventory;
    private final BotRotationController rotation;
    private final BotCPVPController cpvp;
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

    public BotRAPVPController(Player bot,
                              BotInventoryController inventory,
                              BotRotationController rotation,
                              BotCPVPController cpvp,
                              BotEnderpearlController pearlController) {
        this.bot = bot;
        this.inventory = inventory;
        this.rotation = rotation;
        this.cpvp = cpvp;
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
    }

    public void tick() {
        if (!bot.isAlive() || currentTarget == null || !currentTarget.isAlive() || !enabled) {
            return;
        }

        switch (state) {
            case PLACING_ANCHOR -> handlePlacingAnchor();
            case CHARGING_ANCHOR -> handleChargingAnchor();
            case WAITING_EXPLOSION -> handleWaitingExplosion();
            case IDLE -> {  }
        }
    }

    private void handlePlacingAnchor() {
        Optional<BlockPos> posOpt = positionFinder.findBestAnchorPos(currentTarget);
        if (posOpt.isEmpty()) return;

        anchorPos = posOpt.get();

        if (!inventory.isHoldingAnchor()) {
            inventory.switchToAnchor();
            return;
        }

        if (anchorPlacer.placeAnchor(anchorPos)) {
            state = RAPVPState.CHARGING_ANCHOR;
        }
    }

    private void handleChargingAnchor() {
        BlockState stateBlock = bot.level().getBlockState(anchorPos);
        if (!(stateBlock.getBlock() instanceof RespawnAnchorBlock)) {
            state = RAPVPState.PLACING_ANCHOR;
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
        BlockState stateBlock = bot.level().getBlockState(anchorPos);
        if (stateBlock.getBlock() instanceof RespawnAnchorBlock) {
            inventory.switchToEmptySlot();
            anchorExploder.explodeAnchor(anchorPos);
        } else {
            pearlController.tryPearlToObsidianSide(anchorPos, currentTarget);
            cpvp.tick(currentTarget);

            state = RAPVPState.PLACING_ANCHOR;
            anchorPos = null;
        }
    }

    public void disable() {
        this.enabled = false;
        this.state = RAPVPState.IDLE;
        this.anchorPos = null;
        this.currentTarget = null;
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
}
