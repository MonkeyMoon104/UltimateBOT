package com.monkey.ultimatebot.api.extension.brain;

/** Factory and metadata provider for a custom bot brain. */
public interface BotBrainProvider {
    BrainDescriptor descriptor();

    BotBrainSession create(BotBrainContext context);
}
