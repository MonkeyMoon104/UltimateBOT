package com.monkey.mcbot.bot.ai.controllers.rotation.helper.inter;

import net.minecraft.world.entity.player.Player;

public interface IRotationApplier {

    void applyRotation(Player bot, float yaw, float pitch);

    void resetRotation(Player bot);
}