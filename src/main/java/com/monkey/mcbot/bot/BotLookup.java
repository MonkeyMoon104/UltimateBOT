package com.monkey.mcbot.bot;

import com.monkey.mcbot.bot.ai.TrainingBot;
import com.monkey.mcbot.utils.EntityUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

import java.util.UUID;

public class BotLookup {

    private final BotRegistry registry;

    public BotLookup(BotRegistry registry) {
        this.registry = registry;
    }

    public TrainingBot getBotByOwnerUUID(UUID ownerUUID) {
        ServerLevel world = EntityUtils.getPlayerWorld(ownerUUID);
        if (world == null) return null;

        UUID botUUID = registry.getBotUUID(ownerUUID);
        if (botUUID == null) return null;

        Entity entity = EntityUtils.findBotByUUID(world, botUUID);
        return (entity instanceof TrainingBot trainingBot) ? trainingBot : null;
    }

    public TrainingBot getBotSafe(UUID ownerUUID) {
        return registry.getBot(ownerUUID);
    }


    public boolean isBotSpawned(UUID ownerUUID) {
        return registry.isBotSpawned(ownerUUID);
    }

    public void removeBot(UUID botUUID) {
        registry.removeBotByUUID(botUUID);
    }
}
