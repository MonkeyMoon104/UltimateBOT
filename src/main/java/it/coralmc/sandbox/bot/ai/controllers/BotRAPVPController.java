package it.coralmc.sandbox.bot.ai.controllers;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class BotRAPVPController {

    private final Player bot;
    private final BotInventoryController inventory;
    private final BotRotationController rotation;
    private final BotCPVPController cpvp;
    private final BotEnderpearlController pearlController;

    private boolean enabled = false;
    private boolean anchorPlaced = false;
    private boolean isChargingAnchor = false;
    private boolean isWaitingExplosion = false;

    private BlockPos anchorPos = null;
    private Player currentTarget = null;
    private long anchorCooldownEnd = 0;
    private static final long ANCHOR_COOLDOWN_MS = 5000;

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
    }

    public void enable(Player target) {
        if (System.currentTimeMillis() < anchorCooldownEnd) return;

        this.enabled = true;
        this.anchorPlaced = false;
        this.isChargingAnchor = false;
        this.isWaitingExplosion = false;
        this.anchorPos = null;
        this.currentTarget = target;
    }

    public void tick() {
        if (!bot.isAlive() || currentTarget == null || !currentTarget.isAlive()) return;

        if (!enabled && System.currentTimeMillis() < anchorCooldownEnd) return;
        if (!enabled) return;

        if (!anchorPlaced) {
            Optional<BlockPos> posOpt = findBestAnchorPos(currentTarget);
            if (posOpt.isEmpty()) return;

            anchorPos = posOpt.get();

            if (!inventory.isHoldingAnchor()) {
                inventory.switchToAnchor();
                return;
            }

            rotation.lookAt(Vec3.atCenterOf(anchorPos));

            if (placeAnchor(anchorPos)) {
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

            rotation.lookAt(Vec3.atCenterOf(anchorPos));

            if (chargeAnchor(anchorPos)) {
                isChargingAnchor = false;
                isWaitingExplosion = true;
            } else {
                System.out.println("Fallimento caricamento anchor in: " + anchorPos);
                System.out.println("Stato anchor: " + state);
                System.out.println("Cariche attuali: " + state.getValue(RespawnAnchorBlock.CHARGE));
            }
            return;
        }

        if (isWaitingExplosion) {
            BlockState state = bot.level().getBlockState(anchorPos);
            if (state.getBlock() instanceof RespawnAnchorBlock) {
                inventory.switchToEmptySlot();

                rotation.lookAt(Vec3.atCenterOf(anchorPos));

                explodeAnchor(anchorPos);
            } else {
                isWaitingExplosion = false;

                pearlController.tryPearlToObsidianSide(anchorPos, currentTarget);
                cpvp.tick(currentTarget);

                anchorCooldownEnd = System.currentTimeMillis() + ANCHOR_COOLDOWN_MS;
                disable();
            }
        }
    }

    private Optional<BlockPos> findBestAnchorPos(Player target) {
        BlockPos targetPos = target.blockPosition();
        BlockPos best = null;
        int bestY = Integer.MAX_VALUE;

        for (int dy = -3; dy <= 0; dy++) {
            BlockPos check = targetPos.offset(0, dy, 0);
            if (bot.level().getBlockState(check).canBeReplaced()
                    && bot.level().getBlockState(check.below()).isSolid()) {
                if (check.getY() < bestY) {
                    best = check;
                    bestY = check.getY();
                }
            }
        }
        return Optional.ofNullable(best);
    }

    private boolean placeAnchor(BlockPos pos) {
        try {
            if (!hasLineOfSight(pos)) {
                return false;
            }

            ItemStack stack = inventory.getCurrentItem();
            if (stack == null || stack.getItem() != Items.RESPAWN_ANCHOR) return false;

            Direction bestFace = findBestPlacementFace(pos);
            if (bestFace == null) bestFace = Direction.UP;

            BlockPos adjacentPos = pos.relative(bestFace.getOpposite());

            BlockHitResult hitResult = new BlockHitResult(
                    Vec3.atCenterOf(adjacentPos).relative(bestFace, 0.5),
                    bestFace,
                    adjacentPos,
                    false
            );

            stack.useOn(new net.minecraft.world.item.context.UseOnContext(bot, InteractionHand.MAIN_HAND, hitResult));
            bot.swing(InteractionHand.MAIN_HAND);

            inventory.onItemUsed(BotInventoryController.ANCHOR_SLOT);

            return true;
        } catch (Exception e) {
            System.err.println("Errore nel piazzamento anchor: " + e.getMessage());
            return false;
        }
    }

    private boolean chargeAnchor(BlockPos anchorPos) {
        try {

            if (!hasLineOfSight(anchorPos)) {
                return false;
            }

            ItemStack stack = inventory.getCurrentItem();
            if (stack == null || stack.getItem() != Items.GLOWSTONE) {
                return false;
            }

            BlockState anchorState = bot.level().getBlockState(anchorPos);
            if (!(anchorState.getBlock() instanceof RespawnAnchorBlock)) {
                return false;
            }

            int currentCharges = anchorState.getValue(RespawnAnchorBlock.CHARGE);
            if (currentCharges >= 4) {
                return false;
            }

            int newCharges = Math.min(4, currentCharges + 1);
            BlockState newState = anchorState.setValue(RespawnAnchorBlock.CHARGE, newCharges);

            bot.level().setBlock(anchorPos, newState, 3);

            stack.shrink(1);

            bot.level().playSound(null, anchorPos,
                    net.minecraft.sounds.SoundEvents.RESPAWN_ANCHOR_CHARGE,
                    net.minecraft.sounds.SoundSource.BLOCKS,
                    1.0F, 1.0F);

            bot.swing(InteractionHand.MAIN_HAND);

            inventory.onItemUsed(BotInventoryController.GLOW_SLOT);
            return true;

        } catch (Exception e) {
            System.err.println("Errore nel caricamento anchor: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private void explodeAnchor(BlockPos anchorPos) {
        try {
            if (!hasLineOfSight(anchorPos)) {
                return;
            }

            BlockState anchorState = bot.level().getBlockState(anchorPos);
            if (!(anchorState.getBlock() instanceof RespawnAnchorBlock)) {
                return;
            }

            int currentCharges = anchorState.getValue(RespawnAnchorBlock.CHARGE);
            if (currentCharges == 0) {
                return;
            }


            inventory.switchToEmptySlot();

            BlockHitResult hitResult = new BlockHitResult(
                    Vec3.atCenterOf(anchorPos),
                    Direction.UP,
                    anchorPos,
                    false
            );

            try {
                InteractionResult result = anchorState.useWithoutItem(bot.level(), bot, hitResult);

                bot.swing(InteractionHand.MAIN_HAND);


                if (result.consumesAction()) {
                    return;
                }
            } catch (Exception e) {
                System.out.println("Errore interazione anchor: " + e.getMessage());
            }

            bot.level().removeBlock(anchorPos, false);

            bot.level().explode(
                    null,
                    anchorPos.getX() + 0.5,
                    anchorPos.getY() + 0.5,
                    anchorPos.getZ() + 0.5,
                    5.0F,
                    net.minecraft.world.level.Level.ExplosionInteraction.BLOCK
            );

        } catch (Exception e) {
            System.err.println("Errore generale nell'esplosione anchor: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Direction findBestPlacementFace(BlockPos targetPos) {
        Vec3 botPos = bot.position();

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos adjacentPos = targetPos.relative(direction.getOpposite());
            BlockState adjacentState = bot.level().getBlockState(adjacentPos);

            if (adjacentState.isSolid() && !adjacentState.isAir()) {
                double distance = botPos.distanceTo(Vec3.atCenterOf(adjacentPos));
                if (distance <= 6.5) return direction;
            }
        }

        for (Direction direction : new Direction[]{Direction.UP, Direction.DOWN}) {
            BlockPos adjacentPos = targetPos.relative(direction.getOpposite());
            BlockState adjacentState = bot.level().getBlockState(adjacentPos);

            if (adjacentState.isSolid() && !adjacentState.isAir()) {
                double distance = botPos.distanceTo(Vec3.atCenterOf(adjacentPos));
                if (distance <= 6.5) return direction;
            }
        }

        return null;
    }

    public void disable() {
        this.enabled = false;
        this.anchorPlaced = false;
        this.isChargingAnchor = false;
        this.isWaitingExplosion = false;
        this.anchorPos = null;
        this.currentTarget = null;
    }

    private boolean hasLineOfSight(BlockPos pos) {
        Vec3 botEyes = bot.getEyePosition(1.0F);
        Vec3 targetPos = Vec3.atCenterOf(pos);

        net.minecraft.world.level.ClipContext context = new net.minecraft.world.level.ClipContext(
                botEyes,
                targetPos,
                net.minecraft.world.level.ClipContext.Block.COLLIDER,
                net.minecraft.world.level.ClipContext.Fluid.NONE,
                bot
        );

        net.minecraft.world.phys.BlockHitResult result = level.clip(context);
        return result.getType() == net.minecraft.world.phys.HitResult.Type.MISS ||
                result.getBlockPos().equals(pos);
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