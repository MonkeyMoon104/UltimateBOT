package it.coralmc.sandbox.bot.ai.controllers.totem.helper;

import it.coralmc.sandbox.SandboxTraining;
import it.coralmc.sandbox.bot.ai.TrainingBot;
import it.coralmc.sandbox.bot.ai.controllers.totem.helper.interf.ITotemNotificationManager;
import it.coralmc.sandbox.utils.ChatColorUtils;

public class TotemNotificationManager implements ITotemNotificationManager {
    private final SandboxTraining plugin;
    private boolean warnedOutOfTotems = false;

    public TotemNotificationManager(SandboxTraining plugin) {
        this.plugin = plugin;
    }

    @Override
    public void sendTotemWarning(TrainingBot trainingBot) {
        var player = trainingBot.getTargetPlayer();
        if (player != null && player.isOnline()) {
            String msg = plugin.getConfig()
                    .getString("bot.totem-finish", "[%botname%] Running out of totems");
            String botName = plugin.getConfig().getString("bot.name", "CrystalBot");
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