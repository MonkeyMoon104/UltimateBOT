package com.monkey.ultimatebot.bot.ai.controllers.inventory.helper.inter;

import com.monkey.ultimatebot.bot.ai.ITrainingBot;

public interface IEquipmentBroadcaster {

    void broadcastEquipmentChange(ITrainingBot bot);

    void broadcastHandChange(ITrainingBot bot);

    void broadcastMetadataChange(ITrainingBot bot);
}
