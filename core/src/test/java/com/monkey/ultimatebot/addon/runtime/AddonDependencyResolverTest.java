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
        var base = discovered("base", List.of(), List.of());
        var optional = discovered("optional", List.of(), List.of());
        var feature = discovered("feature", List.of("base"), List.of("optional"));
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
        var first = discovered("first", List.of("second"), List.of());
        var second = discovered("second", List.of("first"), List.of());

        assertThatThrownBy(() -> AddonDependencyResolver.resolve(Map.of("first", first, "second", second)))
                .isInstanceOf(AddonLoadException.class)
                .hasMessageContaining("cyclic");
        assertThat(AddonDependencyResolver.cyclicAddons(Map.of("first", first, "second", second)))
                .containsExactlyInAnyOrder("first", "second");
    }

    private static AddonDependencyResolver.DiscoveredAddon discovered(
            String id, List<String> dependencies, List<String> softDependencies) {
        AddonDescriptor descriptor = new AddonDescriptor(
                id,
                id,
                "1.0.0",
                UltimateBotAddonEngine.API_VERSION,
                "example.Addon",
                List.of("Developer"),
                dependencies,
                softDependencies,
                Map.of());
        return new AddonDependencyResolver.DiscoveredAddon(descriptor, Path.of(id + ".jar"));
    }
}
