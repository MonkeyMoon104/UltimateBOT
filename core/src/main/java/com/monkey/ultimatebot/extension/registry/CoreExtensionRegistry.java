package com.monkey.ultimatebot.extension.registry;

import com.monkey.ultimatebot.api.extension.ExtensionRegistration;
import com.monkey.ultimatebot.api.extension.UltimateBotExtensionRegistry;
import com.monkey.ultimatebot.api.extension.brain.BotBrainProvider;
import com.monkey.ultimatebot.api.extension.brain.BrainDescriptor;
import com.monkey.ultimatebot.api.extension.combat.CombatModeDescriptor;
import com.monkey.ultimatebot.api.extension.combat.CombatModeProvider;
import com.monkey.ultimatebot.common.model.BrainKey;
import com.monkey.ultimatebot.common.model.CombatMode;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.Nullable;

public final class CoreExtensionRegistry implements UltimateBotExtensionRegistry, AutoCloseable {
    private final ConcurrentMap<CombatMode, OwnedMode> modes = new ConcurrentHashMap<>();
    private final ConcurrentMap<BrainKey, OwnedBrain> brains = new ConcurrentHashMap<>();
    private final AtomicBoolean closed = new AtomicBoolean();

    @Override
    public ExtensionRegistration registerCombatMode(Plugin owner, CombatModeProvider provider) {
        Plugin checkedOwner = Objects.requireNonNull(owner, "owner");
        if (!checkedOwner.isEnabled()) {
            throw new IllegalStateException("owner plugin is not enabled: " + checkedOwner.getName());
        }
        return registerCombatMode(checkedOwner.getName(), provider);
    }

    public ExtensionRegistration registerCombatMode(String ownerId, CombatModeProvider provider) {
        ensureOpen();
        String checkedOwner = requireText(ownerId, "ownerId");
        CombatModeProvider checkedProvider = Objects.requireNonNull(provider, "provider");
        CombatModeDescriptor descriptor = Objects.requireNonNull(checkedProvider.descriptor(), "provider.descriptor");
        CombatMode mode = descriptor.mode();
        BrainKey linkedBrain = descriptor.brain();
        if (linkedBrain != null && !brains.containsKey(linkedBrain)) {
            throw new IllegalArgumentException(
                    "combat mode " + mode + " references an unregistered brain: " + linkedBrain);
        }
        if (mode.builtIn()) {
            throw new IllegalArgumentException("built-in combat modes cannot be replaced: " + mode);
        }
        OwnedMode registered = new OwnedMode(checkedOwner, checkedProvider, linkedBrain);
        OwnedMode previous = modes.putIfAbsent(mode, registered);
        if (previous != null) {
            throw new IllegalArgumentException(
                    "combat mode " + mode + " is already registered by " + previous.ownerId());
        }
        return new Registration(() -> modes.remove(mode, registered));
    }

    @Override
    public ExtensionRegistration registerBrain(Plugin owner, BotBrainProvider provider) {
        Plugin checkedOwner = Objects.requireNonNull(owner, "owner");
        if (!checkedOwner.isEnabled()) {
            throw new IllegalStateException("owner plugin is not enabled: " + checkedOwner.getName());
        }
        return registerBrain(checkedOwner.getName(), provider);
    }

    public ExtensionRegistration registerBrain(String ownerId, BotBrainProvider provider) {
        ensureOpen();
        String checkedOwner = requireText(ownerId, "ownerId");
        BotBrainProvider checkedProvider = Objects.requireNonNull(provider, "provider");
        BrainDescriptor descriptor = Objects.requireNonNull(checkedProvider.descriptor(), "provider.descriptor");
        BrainKey key = descriptor.key();
        OwnedBrain registered = new OwnedBrain(checkedOwner, checkedProvider);
        OwnedBrain previous = brains.putIfAbsent(key, registered);
        if (previous != null) {
            throw new IllegalArgumentException("brain " + key + " is already registered by " + previous.ownerId());
        }
        return new Registration(() -> {
            if (brains.remove(key, registered)) {
                modes.entrySet()
                        .removeIf(entry -> Objects.equals(entry.getValue().linkedBrain(), key));
            }
        });
    }

    @Override
    public Optional<CombatModeProvider> combatMode(CombatMode mode) {
        OwnedMode registered = modes.get(Objects.requireNonNull(mode, "mode"));
        return registered == null ? Optional.empty() : Optional.of(registered.provider());
    }

    @Override
    public Optional<BotBrainProvider> brain(BrainKey key) {
        OwnedBrain registered = brains.get(Objects.requireNonNull(key, "key"));
        return registered == null ? Optional.empty() : Optional.of(registered.provider());
    }

    @Override
    public List<CombatModeProvider> combatModes() {
        return modes.values().stream()
                .map(OwnedMode::provider)
                .sorted(Comparator.comparingInt(
                        provider -> provider.descriptor().order()))
                .toList();
    }

    @Override
    public List<BotBrainProvider> brains() {
        return brains.values().stream()
                .map(OwnedBrain::provider)
                .sorted(Comparator.comparing(provider -> provider.descriptor().key()))
                .toList();
    }

    public void unregisterOwner(String ownerId) {
        String checkedOwner = requireText(ownerId, "ownerId");
        Set<BrainKey> removedBrains = brains.entrySet().stream()
                .filter(entry -> entry.getValue().ownerId().equalsIgnoreCase(checkedOwner))
                .map(Map.Entry::getKey)
                .collect(Collectors.toUnmodifiableSet());
        modes.entrySet().removeIf(entry -> entry.getValue().ownerId().equalsIgnoreCase(checkedOwner));
        brains.entrySet().removeIf(entry -> entry.getValue().ownerId().equalsIgnoreCase(checkedOwner));
        if (!removedBrains.isEmpty()) {
            modes.entrySet()
                    .removeIf(entry -> removedBrains.contains(entry.getValue().linkedBrain()));
        }
    }

    @Override
    public void close() {
        if (!closed.compareAndSet(false, true)) {
            return;
        }
        modes.clear();
        brains.clear();
    }

    private void ensureOpen() {
        if (closed.get()) {
            throw new IllegalStateException("extension registry is closed");
        }
    }

    private static String requireText(String value, String name) {
        String checked = Objects.requireNonNull(value, name).trim();
        if (checked.isEmpty()) {
            throw new IllegalArgumentException(name + " cannot be blank");
        }
        return checked;
    }

    private record OwnedMode(
            String ownerId,
            CombatModeProvider provider,
            @Nullable BrainKey linkedBrain) {}

    private record OwnedBrain(String ownerId, BotBrainProvider provider) {}

    private static final class Registration implements ExtensionRegistration {
        private final AtomicBoolean closed = new AtomicBoolean();
        private final Runnable unregister;

        private Registration(Runnable unregister) {
            this.unregister = unregister;
        }

        @Override
        public void close() {
            if (closed.compareAndSet(false, true)) {
                unregister.run();
            }
        }
    }
}
