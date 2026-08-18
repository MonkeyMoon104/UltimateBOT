package com.monkey.ultimatebot.api;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.monkey.ultimatebot.api.managers.IBotManager;
import com.monkey.ultimatebot.api.managers.IBotRegistry;
import com.monkey.ultimatebot.api.model.runtime.BotSnapshot;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class ApiSurfaceParityTest {

    @Test
    void managerDoesNotHideUnsupportedOperationsBehindDefaults() {
        assertFalse(Arrays.stream(IBotManager.class.getDeclaredMethods()).anyMatch(Method::isDefault));
    }

    @Test
    void everyOwnerUpdateHasABotUuidCounterpart() {
        Set<String> methods = Arrays.stream(IBotManager.class.getDeclaredMethods())
                .map(Method::getName)
                .collect(Collectors.toUnmodifiableSet());

        methods.stream()
                .filter(name -> name.startsWith("update"))
                .filter(name -> !name.endsWith("ByBotUUID"))
                .forEach(name -> assertTrue(methods.contains(name + "ByBotUUID"), name + " lacks bot UUID parity"));
        assertTrue(methods.contains("removeByBotUUID"));
    }

    @Test
    void registryAndSnapshotExposeCompleteIdentityAndConfigurationState() {
        Set<String> registryMethods = Arrays.stream(IBotRegistry.class.getDeclaredMethods())
                .map(Method::getName)
                .collect(Collectors.toUnmodifiableSet());
        assertTrue(registryMethods.containsAll(
                Set.of("getBot", "getBotByBotUUID", "getBotUUID", "getOwnerUUID", "getAllBotsByBotUUID")));

        Set<String> managerMethods = Arrays.stream(IBotManager.class.getDeclaredMethods())
                .map(Method::getName)
                .collect(Collectors.toUnmodifiableSet());
        assertTrue(managerMethods.containsAll(Set.of("getPlatform", "supports", "getCombatModes", "getBrains")));

        Set<String> snapshotMethods = Arrays.stream(BotSnapshot.class.getDeclaredMethods())
                .map(Method::getName)
                .collect(Collectors.toUnmodifiableSet());
        assertTrue(snapshotMethods.containsAll(Set.of(
                "botMode",
                "botUUID",
                "combatMode",
                "brain",
                "combatTuning",
                "armor",
                "blastProtection",
                "equipmentSlots",
                "targetUUIDs",
                "teamOwnerUUIDs",
                "spawnLocation",
                "killMessage")));
    }
}
