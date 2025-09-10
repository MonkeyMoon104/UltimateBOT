package it.coralmc.sandbox.placeholders;

import it.coralmc.sandbox.SandboxTraining;
import it.coralmc.sandbox.placeholders.list.*;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class BotPlaceholderCoordinator extends PlaceholderExpansion {

    private final SandboxTraining plugin;
    private final Map<String, IBotPlaceholder> placeholders = new HashMap<>();

    public BotPlaceholderCoordinator(SandboxTraining plugin) {
        this.plugin = plugin;
        registerPlaceholders();
    }

    private void registerPlaceholders() {
        try {
            addPlaceholder(new TotemPlaceholder(plugin));
            addPlaceholder(new FollowPlaceholder(plugin));
            addPlaceholder(new CombatPlaceholder(plugin));
            addPlaceholder(new HealthPlaceholder(plugin));
            addPlaceholder(new ArmorPlaceholder(plugin));
            addPlaceholder(new StatusPlaceholder(plugin));
            addPlaceholder(new LocationPlaceholder(plugin));
            addPlaceholder(new DistancePlaceholder(plugin));

            plugin.getLogger().info("Registrati " + placeholders.size() + " placeholder");
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Errore durante la registrazione dei placeholder", e);
        }
    }

    private void addPlaceholder(IBotPlaceholder placeholder) {
        placeholders.put(placeholder.getIdentifier().toLowerCase(), placeholder);
    }

    @Override
    public String getIdentifier() {
        return "bot";
    }

    @Override
    public String getAuthor() {
        return "CoralMC";
    }

    @Override
    public String getVersion() {
        return "2.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (player == null || identifier == null) {
            return null;
        }

        try {
            IBotPlaceholder placeholder = placeholders.get(identifier.toLowerCase());
            if (placeholder != null) {
                String result = placeholder.getValue(player);
                return result != null ? result : "N/A";
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Errore nel placeholder " + identifier + " per " + player.getName(), e);
            return "ERROR";
        }

        return null;
    }
}