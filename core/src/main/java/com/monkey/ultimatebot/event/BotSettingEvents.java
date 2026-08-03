package com.monkey.ultimatebot.event;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.api.event.base.BotEventSource;
import com.monkey.ultimatebot.api.event.state.BotSettingKey;
import com.monkey.ultimatebot.api.event.state.BotSettingsChangeEvent;
import com.monkey.ultimatebot.api.model.runtime.BotSnapshot;
import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** Shared typed gate used by every runtime setting entry point. */
public final class BotSettingEvents {
    private BotSettingEvents() {}

    public static <T> Optional<T> propose(
            UltimateBot plugin,
            UUID ownerUUID,
            BotEventSource source,
            BotSettingKey key,
            T oldValue,
            T newValue,
            Class<T> valueType) {
        if (Objects.equals(oldValue, newValue)) return Optional.ofNullable(newValue);
        ITrainingBot bot = plugin.getBotRegistry().getBot(ownerUUID);
        BotSnapshot snapshot = plugin.getBotEventDispatcher().snapshot(ownerUUID, bot);
        if (snapshot == null) return Optional.empty();
        BotSettingsChangeEvent event = plugin.getBotEventDispatcher()
                .publish(new BotSettingsChangeEvent(
                        plugin.getBotEventDispatcher().nextSequence(snapshot.requireBotUUID()),
                        snapshot,
                        source,
                        key,
                        oldValue,
                        newValue));
        if (event.isCancelled() || !valueType.isInstance(event.getNewValue())) return Optional.empty();
        return Optional.of(valueType.cast(event.getNewValue()));
    }
}
