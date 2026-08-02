package com.monkey.ultimatebot.placeholders.list;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.placeholders.IBotPlaceholder;
import com.monkey.ultimatebot.placeholders.PlaceholderHelper;
import org.bukkit.entity.Player;

public class DistancePlaceholder implements IBotPlaceholder {

    private final PlaceholderHelper helper;

    public DistancePlaceholder(UltimateBot plugin) {
        this.helper = new PlaceholderHelper(plugin);
    }

    @Override
    public String getIdentifier() {
        return "distance";
    }

    @Override
    public String getValue(Player player) {
        ITrainingBot bot = helper.getBotForPlaceholder(player);
        if (bot != null && bot.asPlayer().getBukkitEntity() != null) {
            double distance = player.getLocation()
                    .distance(bot.asPlayer().getBukkitEntity().getLocation());
            return String.format("◈ %.1fm", distance);
        }
        return "◈ ∞";
    }
}
