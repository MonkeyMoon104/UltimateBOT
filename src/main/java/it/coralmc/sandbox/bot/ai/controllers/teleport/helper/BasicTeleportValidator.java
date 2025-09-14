package it.coralmc.sandbox.bot.ai.controllers.teleport.helper;

import it.coralmc.sandbox.bot.ai.controllers.teleport.helper.inter.ITeleportValidator;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class BasicTeleportValidator implements ITeleportValidator {

    @Override
    public boolean isSafePosition(Player bot, BlockPos pos) {
        Level level = bot.level();

        if (!level.getBlockState(pos.below()).isSolidRender()) return false;

        if (level.getBlockState(pos).isSolidRender()) return false;
        if (level.getBlockState(pos.above()).isSolidRender()) return false;

        return true;
    }

    @Override
    public boolean isSuffocationDamage(Player bot) {
        BlockPos botPos = bot.blockPosition();
        Level level = bot.level();

        if (level.getBlockState(botPos).isSolidRender()) return true;
        if (level.getBlockState(botPos.above()).isSolidRender()) return true;

        return false;
    }
}
