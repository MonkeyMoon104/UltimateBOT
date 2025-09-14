package it.coralmc.sandbox.bot.ai.controllers.teleport.helper.inter;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;

public interface ITeleportValidator {
    boolean isSafePosition(Player bot, BlockPos pos);
    boolean isSuffocationDamage(Player bot);
}
