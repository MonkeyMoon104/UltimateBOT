package com.monkey.mcbot.bot.ai.controllers.rapvp.helper;

import com.monkey.mcbot.MinecraftBot;
import com.monkey.mcbot.bot.ai.controllers.inventory.BotInventoryController;
import com.monkey.mcbot.bot.ai.controllers.rotation.BotRotationController;
import com.monkey.mcbot.nms.NMSBridgeManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class AnchorExploder {

    private final Player bot;
    private final BotInventoryController inventory;
    private final BotRotationController rotation;
    private final Level level;

    public AnchorExploder(Player bot, BotInventoryController inventory, BotRotationController rotation, Level level) {
        this.bot = bot;
        this.inventory = inventory;
        this.rotation = rotation;
        this.level = level;
    }

    public boolean explodeAnchor(BlockPos anchorPos) {
        try {
            if (!hasLineOfSight(anchorPos)) {
                double distanceToAnchor = bot.position().distanceTo(Vec3.atCenterOf(anchorPos));
                if (distanceToAnchor > 3.2D) {
                    return false;
                }
            }

            BlockState anchorState = bot.level().getBlockState(anchorPos);
            if (!(anchorState.getBlock() instanceof RespawnAnchorBlock)) {
                return false;
            }

            int currentCharges = anchorState.getValue(RespawnAnchorBlock.CHARGE);
            if (currentCharges == 0) {
                return false;
            }

            inventory.switchToEmptySlot();
            rotation.lookAt(Vec3.atCenterOf(anchorPos));

            if (tryVanillaExplosion(anchorPos, anchorState)) {
                return true;
            }

            return tryManualExplosion(anchorPos);

        } catch (Exception e) {
            MinecraftBot.getInstance().getLogger().warning("Errore durante esplosione anchor: " + e.getMessage());
            return false;
        }
    }

    private boolean tryVanillaExplosion(BlockPos anchorPos, BlockState anchorState) {
        try {
            BlockHitResult hitResult = new BlockHitResult(
                    Vec3.atCenterOf(anchorPos),
                    Direction.UP,
                    anchorPos,
                    false
            );

            InteractionResult result = anchorState.useWithoutItem(bot.level(), bot, hitResult);
            bot.swing(InteractionHand.MAIN_HAND);

            return result.consumesAction();
        } catch (Exception e) {
            return false;
        }
    }

    private boolean tryManualExplosion(BlockPos anchorPos) {
        System.out.println("FALLBACK -> USO ESPLOSIONE ANCHOR MANUALE");
        try {
            bot.level().removeBlock(anchorPos, false);

            NMSBridgeManager.get().explode(
                    bot.level(),
                    bot,
                    anchorPos.getX() + 0.5,
                    anchorPos.getY() + 0.5,
                    anchorPos.getZ() + 0.5,
                    3.5F
            );

            bot.swing(InteractionHand.MAIN_HAND);
            return true;

        } catch (Exception e) {
            MinecraftBot.getInstance().getLogger().warning("Errore durante esplosione manuale: " + e.getMessage());
            return false;
        }
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
}
