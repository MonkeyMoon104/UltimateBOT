package com.monkey.mcbot.bot;

import com.monkey.mcbot.bot.ai.ITrainingBot;
import com.monkey.mcbot.utils.EntityUtils;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

public class BotLookup {

    private final BotRegistry registry;

    public BotLookup(BotRegistry registry) {
        this.registry = java.util.Objects.requireNonNull(registry, "registry");
    }

    public ITrainingBot getBotByOwnerUUID(UUID ownerUUID) {
        ServerLevel world = EntityUtils.getPlayerWorld(ownerUUID);
        if (world == null) return null;

        UUID botUUID = registry.getBotUUID(ownerUUID);
        if (botUUID == null) return null;

        Entity entity = EntityUtils.findBotByUUID(world, botUUID);
        return (entity instanceof ITrainingBot trainingBot) ? trainingBot : null;
    }

    public ITrainingBot getBotSafe(UUID ownerUUID) {
        return registry.getBot(ownerUUID);
    }

    public boolean isBotSpawned(UUID ownerUUID) {
        return registry.isBotSpawned(ownerUUID);
    }

    public void removeBot(UUID botUUID) {
        registry.removeBotByUUID(botUUID);
    }
}
