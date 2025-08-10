package it.coralmc.sandbox.utils;

import org.bukkit.ChatColor;

public class ChatColorUtils {

    public static String translate(String input) {
        return input == null ? null : ChatColor.translateAlternateColorCodes('&', input);
    }
}
