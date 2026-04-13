package com.monkey.mcbot.placeholders;

import com.monkey.mcbot.MinecraftBot;
import com.monkey.mcbot.placeholders.list.*;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class BotPlaceholderCoordinator extends PlaceholderExpansion implements PlaceholderRegistration {

    private final MinecraftBot plugin;
    private final Map<String, IBotPlaceholder> placeholders = new HashMap<>();

    public BotPlaceholderCoordinator(MinecraftBot plugin) {
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
            addPlaceholder(new CombatStatePlaceholder(plugin));
            addPlaceholder(new HealingPlaceholder(plugin));
            addPlaceholder(new HealthBarPlaceholder(plugin));
            addPlaceholder(new OnlineTimePlaceholder(plugin));
            addPlaceholder(new PercentageHealthPlaceholder(plugin));
            addPlaceholder(new RankPlaceholder(plugin));
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Errore durante la registrazione dei placeholder", e);
        }
    }

    private void addPlaceholder(IBotPlaceholder placeholder) {
        placeholders.put(placeholder.getIdentifier().toLowerCase(), placeholder);
    }

    public int getRegisteredPlaceholderCount() {
        return placeholders.size();
    }

    public List<String> getRegisteredPlaceholderKeys() {
        return placeholders.keySet().stream()
                .sorted()
                .toList();
    }

    @Override
    public String getIdentifier() {
        return plugin.getName();
    }

    @Override
    public String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public String getVersion() {
        return "1.0";
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
