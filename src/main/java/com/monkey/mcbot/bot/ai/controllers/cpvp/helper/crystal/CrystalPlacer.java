package com.monkey.mcbot.bot.ai.controllers.cpvp.helper.crystal;

import com.monkey.mcbot.bot.ai.controllers.inventory.BotInventoryController;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class CrystalPlacer {

    private final Player bot;
    private final BotInventoryController inventoryController;
    private final Level level;

    public CrystalPlacer(Player bot, BotInventoryController inventoryController, Level level) {
        this.bot = bot;
        this.inventoryController = inventoryController;
        this.level = level;
    }

    public boolean placeCrystal(BlockPos pos) {
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

                inventoryController.onItemUsed(BotInventoryController.CRYSTAL_SLOT);

                return true;
            }
        } catch (Exception e) {
            System.err.println("Errore nel piazzamento crystal: " + e.getMessage());
        }

        return false;
    }

    public boolean hasLineOfSight(BlockPos pos) {
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