package it.coralmc.sandbox.commands.reload;

import it.coralmc.sandbox.SandboxTraining;
import it.coralmc.sandbox.commands.reload.options.ReloadPermission;
import it.coralmc.sandbox.commands.reload.options.ReloadService;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ReloadCommand implements CommandExecutor {

    private final SandboxTraining plugin;
    private final ReloadService reloadService;

    public ReloadCommand(SandboxTraining plugin) {
        this.plugin = plugin;
        this.reloadService = new ReloadService(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!sender.hasPermission(ReloadPermission.ADMIN_USE)) {
            sender.sendRichMessage("<red>Non hai il permesso di usare questo comando");
            return true;
        }

        if (args.length == 0) {
            sender.sendRichMessage("<yellow>Utilizzo: /reload <config|bot|maps|all>");
            return true;
        }

        String option = args[0].toLowerCase();

        switch (option) {
            case "config" -> {
                reloadService.reloadConfig();
                sender.sendRichMessage("<green>Config ricaricata con successo");
            }
            case "bot" -> {
                reloadService.reloadBot();
                sender.sendRichMessage("<green>Tutti i bot attivi sono stati rimossi");
            }
            case "maps" -> {
                reloadService.reloadMaps();
                sender.sendRichMessage("<green>Tutte le mappe dei player sono state pulite");
            }
            case "all" -> {
                reloadService.reloadAll();
                sender.sendRichMessage("<green>Configurazione, bot e mappe sono state ricaricate/pulite");
            }
            default -> sender.sendRichMessage("<green>Opzione non valida. Usa: config, bot, maps, all");
        }

        return true;
    }
}