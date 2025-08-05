package it.coralmc.sandbox.utils;

import org.bukkit.ChatColor;

import java.util.List;
import java.util.stream.Collectors;

public class ChatColorUtils {

    public static String translate(String input) {
        return input == null ? null : ChatColor.translateAlternateColorCodes('&', input);
    }

    public static List<String> translate(List<String> input) {
        return input == null ? null : input.stream()
                .map(ChatColorUtils::translate)
                .collect(Collectors.toList());
    }
}
