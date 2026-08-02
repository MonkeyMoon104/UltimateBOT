package com.monkey.ultimatebot.bot.ai.controllers.rapvp.helper;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.bot.ai.controllers.inventory.BotInventoryController;
import com.monkey.ultimatebot.bot.ai.controllers.rotation.BotRotationController;
import com.monkey.ultimatebot.logging.UltimateBotLogging;
import com.monkey.ultimatebot.nms.NMSBridgeManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class AnchorCharger {

    private final Player bot;
    private final BotInventoryController inventory;
    private final BotRotationController rotation;
    private final Level level;

    public AnchorCharger(Player bot, BotInventoryController inventory, BotRotationController rotation, Level level) {
        this.bot = bot;
        this.inventory = inventory;
        this.rotation = rotation;
        this.level = level;
    }

    public boolean chargeAnchor(BlockPos anchorPos) {
        try {

            if (!hasLineOfSight(anchorPos)) {
                return false;
            }

            ItemStack stack = inventory.getCurrentItem();
            if (stack == null || !Items.GLOWSTONE.equals(stack.getItem())) {
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

            BlockState newState = anchorState.setValue(RespawnAnchorBlock.CHARGE, 4);

            bot.level().setBlock(anchorPos, newState, 3);

            NMSBridgeManager.get()
                    .playSound(
                            bot.level(),
                            anchorPos,
                            net.minecraft.sounds.SoundEvents.RESPAWN_ANCHOR_CHARGE,
                            net.minecraft.sounds.SoundSource.BLOCKS,
                            1.0F,
                            1.0F);

            rotation.lookAt(Vec3.atLowerCornerOf(anchorPos));

            bot.swing(InteractionHand.MAIN_HAND);

            inventory.onItemUsed(BotInventoryController.GLOW_SLOT);
            return true;

        } catch (Exception e) {
            UltimateBotLogging.warn(
                    UltimateBot.getInstance().getLogger(), "Combat", "Anchor charge failed -> " + e.getMessage());
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
                bot);

        net.minecraft.world.phys.BlockHitResult result = level.clip(context);
        return result.getType() == net.minecraft.world.phys.HitResult.Type.MISS
                || result.getBlockPos().equals(pos);
    }
}
