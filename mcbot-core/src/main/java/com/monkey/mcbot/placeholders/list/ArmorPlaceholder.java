package com.monkey.mcbot.placeholders.list;

import com.monkey.mcbot.MinecraftBot;
import com.monkey.mcbot.bot.ai.ITrainingBot;
import com.monkey.mcbot.placeholders.IBotPlaceholder;
import com.monkey.mcbot.placeholders.PlaceholderHelper;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ArmorPlaceholder implements IBotPlaceholder {

    private final PlaceholderHelper helper;

    public ArmorPlaceholder(MinecraftBot plugin) {
        this.helper = new PlaceholderHelper(plugin);
    }

    @Override
    public String getIdentifier() {
        return "armor";
    }

    @Override
    public String getValue(Player player) {
        ITrainingBot bot = helper.getBotForPlaceholder(player);
        if (bot != null && bot.asPlayer().getBukkitEntity() != null) {
            ItemStack[] armor = bot.asPlayer().getBukkitEntity().getEquipment().getArmorContents();
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
