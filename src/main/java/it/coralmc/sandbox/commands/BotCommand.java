package it.coralmc.sandbox.commands;

import it.coralmc.sandbox.gui.BotSettingsGUI;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BotCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player player)) {
			return true;
		}

		World world = player.getWorld();
		if (world.getName().equalsIgnoreCase("SBLobby") || world.getName().equalsIgnoreCase("SBBox")) {
			player.sendRichMessage("<red>Non puoi qui!");
			return true;
		}

		new BotSettingsGUI(player).open();
		return true;
	}
}
