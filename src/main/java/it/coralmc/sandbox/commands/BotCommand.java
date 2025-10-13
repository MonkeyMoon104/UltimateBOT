package it.coralmc.sandbox.commands;

import it.coralmc.sandbox.SandboxTraining;
import it.coralmc.sandbox.bot.ai.TrainingBot;
import it.coralmc.sandbox.gui.NewBotGUI;
import it.coralmc.sandbox.utils.ChatColorUtils;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BotCommand implements CommandExecutor {

    private final SandboxTraining plugin;

    public BotCommand(SandboxTraining plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }

        if (!sender.hasPermission("sb.bot.use")) {
            sender.sendMessage(ChatColorUtils.translate(plugin.getConfig().getString("messages.reload-no-permission", "")));
            return true;
        }

        World world = player.getWorld();
        if (world.getName().equalsIgnoreCase("SBLobby") || world.getName().equalsIgnoreCase("SBBox")) {
            player.sendRichMessage("<red>Non puoi qui!");
            return true;
        }

        if (isEventBotActive()) {
            player.sendMessage(ChatColorUtils.translate("&c❌ C'è un bot event attivo! Non puoi spawnare bot normali durante un evento."));
            return true;
        }

        new NewBotGUI(player, plugin).open();
        return true;
    }

    private boolean isEventBotActive() {
        for (TrainingBot bot : plugin.getBotRegistry().getAllBots().values()) {
            if (bot != null && bot.getBrainController() != null) {
                var botOptions = bot.getBrainController().getBotOptions();
                if (botOptions != null && botOptions.isEventBot()) {
                    return true;
                }
            }
        }
        return false;
    }
}
