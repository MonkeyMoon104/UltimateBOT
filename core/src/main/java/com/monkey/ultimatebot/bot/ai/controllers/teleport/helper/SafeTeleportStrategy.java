package com.monkey.ultimatebot.bot.ai.controllers.teleport.helper;

import com.monkey.ultimatebot.bot.ai.controllers.teleport.helper.inter.ITeleportStrategy;
import com.monkey.ultimatebot.bot.ai.controllers.teleport.helper.inter.ITeleportValidator;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class SafeTeleportStrategy implements ITeleportStrategy {

    private final ITeleportValidator validator;

    public SafeTeleportStrategy(ITeleportValidator validator) {
        this.validator = validator;
    }

    @Override
    public Vec3 findTeleportPosition(Player bot, Player target) {
        if (target == null) return null;
        Vec3 base = target.position();
        Vec3[] offsets = {
            base.add(2, 0, 0),
            base.add(-2, 0, 0),
            base.add(0, 0, 2),
            base.add(0, 0, -2),
            base.add(2, 0, 2),
            base.add(-2, 0, -2)
        };

        for (Vec3 candidate : offsets) {
            BlockPos pos = BlockPos.containing(candidate);
            if (validator.isSafePosition(bot, pos)) {
                return Vec3.atCenterOf(pos);
            }
        }

        return null;
    }
}
