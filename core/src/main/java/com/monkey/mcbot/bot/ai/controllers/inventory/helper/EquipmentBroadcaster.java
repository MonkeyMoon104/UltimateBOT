package com.monkey.mcbot.bot.ai.controllers.inventory.helper;

import com.mojang.datafixers.util.Pair;
import com.monkey.mcbot.bot.ai.controllers.inventory.helper.inter.IEquipmentBroadcaster;
import com.monkey.mcbot.nms.NMSBridgeManager;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftPlayer;

import java.util.ArrayList;
import java.util.List;

public class EquipmentBroadcaster implements IEquipmentBroadcaster {

    @Override
    public void broadcastEquipmentChange(Player bot) {
        List<Pair<EquipmentSlot, ItemStack>> equipmentList = new ArrayList<>();
        equipmentList.add(Pair.of(EquipmentSlot.MAINHAND, bot.getItemBySlot(EquipmentSlot.MAINHAND)));

        ClientboundSetEquipmentPacket packet = NMSBridgeManager.get()
                .createEquipmentPacket(bot.getId(), equipmentList);

        for (org.bukkit.entity.Player online : Bukkit.getOnlinePlayers()) {
            ServerPlayer handle = ((CraftPlayer) online).getHandle();
            handle.connection.send(packet);
        }
    }
}