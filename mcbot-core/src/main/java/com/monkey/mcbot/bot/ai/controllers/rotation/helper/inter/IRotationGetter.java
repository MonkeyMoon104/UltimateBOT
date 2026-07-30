package com.monkey.mcbot.bot.ai.controllers.rotation.helper.inter;

import net.minecraft.world.entity.player.Player;

public interface IRotationGetter {

    float getCurrentYaw(Player bot);

    float getCurrentPitch(Player bot);
}
