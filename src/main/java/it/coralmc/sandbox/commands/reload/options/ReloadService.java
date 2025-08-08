package it.coralmc.sandbox.commands.reload.options;

import it.coralmc.sandbox.SandboxTraining;
public class ReloadService {

    private final SandboxTraining plugin;

    public ReloadService(SandboxTraining plugin) {
        this.plugin = plugin;
    }

    public void reloadConfig() {
        plugin.reloadConfig();
    }

    public void reloadBot() {
        plugin.getBotSpawner().despawnAllBots();
    }

    public void reloadMaps() {
        plugin.getPlayerArmorManager().clearAll();
    }

    public void reloadAll() {
        reloadConfig();
        reloadBot();
        reloadMaps();
    }
}
