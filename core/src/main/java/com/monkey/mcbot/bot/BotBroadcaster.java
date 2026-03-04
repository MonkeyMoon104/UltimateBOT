package com.monkey.mcbot.bot;

import com.monkey.mcbot.bot.ai.ITrainingBot;
import com.monkey.mcbot.utils.Packet;
import com.monkey.mcbot.utils.equipment.BotEquipmentUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;

public class BotBroadcaster {

    public static void broadcastSpawn(ITrainingBot bot,
                                      Map<org.bukkit.inventory.EquipmentSlot, org.bukkit.inventory.ItemStack> armorMap,
                                      Map<org.bukkit.inventory.EquipmentSlot, Boolean> blastProtectionMap) {
        for (Player online : Bukkit.getOnlinePlayers()) {
            Packet.sendAddPlayerPacket(online, bot);
            Packet.sendSpawnPlayerPacket(online, bot);
        }
        BotEquipmentUtils.broadcastEquipment(bot.asPlayer(), armorMap, blastProtectionMap);
    }
}
