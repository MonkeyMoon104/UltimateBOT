package com.monkey.mcbot.bot.ai.controllers.inventory.helper;

import com.monkey.mcbot.bot.ai.controllers.inventory.helper.inter.IEquipmentBroadcaster;
import com.monkey.mcbot.protocol.BotEquipment;
import com.monkey.mcbot.protocol.PacketEventsBotPacketGateway;
import net.minecraft.world.entity.player.Player;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.inventory.EquipmentSlot;

import java.util.List;

public class EquipmentBroadcaster implements IEquipmentBroadcaster {

    @Override
    public void broadcastEquipmentChange(Player bot) {
        List<BotEquipment> equipment = List.of(new BotEquipment(
                EquipmentSlot.HAND,
                CraftItemStack.asBukkitCopy(bot.getMainHandItem())
        ));

        for (org.bukkit.entity.Player online : Bukkit.getOnlinePlayers()) {
            PacketEventsBotPacketGateway.get().sendEquipment(online, bot.getId(), equipment);
        }
    }
}
