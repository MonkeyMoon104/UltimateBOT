package com.monkey.ultimatebot.extension.registry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.monkey.ultimatebot.api.extension.ExtensionRegistration;
import com.monkey.ultimatebot.api.extension.brain.BotBrainProvider;
import com.monkey.ultimatebot.api.extension.brain.BrainDescriptor;
import com.monkey.ultimatebot.api.extension.combat.CombatModeDescriptor;
import com.monkey.ultimatebot.api.extension.combat.CombatModeProvider;
import com.monkey.ultimatebot.api.extension.combat.ModeKit;
import com.monkey.ultimatebot.common.model.BrainCapability;
import com.monkey.ultimatebot.common.model.BrainKey;
import com.monkey.ultimatebot.common.model.CombatMode;
import com.monkey.ultimatebot.common.model.CombatTuning;
import com.monkey.ultimatebot.common.model.DifficultyTier;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

class CoreExtensionRegistryTest {
    @Test
    void registrationHandleRemovesExactlyItsProvider() {
        CoreExtensionRegistry registry = new CoreExtensionRegistry();
        Plugin plugin = enabledPlugin("ExampleAddon");
        CombatModeProvider provider = provider(CombatMode.of("example", "duels"));

        ExtensionRegistration registration = registry.registerCombatMode(plugin, provider);
        assertThat(registry.combatMode(provider.descriptor().mode())).contains(provider);

        registration.close();
        registration.close();
        assertThat(registry.combatMode(provider.descriptor().mode())).isEmpty();
    }

    @Test
    void rejectsBuiltInReplacementAndDuplicateKeys() {
        CoreExtensionRegistry registry = new CoreExtensionRegistry();
        Plugin plugin = enabledPlugin("ExampleAddon");
        CombatMode custom = CombatMode.of("example", "duels");
        registry.registerCombatMode(plugin, provider(custom));

        assertThatThrownBy(() -> registry.registerCombatMode(plugin, provider(custom)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already registered");
        assertThatThrownBy(() -> registry.registerCombatMode(plugin, provider(CombatMode.SWORD)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("built-in");
    }

    @Test
    void linkedBrainRegistrationOwnsTheDependentModeLifecycle() {
        CoreExtensionRegistry registry = new CoreExtensionRegistry();
        Plugin plugin = enabledPlugin("ExampleAddon");
        BrainKey brainKey = BrainKey.of("example", "brain");
        CombatMode mode = CombatMode.of("example", "duels");

        assertThatThrownBy(() -> registry.registerCombatMode(plugin, provider(mode, brainKey)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("unregistered brain");

        ExtensionRegistration brain = registry.registerBrain(plugin, brainProvider(brainKey));
        registry.registerCombatMode(plugin, provider(mode, brainKey));
        assertThat(registry.combatMode(mode)).isPresent();

        brain.close();
        assertThat(registry.brain(brainKey)).isEmpty();
        assertThat(registry.combatMode(mode)).isEmpty();
    }

    private static Plugin enabledPlugin(String name) {
        Plugin plugin = mock(Plugin.class);
        when(plugin.isEnabled()).thenReturn(true);
        when(plugin.getName()).thenReturn(name);
        return plugin;
    }

    private static CombatModeProvider provider(CombatMode mode) {
        return provider(mode, null);
    }

    private static CombatModeProvider provider(CombatMode mode, @Nullable BrainKey brain) {
        EnumMap<DifficultyTier, CombatTuning> profiles = new EnumMap<>(DifficultyTier.class);
        for (DifficultyTier difficulty : DifficultyTier.values()) {
            profiles.put(difficulty, CombatTuning.builder().build());
        }
        CombatModeDescriptor descriptor = new CombatModeDescriptor(
                mode,
                "Custom Duels",
                List.of("Test mode"),
                Material.IRON_SWORD,
                "",
                100,
                Set.of(),
                Map.copyOf(profiles),
                ModeKit.empty(),
                brain);
        return new CombatModeProvider() {
            @Override
            public CombatModeDescriptor descriptor() {
                return descriptor;
            }

            @Override
            public com.monkey.ultimatebot.api.extension.combat.CombatModeSession create(
                    com.monkey.ultimatebot.api.extension.combat.CombatModeRuntime runtime) {
                throw new UnsupportedOperationException("not used by this registry test");
            }
        };
    }

    private static BotBrainProvider brainProvider(BrainKey key) {
        BrainDescriptor descriptor =
                new BrainDescriptor(key, "Example Brain", "Test brain", Set.of(BrainCapability.FULL_CONTROL), false);
        return new BotBrainProvider() {
            @Override
            public BrainDescriptor descriptor() {
                return descriptor;
            }

            @Override
            public com.monkey.ultimatebot.api.extension.brain.BotBrainSession create(
                    com.monkey.ultimatebot.api.extension.brain.BotBrainContext context) {
                throw new UnsupportedOperationException("not used by this registry test");
            }
        };
    }
}
