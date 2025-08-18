package it.coralmc.sandbox.bot;

import it.coralmc.sandbox.bot.ai.TrainingBot;
import it.coralmc.sandbox.utils.Packet;
import it.coralmc.sandbox.utils.equipment.BotEquipmentUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;

public class BotBroadcaster {

    public static void broadcastSpawn(TrainingBot bot,
                                      Map<org.bukkit.inventory.EquipmentSlot, org.bukkit.inventory.ItemStack> armorMap,
                                      Map<org.bukkit.inventory.EquipmentSlot, Boolean> blastProtectionMap) {
        for (Player online : Bukkit.getOnlinePlayers()) {
            Packet.sendAddPlayerPacket(online, bot);
            Packet.sendSpawnPlayerPacket(online, bot);
        }
        BotEquipmentUtils.broadcastEquipment(bot, armorMap, blastProtectionMap);
    }
}
