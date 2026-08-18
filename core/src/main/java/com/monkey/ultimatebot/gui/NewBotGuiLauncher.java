package com.monkey.ultimatebot.gui;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.bot.BotType;
import org.bukkit.entity.Player;

public final class NewBotGuiLauncher {

    private NewBotGuiLauncher() {}

    public static void open(Player player, UltimateBot plugin, BotType botType) {
        new NewBotGUI(player, plugin, botType).open();
    }
}
