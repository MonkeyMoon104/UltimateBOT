package com.monkey.ultimatebot.bot.ai.controllers.inventory.helper;

import com.monkey.ultimatebot.common.model.EquipmentSlotKind;
import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.bot.ai.controllers.inventory.helper.inter.IEquipmentBroadcaster;
import com.monkey.ultimatebot.compat.EquipmentSlotAccess;
import com.monkey.ultimatebot.nms.NMSBridgeManager;
import java.util.EnumMap;
import java.util.Map;

public class EquipmentBroadcaster implements IEquipmentBroadcaster {

    @Override
    public void broadcastEquipmentChange(ITrainingBot bot) {
        Map<EquipmentSlotKind, org.bukkit.inventory.ItemStack> equipment = new EnumMap<>(EquipmentSlotKind.class);
        equipment.put(EquipmentSlotKind.HAND, bot.getItem(EquipmentSlotKind.HAND));
        EquipmentSlotKind offHand = EquipmentSlotAccess.offHand();
        if (offHand != null) {
            equipment.put(offHand, bot.getItem(offHand));
        }
        equipment.put(EquipmentSlotKind.HEAD, bot.getItem(EquipmentSlotKind.HEAD));
        equipment.put(EquipmentSlotKind.CHEST, bot.getItem(EquipmentSlotKind.CHEST));
        equipment.put(EquipmentSlotKind.LEGS, bot.getItem(EquipmentSlotKind.LEGS));
        equipment.put(EquipmentSlotKind.FEET, bot.getItem(EquipmentSlotKind.FEET));
        NMSBridgeManager.get().broadcastEquipment(bot, equipment);
    }

    @Override
    public void broadcastHandChange(ITrainingBot bot) {
        Map<EquipmentSlotKind, org.bukkit.inventory.ItemStack> equipment = new EnumMap<>(EquipmentSlotKind.class);
        equipment.put(EquipmentSlotKind.HAND, bot.getItem(EquipmentSlotKind.HAND));
        NMSBridgeManager.get().broadcastEquipment(bot, equipment);
    }

    @Override
    public void broadcastMetadataChange(ITrainingBot bot) {
        NMSBridgeManager.get().broadcastMetadata(bot);
    }
}
