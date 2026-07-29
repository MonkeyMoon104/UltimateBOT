package com.monkey.mcbot.bot.ai.controllers.enderpearl.helper;

import com.monkey.mcbot.bot.ai.controllers.enderpearl.helper.inter.IPearlThrower;
import com.monkey.mcbot.nms.NMSBridgeManager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class PearlThrower implements IPearlThrower {

    @Override
    public void throwEnderpearl(Player bot, Vec3 targetPos) {
        NMSBridgeManager.get().throwEnderpearl(bot, targetPos);
    }
}
