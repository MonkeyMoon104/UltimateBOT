package com.monkey.ultimatebot.listener;

import com.github.sirblobman.combatlogx.api.event.PlayerPreTagEvent;
import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.bot.BotManager;
import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerTagListener implements Listener {

    private final BotManager botManager;

    public PlayerTagListener(UltimateBot plugin) {
        this.botManager = plugin.getBotManager();
    }

    @EventHandler
    public void onPlayerPreTag(PlayerPreTagEvent event) {
        Player player = event.getPlayer();
        ITrainingBot bot = botManager.getBot(player.getUniqueId());
        if (bot == null) return;

        Entity enemy = event.getEnemy();
        if (enemy != null && enemy.getUniqueId().equals(bot.getUniqueId())) event.setCancelled(true);
    }
}
