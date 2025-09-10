package it.coralmc.sandbox.placeholders.list;

import it.coralmc.sandbox.SandboxTraining;
import it.coralmc.sandbox.bot.ai.TrainingBot;
import it.coralmc.sandbox.placeholders.IBotPlaceholder;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ArmorPlaceholder implements IBotPlaceholder {

    private final SandboxTraining plugin;

    public ArmorPlaceholder(SandboxTraining plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier() {
        return "armor";
    }

    @Override
    public String getValue(Player player) {
        TrainingBot bot = plugin.getBot(player);
        if (bot != null && bot.getBukkitEntity() != null) {
            ItemStack[] armor = bot.getBukkitEntity().getEquipment().getArmorContents();
            int armorPieces = 0;
            for (ItemStack item : armor) {
                if (item != null && !item.getType().isAir()) {
                    armorPieces++;
                }
            }
            return armorPieces + "/4";
        }
        return "0/4";
    }
}