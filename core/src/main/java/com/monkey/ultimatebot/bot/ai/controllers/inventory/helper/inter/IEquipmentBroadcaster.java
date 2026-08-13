package com.monkey.ultimatebot.bot.ai.controllers.inventory.helper.inter;

import com.monkey.ultimatebot.bot.ai.ITrainingBot;

public interface IEquipmentBroadcaster {

    /** Full armor + hands (loadout / armor changes). */
    void broadcastEquipmentChange(ITrainingBot bot);

    /** Main-hand only — hotbar switches must not re-send armor (client equip-sound spam). */
    void broadcastHandChange(ITrainingBot bot);

    void broadcastMetadataChange(ITrainingBot bot);
}
