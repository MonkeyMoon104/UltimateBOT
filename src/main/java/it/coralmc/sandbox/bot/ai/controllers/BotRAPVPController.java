package it.coralmc.sandbox.bot.ai.controllers;

import it.coralmc.sandbox.bot.ai.controllers.rapvp.AnchorPlacer;
import it.coralmc.sandbox.bot.ai.controllers.rapvp.AnchorCharger;
import it.coralmc.sandbox.bot.ai.controllers.rapvp.AnchorExploder;
import it.coralmc.sandbox.bot.ai.controllers.rapvp.AnchorPositionFinder;
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
    private boolean anchorPlaced = false;
    private boolean isChargingAnchor = false;
    private boolean isWaitingExplosion = false;

    private BlockPos anchorPos = null;
    private Player currentTarget = null;

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
        this.anchorPlaced = false;
        this.isChargingAnchor = false;
        this.isWaitingExplosion = false;
        this.anchorPos = null;
        this.currentTarget = target;
    }

    public void tick() {
        if (!bot.isAlive() || currentTarget == null || !currentTarget.isAlive()) return;

        if (!enabled) return;

        if (!anchorPlaced) {
            Optional<BlockPos> posOpt = positionFinder.findBestAnchorPos(currentTarget);
            if (posOpt.isEmpty()) return;

            anchorPos = posOpt.get();

            if (!inventory.isHoldingAnchor()) {
                inventory.switchToAnchor();
                return;
            }

            rotation.lookAt(net.minecraft.world.phys.Vec3.atCenterOf(anchorPos));

            if (anchorPlacer.placeAnchor(anchorPos)) {
                anchorPlaced = true;
                isChargingAnchor = true;
            }
            return;
        }

        if (isChargingAnchor) {
            BlockState state = bot.level().getBlockState(anchorPos);
            if (!(state.getBlock() instanceof RespawnAnchorBlock)) {
                anchorPlaced = false;
                isChargingAnchor = false;
                return;
            }

            if (!inventory.isHoldingGlow()) {
                inventory.switchToGlow();
                return;
            }

            rotation.lookAt(net.minecraft.world.phys.Vec3.atCenterOf(anchorPos));

            if (anchorCharger.chargeAnchor(anchorPos)) {
                isChargingAnchor = false;
                isWaitingExplosion = true;
            }
            return;
        }

        if (isWaitingExplosion) {
            BlockState state = bot.level().getBlockState(anchorPos);
            if (state.getBlock() instanceof RespawnAnchorBlock) {
                inventory.switchToEmptySlot();

                rotation.lookAt(net.minecraft.world.phys.Vec3.atCenterOf(anchorPos));

                anchorExploder.explodeAnchor(anchorPos);
            } else {
                isWaitingExplosion = false;

                pearlController.tryPearlToObsidianSide(anchorPos, currentTarget);
                cpvp.tick(currentTarget);

                isWaitingExplosion = false;
                anchorPlaced = false;
                isChargingAnchor = false;
                anchorPos = null;
            }
        }
    }

    public void disable() {
        this.enabled = false;
        this.anchorPlaced = false;
        this.isChargingAnchor = false;
        this.isWaitingExplosion = false;
        this.anchorPos = null;
        this.currentTarget = null;
    }

    public boolean isActive() {
        return enabled;
    }

    public boolean isAnchorPlaced() {
        return anchorPlaced;
    }

    public boolean isChargingAnchor() {
        return isChargingAnchor;
    }

    public boolean isWaitingExplosion() {
        return isWaitingExplosion;
    }

    public BlockPos getCurrentAnchorPos() {
        return anchorPos;
    }
}