package com.monkey.ultimatebot.event;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.api.event.base.BotEventSource;
import com.monkey.ultimatebot.api.event.state.BotSettingKey;
import com.monkey.ultimatebot.api.event.state.BotSettingsChangeEvent;
import com.monkey.ultimatebot.api.model.runtime.BotSnapshot;
import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
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

    public static Optional<Set<UUID>> proposeUuidSet(
            UltimateBot plugin,
            UUID ownerUUID,
            BotEventSource source,
            BotSettingKey key,
            Set<UUID> oldValue,
            Set<UUID> newValue) {
        if (Objects.equals(oldValue, newValue)) {
            return Optional.of(Set.copyOf(newValue));
        }
        ITrainingBot bot = plugin.getBotRegistry().getBot(ownerUUID);
        BotSnapshot snapshot = plugin.getBotEventDispatcher().snapshot(ownerUUID, bot);
        if (snapshot == null) {
            return Optional.empty();
        }
        BotSettingsChangeEvent event = plugin.getBotEventDispatcher()
                .publish(new BotSettingsChangeEvent(
                        plugin.getBotEventDispatcher().nextSequence(snapshot.requireBotUUID()),
                        snapshot,
                        source,
                        key,
                        Set.copyOf(oldValue),
                        Set.copyOf(newValue)));
        if (event.isCancelled() || !(event.getNewValue() instanceof Set<?> proposedValues)) {
            return Optional.empty();
        }
        LinkedHashSet<UUID> validatedValues = new LinkedHashSet<>();
        for (Object proposedValue : proposedValues) {
            if (!(proposedValue instanceof UUID uuid)) {
                return Optional.empty();
            }
            validatedValues.add(uuid);
        }
        return Optional.of(Set.copyOf(validatedValues));
    }
}
