package com.monkey.mcbot.listener;

import com.github.sirblobman.combatlogx.api.event.PlayerPreTagEvent;
import com.monkey.mcbot.MinecraftBot;
import com.monkey.mcbot.bot.BotManager;
import com.monkey.mcbot.bot.ai.ITrainingBot;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerTagListener implements Listener {

    private final BotManager botManager;

    public PlayerTagListener(MinecraftBot plugin) {
        this.botManager = plugin.getBotManager();
    }

    @EventHandler
    public void onPlayerPreTag(PlayerPreTagEvent event) {
        Player player = event.getPlayer();
        ITrainingBot bot = botManager.getBot(player.getUniqueId());
        if (bot == null) return;

        Entity enemy = event.getEnemy();
        if (enemy != null && enemy.getUniqueId().equals(bot.asPlayer().getUUID())) event.setCancelled(true);
    }
}
