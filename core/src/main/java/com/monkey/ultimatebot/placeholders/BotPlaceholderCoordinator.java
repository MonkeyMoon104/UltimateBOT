package com.monkey.ultimatebot.placeholders;

import java.util.stream.Collectors;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.compat.PluginMetaAccess;
import com.monkey.ultimatebot.placeholders.list.combat.ArmorPlaceholder;
import com.monkey.ultimatebot.placeholders.list.combat.CombatPlaceholder;
import com.monkey.ultimatebot.placeholders.list.combat.CombatStatePlaceholder;
import com.monkey.ultimatebot.placeholders.list.combat.HealingPlaceholder;
import com.monkey.ultimatebot.placeholders.list.combat.TotemPlaceholder;
import com.monkey.ultimatebot.placeholders.list.configuration.DifficultyPlaceholder;
import com.monkey.ultimatebot.placeholders.list.configuration.FollowPlaceholder;
import com.monkey.ultimatebot.placeholders.list.status.DistancePlaceholder;
import com.monkey.ultimatebot.placeholders.list.status.HealthBarPlaceholder;
import com.monkey.ultimatebot.placeholders.list.status.HealthPlaceholder;
import com.monkey.ultimatebot.placeholders.list.status.LocationPlaceholder;
import com.monkey.ultimatebot.placeholders.list.status.OnlineTimePlaceholder;
import com.monkey.ultimatebot.placeholders.list.status.PercentageHealthPlaceholder;
import com.monkey.ultimatebot.placeholders.list.status.StatusPlaceholder;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

public class BotPlaceholderCoordinator extends PlaceholderExpansion implements PlaceholderRegistration {

    private final UltimateBot plugin;
    private final Map<String, IBotPlaceholder> placeholders = new HashMap<>();

    public BotPlaceholderCoordinator(UltimateBot plugin) {
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
            addPlaceholder(new DifficultyPlaceholder(plugin));
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error while registering placeholders", e);
        }
    }

    private void addPlaceholder(IBotPlaceholder placeholder) {
        placeholders.put(placeholder.getIdentifier().toLowerCase(Locale.ROOT), placeholder);
    }

    public int getRegisteredPlaceholderCount() {
        return placeholders.size();
    }

    @Override
    public List<String> getRegisteredPlaceholderKeys() {
        return placeholders.keySet().stream().sorted().collect(Collectors.toList());
    }

    @Override
    public String getIdentifier() {
        return plugin.getName();
    }

    @Override
    public String getAuthor() {
        return PluginMetaAccess.authors(plugin).toString();
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
    public @Nullable String onPlaceholderRequest(@Nullable Player player, @Nullable String identifier) {
        if (player == null || identifier == null) {
            return null;
        }

        try {
            IBotPlaceholder placeholder = placeholders.get(identifier.toLowerCase(Locale.ROOT));
            if (placeholder != null) {
                String result = placeholder.getValue(player);
                return result != null ? result : "N/A";
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error in placeholder " + identifier + " for " + player.getName(), e);
            return "ERROR";
        }

        return null;
    }
}
