package com.monkey.ultimatebot.integration.api.internal;

import com.monkey.ultimatebot.bot.BotManager;
import com.monkey.ultimatebot.bot.BotOptions;
import com.monkey.ultimatebot.bot.BotRegistry;
import com.monkey.ultimatebot.common.model.BotSource;
import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import java.util.Map;
import java.util.UUID;

public final class CoreBotLifecycleService {

    private final BotManager botManager;
    private final BotRegistry botRegistry;
    private final CoreBotOwnerResolver ownerResolver;
    private final CoreBotCacheStore cacheStore;

    public CoreBotLifecycleService(
            BotManager botManager, BotRegistry botRegistry, CoreBotOwnerResolver ownerResolver, CoreBotCacheStore cacheStore) {
        this.botManager = botManager;
        this.botRegistry = botRegistry;
        this.ownerResolver = ownerResolver;
        this.cacheStore = cacheStore;
    }

    public boolean remove(UUID ownerUUID) {
        if (ownerUUID == null) {
            return false;
        }

        UUID managedOwner = ownerResolver.resolveManagedOwner(ownerUUID);
        if (!botManager.isBotSpawned(managedOwner)) {
            return false;
        }

        ITrainingBot bot = botManager.getBotSafe(managedOwner);
        BotOptions options = bot != null && bot.getBrainController() != null ? bot.getBrainController().getBotOptions() : null;

        if (!botManager.despawnByOwnerUUID(managedOwner)) {
            return false;
        }
        cacheStore.removeCachedOptions(managedOwner, options);
        return true;
    }

    public boolean removeByBotUUID(UUID botUUID) {
        UUID ownerUUID = ownerResolver.resolveOwnerByBotUUID(botUUID);
        return ownerUUID != null && remove(ownerUUID);
    }

    public int removeBySource(BotSource source) {
        if (source == null) {
            return 0;
        }

        int removed = 0;
        Map<UUID, ITrainingBot> snapshot = botRegistry.getAllBots();
        for (Map.Entry<UUID, ITrainingBot> entry : snapshot.entrySet()) {
            ITrainingBot bot = entry.getValue();
            if (bot == null || bot.getBrainController() == null) {
                continue;
            }

            BotOptions options = bot.getBrainController().getBotOptions();
            if (options == null) {
                continue;
            }

            BotSource currentSource = options.getCreationSource().toCommon();
            if (currentSource == source && remove(entry.getKey())) {
                removed++;
            }
        }
        return removed;
    }

    public int removeAll() {
        int activeBots = botRegistry.getAllBots().size();
        despawnAll();
        return activeBots;
    }

    public void despawnAll() {
        botManager.despawnAll();
        cacheStore.clear();
    }
}
