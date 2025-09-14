package it.coralmc.sandbox.bot.ai.controllers.teleport.helper;

import it.coralmc.sandbox.bot.ai.controllers.teleport.helper.inter.ITeleportStrategy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class BesideTargetStrategy implements ITeleportStrategy {

    private final double offset;

    public BesideTargetStrategy(double offset) {
        this.offset = offset;
    }

    @Override
    public Vec3 findTeleportPosition(Player bot, Player target) {
        if (target == null) return null;
        Vec3 pos = target.position();
        return pos.add(offset, 0, 0);
    }
}
