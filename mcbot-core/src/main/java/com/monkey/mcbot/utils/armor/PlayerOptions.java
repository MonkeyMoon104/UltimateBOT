package com.monkey.mcbot.utils.armor;

import com.monkey.mcbot.bot.BotOptions;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerOptions {

    private final Map<UUID, BotOptions> options = new HashMap<>();

    public void put(UUID uuid, BotOptions options) {
        this.options.put(uuid, options);
    }

    public BotOptions getOptions(UUID uuid) {
        return this.options.get(uuid);
    }

    public void remove(UUID uuid) {
        this.options.remove(uuid);
    }

    public void clear() {
        this.options.clear();
    }

    public int size() {
        return this.options.size();
    }
}
