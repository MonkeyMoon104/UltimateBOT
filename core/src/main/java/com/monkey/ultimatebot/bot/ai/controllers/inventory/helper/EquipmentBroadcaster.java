package com.monkey.ultimatebot.bot.ai.controllers.inventory.helper;

import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.bot.ai.controllers.inventory.helper.inter.IEquipmentBroadcaster;
import com.monkey.ultimatebot.nms.NMSBridgeManager;
import java.util.EnumMap;
import java.util.Map;
import org.bukkit.inventory.EquipmentSlot;

public class EquipmentBroadcaster implements IEquipmentBroadcaster {

    @Override
    public void broadcastEquipmentChange(ITrainingBot bot) {
        Map<EquipmentSlot, org.bukkit.inventory.ItemStack> equipment = new EnumMap<>(EquipmentSlot.class);
        equipment.put(EquipmentSlot.HAND, bot.getItem(EquipmentSlot.HAND));
        equipment.put(EquipmentSlot.OFF_HAND, bot.getItem(EquipmentSlot.OFF_HAND));
        equipment.put(EquipmentSlot.HEAD, bot.getItem(EquipmentSlot.HEAD));
        equipment.put(EquipmentSlot.CHEST, bot.getItem(EquipmentSlot.CHEST));
        equipment.put(EquipmentSlot.LEGS, bot.getItem(EquipmentSlot.LEGS));
        equipment.put(EquipmentSlot.FEET, bot.getItem(EquipmentSlot.FEET));
        NMSBridgeManager.get().broadcastEquipment(bot, equipment);
    }

    @Override
    public void broadcastHandChange(ITrainingBot bot) {
        Map<EquipmentSlot, org.bukkit.inventory.ItemStack> equipment = new EnumMap<>(EquipmentSlot.class);
        equipment.put(EquipmentSlot.HAND, bot.getItem(EquipmentSlot.HAND));
        NMSBridgeManager.get().broadcastEquipment(bot, equipment);
    }

    @Override
    public void broadcastMetadataChange(ITrainingBot bot) {
        NMSBridgeManager.get().broadcastMetadata(bot);
    }
}
