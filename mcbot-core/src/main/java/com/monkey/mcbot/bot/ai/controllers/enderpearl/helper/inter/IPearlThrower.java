package com.monkey.mcbot.bot.ai.controllers.enderpearl.helper.inter;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public interface IPearlThrower {

    void throwEnderpearl(Player bot, Vec3 targetPos);
}
