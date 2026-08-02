package com.monkey.ultimatebot.bot.ai.controllers.inventory.helper;

import com.mojang.datafixers.util.Pair;
import com.monkey.ultimatebot.bot.ai.controllers.inventory.helper.inter.IEquipmentBroadcaster;
import com.monkey.ultimatebot.nms.NMSBridgeManager;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftPlayer;

public class EquipmentBroadcaster implements IEquipmentBroadcaster {

    @Override
    public void broadcastEquipmentChange(Player bot) {
        List<Pair<EquipmentSlot, ItemStack>> equipmentList = new ArrayList<>();
        equipmentList.add(Pair.of(
                EquipmentSlot.MAINHAND,
                bot.getItemBySlot(EquipmentSlot.MAINHAND).copy()));
        equipmentList.add(Pair.of(
                EquipmentSlot.OFFHAND, bot.getItemBySlot(EquipmentSlot.OFFHAND).copy()));
        equipmentList.add(Pair.of(
                EquipmentSlot.HEAD, bot.getItemBySlot(EquipmentSlot.HEAD).copy()));
        equipmentList.add(Pair.of(
                EquipmentSlot.CHEST, bot.getItemBySlot(EquipmentSlot.CHEST).copy()));
        equipmentList.add(Pair.of(
                EquipmentSlot.LEGS, bot.getItemBySlot(EquipmentSlot.LEGS).copy()));
        equipmentList.add(Pair.of(
                EquipmentSlot.FEET, bot.getItemBySlot(EquipmentSlot.FEET).copy()));

        ClientboundSetEquipmentPacket packet = NMSBridgeManager.get().createEquipmentPacket(bot.getId(), equipmentList);

        for (org.bukkit.entity.Player online : Bukkit.getOnlinePlayers()) {
            ServerPlayer handle = ((CraftPlayer) online).getHandle();
            handle.connection.send(packet);
        }
    }
}
