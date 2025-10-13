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

        if (hasNormalBotSpawned(player)) {
            String msg = plugin.getConfig().getString("messages.must-despawn-normal-bot", "&c❌ Hai già un bot normale spawnato! Despawnalo prima di gestire il bot event.");
            player.sendMessage(ChatColorUtils.translate(msg));
            return true;
        }

        new NewBotGUI(player, plugin, true).open();
        return true;
    }

    private boolean hasNormalBotSpawned(Player player) {
        if (!plugin.getBotManager().isBotSpawned(player.getUniqueId())) {
            return false;
        }

        var bot = plugin.getBotManager().getBotSafe(player.getUniqueId());
        if (bot != null && bot.getBrainController() != null) {
            var botOptions = bot.getBrainController().getBotOptions();
            if (botOptions != null) {
                return !botOptions.isEventBot();
            }
        }
        return false;
    }
}