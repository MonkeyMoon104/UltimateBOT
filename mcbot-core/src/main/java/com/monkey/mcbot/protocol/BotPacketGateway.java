package com.monkey.mcbot.protocol;

import com.monkey.mcbot.bot.ai.ITrainingBot;
import net.minecraft.world.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Single boundary for packets used to render a bot to a connected client.
 */
public interface BotPacketGateway {

    void show(Player viewer, ITrainingBot bot);

    void sendEquipment(Player viewer, int entityId, List<BotEquipment> equipment);

    void sendCurrentEquipment(Player viewer, LivingEntity bot);
}
