package it.coralmc.sandbox.bot.ai.controllers.rotation;

import it.coralmc.sandbox.bot.ai.controllers.rotation.inter.IRotationGetter;
import net.minecraft.world.entity.player.Player;

public class RotationGetter implements IRotationGetter {

    @Override
    public float getCurrentYaw(Player bot) {
        return bot.getYRot();
    }

    @Override
    public float getCurrentPitch(Player bot) {
        return bot.getXRot();
    }
}