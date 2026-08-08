package com.monkey.ultimatebot.placeholders.list.combat;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.placeholders.IBotPlaceholder;
import com.monkey.ultimatebot.placeholders.PlaceholderHelper;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ArmorPlaceholder implements IBotPlaceholder {

    private final PlaceholderHelper helper;

    public ArmorPlaceholder(UltimateBot plugin) {
        this.helper = new PlaceholderHelper(plugin);
    }

    @Override
    public String getIdentifier() {
        return "armor";
    }

    @Override
    public String getValue(Player player) {
        ITrainingBot bot = helper.getBotForPlaceholder(player);
        if (bot != null && bot.asBukkitPlayer() != null && bot.asBukkitPlayer().getEquipment() != null) {
            ItemStack[] armor = bot.asBukkitPlayer().getEquipment().getArmorContents();
            int armorPieces = 0;
            for (ItemStack item : armor) {
                if (item != null && !item.getType().isAir()) {
                    armorPieces++;
                }
            }
            return "◆ " + armorPieces + "/4";
        }
        return "◆ 0/4";
    }
}
