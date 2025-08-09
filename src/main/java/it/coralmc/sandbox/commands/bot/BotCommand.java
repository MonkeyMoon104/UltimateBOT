package it.coralmc.sandbox.commands.bot;

import it.coralmc.sandbox.SandboxTraining;
import it.coralmc.sandbox.bot.util.entity.BotEntityFinder;
import it.coralmc.sandbox.commands.bot.options.BotPermission;
import it.coralmc.sandbox.gui.BotSettingsGUI;
import it.coralmc.sandbox.utils.chatcolor.ChatColorUtils;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BotCommand implements CommandExecutor {

	private final SandboxTraining plugin;
	private final BotEntityFinder botEntityFinder;
	private final ChatColorUtils chatColorUtils;

	public BotCommand(SandboxTraining plugin, BotEntityFinder botEntityFinder) {
		this.plugin = plugin;
        this.botEntityFinder = botEntityFinder;
		this.chatColorUtils = plugin.getChatColorUtils();
    }

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player player)) {
			return true;
		}

		if (!sender.hasPermission(BotPermission.USER_BOT)) {
			sender.sendMessage(chatColorUtils.translate(plugin.getConfig().getString("messages.reload-no-permission")));
			return true;
		}

		World world = player.getWorld();
		if (world.getName().equalsIgnoreCase("SBLobby") || world.getName().equalsIgnoreCase("SBBox")) {
			player.sendRichMessage("<red>Non puoi qui!");
			return true;
		}

		new BotSettingsGUI(player, plugin, botEntityFinder).open();
		return true;
	}
}
