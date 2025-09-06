package it.coralmc.sandbox.bot.ai.controllers.inventory.inter;

import net.minecraft.world.entity.player.Player;

public interface IEquipmentBroadcaster {

    void broadcastEquipmentChange(Player bot);
}