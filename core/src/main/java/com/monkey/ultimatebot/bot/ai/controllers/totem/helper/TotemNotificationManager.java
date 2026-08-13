package com.monkey.ultimatebot.bot.ai.controllers.totem.helper;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.bot.ai.controllers.totem.helper.interf.ITotemNotificationManager;
import com.monkey.ultimatebot.utils.ChatColorUtils;

public class TotemNotificationManager implements ITotemNotificationManager {
    private final UltimateBot plugin;
    private boolean warnedOutOfTotems = false;

    public TotemNotificationManager(UltimateBot plugin) {
        this.plugin = plugin;
    }

    @Override
    public void sendTotemWarning(ITrainingBot trainingBot) {
        org.bukkit.entity.Player player = trainingBot.getTargetPlayer();
        if (player != null && player.isOnline()) {
            String msg = plugin.getLangString("messages.totem-finish", "[%botname%] Running out of totems");
            String rawName = plugin.getConfig().getString("bot.name", "CrystalBot");
            String botName = rawName.replace("%player%", player.getName());
            msg = msg.replace("%botname%", botName);
            player.sendMessage(ChatColorUtils.translate(msg));
        }
    }

    @Override
    public boolean hasWarnedOutOfTotems() {
        return warnedOutOfTotems;
    }

    @Override
    public void setWarnedOutOfTotems(boolean warned) {
        this.warnedOutOfTotems = warned;
    }

    @Override
    public void resetWarning() {
        this.warnedOutOfTotems = false;
    }
}
