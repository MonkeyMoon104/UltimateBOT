package com.monkey.ultimatebot.bot.ai.controllers.enderpearl.helper;

import com.monkey.ultimatebot.bot.ai.controllers.enderpearl.helper.inter.IPearlThrower;
import com.monkey.ultimatebot.nms.NMSBridgeManager;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class PearlThrower implements IPearlThrower {

    @Override
    public void throwEnderpearl(Player bot, Vector targetPos) {
        NMSBridgeManager.get().throwEnderpearl(bot, targetPos);
    }
}
