package com.monkey.mcbot.bot.ai.controllers.totem.helper;

import com.monkey.mcbot.MinecraftBot;
import com.monkey.mcbot.bot.ai.ITrainingBot;
import com.monkey.mcbot.bot.ai.controllers.totem.helper.interf.ITotemNotificationManager;
import com.monkey.mcbot.utils.ChatColorUtils;

public class TotemNotificationManager implements ITotemNotificationManager {
    private final MinecraftBot plugin;
    private boolean warnedOutOfTotems = false;

    public TotemNotificationManager(MinecraftBot plugin) {
        this.plugin = plugin;
    }

    @Override
    public void sendTotemWarning(ITrainingBot trainingBot) {
        var player = trainingBot.getTargetPlayer();
        if (player != null && player.isOnline()) {
            String msg = plugin.getConfig()
                    .getString("bot.totem-finish", "[%botname%] Running out of totems");
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