package it.coralmc.sandbox.utils.chatcolor;

import org.bukkit.ChatColor;

import java.util.List;
import java.util.stream.Collectors;

public class ChatColorUtils {

	public String translate(String input) {
		return input == null ? null : ChatColor.translateAlternateColorCodes('&', input);
	}

	public List<String> translate(List<String> input) {
		return input == null ? null : input.stream()
				.map(this::translate)
				.collect(Collectors.toList());
	}
}
