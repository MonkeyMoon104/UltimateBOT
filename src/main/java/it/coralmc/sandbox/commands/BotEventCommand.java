package it.coralmc.sandbox.commands;

import it.coralmc.sandbox.SandboxTraining;
import it.coralmc.sandbox.gui.NewBotGUI;
import it.coralmc.sandbox.utils.ChatColorUtils;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BotEventCommand implements CommandExecutor {

    private final SandboxTraining plugin;

    public BotEventCommand(SandboxTraining plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }

        if (!sender.hasPermission("admin.host.bot")) {
            sender.sendMessage(ChatColorUtils.translate(plugin.getConfig().getString("messages.reload-no-permission", "")));
            return true;
        }

        World world = player.getWorld();
        if (world.getName().equalsIgnoreCase("SBLobby") || world.getName().equalsIgnoreCase("SBBox")) {
            player.sendRichMessage("<red>Non puoi qui!");
            return true;
        }

        new NewBotGUI(player, plugin, true).open();
        return true;
    }
}
