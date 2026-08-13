package com.monkey.ultimatebot.addon.runtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.monkey.ultimatebot.api.addon.AddonDescriptor;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AddonDependencyResolverTest {
    @Test
    void ordersRequiredAndAvailableSoftDependenciesBeforeConsumers() throws Exception {
        AddonDependencyResolver.DiscoveredAddon base =
                discovered("base", java.util.Collections.emptyList(), java.util.Collections.emptyList());
        AddonDependencyResolver.DiscoveredAddon optional =
                discovered("optional", java.util.Collections.emptyList(), java.util.Collections.emptyList());
        AddonDependencyResolver.DiscoveredAddon feature = discovered(
                "feature",
                java.util.Collections.singletonList("base"),
                java.util.Collections.singletonList("optional"));
        Map<String, AddonDependencyResolver.DiscoveredAddon> addons = new LinkedHashMap<>();
        addons.put(feature.descriptor().id(), feature);
        addons.put(optional.descriptor().id(), optional);
        addons.put(base.descriptor().id(), base);

        assertThat(AddonDependencyResolver.resolve(addons))
                .extracting(candidate -> candidate.descriptor().id())
                .containsExactly("base", "optional", "feature");
    }

    @Test
    void rejectsDependencyCycles() {
        AddonDependencyResolver.DiscoveredAddon first =
                discovered("first", java.util.Collections.singletonList("second"), java.util.Collections.emptyList());
        AddonDependencyResolver.DiscoveredAddon second =
                discovered("second", java.util.Collections.singletonList("first"), java.util.Collections.emptyList());
        Map<String, AddonDependencyResolver.DiscoveredAddon> cycle = new LinkedHashMap<>();
        cycle.put("first", first);
        cycle.put("second", second);

        assertThatThrownBy(() -> AddonDependencyResolver.resolve(cycle))
                .isInstanceOf(AddonLoadException.class)
                .hasMessageContaining("cyclic");
        assertThat(AddonDependencyResolver.cyclicAddons(cycle)).containsExactlyInAnyOrder("first", "second");
    }

    private static AddonDependencyResolver.DiscoveredAddon discovered(
            String id, List<String> dependencies, List<String> softDependencies) {
        AddonDescriptor descriptor = new AddonDescriptor(
                id,
                id,
                "1.0.0",
                UltimateBotAddonEngine.API_VERSION,
                "example.Addon",
                java.util.Collections.singletonList("Developer"),
                dependencies,
                softDependencies,
                java.util.Collections.emptyMap());
        return new AddonDependencyResolver.DiscoveredAddon(descriptor, java.nio.file.Paths.get(id + ".jar"));
    }
}
