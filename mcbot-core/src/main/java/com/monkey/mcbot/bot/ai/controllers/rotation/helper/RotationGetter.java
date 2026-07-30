package com.monkey.mcbot.bot.ai.controllers.rotation.helper;

import com.monkey.mcbot.bot.ai.controllers.rotation.helper.inter.IRotationGetter;
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
