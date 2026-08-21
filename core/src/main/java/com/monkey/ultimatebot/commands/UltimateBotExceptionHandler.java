package com.monkey.ultimatebot.commands;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.utils.ChatColorUtils;
import org.jetbrains.annotations.NotNull;
import com.monkey.ultimatebot.libs.lamp.bukkit.actor.BukkitCommandActor;
import com.monkey.ultimatebot.libs.lamp.bukkit.exception.BukkitExceptionHandler;
import com.monkey.ultimatebot.libs.lamp.exception.NoPermissionException;

public final class UltimateBotExceptionHandler extends BukkitExceptionHandler {

    private final UltimateBot plugin;

    public UltimateBotExceptionHandler(UltimateBot plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onNoPermission(@NotNull NoPermissionException e, @NotNull BukkitCommandActor actor) {
        String msg = plugin.getLangString("messages.reload-no-permission", "");
        if (msg == null || msg.trim().isEmpty()) {
            super.onNoPermission(e, actor);
            return;
        }
        actor.sender().sendMessage(ChatColorUtils.translate(msg));
    }
}
