package com.monkey.ultimatebot.addon.runtime;


import java.util.Collections;
import com.monkey.ultimatebot.api.addon.AddonDescriptor;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

final class AddonDependencyResolver {
    private AddonDependencyResolver() {}

    static List<DiscoveredAddon> resolve(Map<String, DiscoveredAddon> addons) throws AddonLoadException {
        List<DiscoveredAddon> ordered = new ArrayList<>();
        Set<String> visiting = new HashSet<>();
        Set<String> visited = new HashSet<>();
        Map<String, String> missing = new HashMap<>();
        for (DiscoveredAddon addon : addons.values()) {
            visit(addon, addons, visiting, visited, ordered, missing);
        }
        if (!missing.isEmpty()) {
            Map.Entry<String, String> first = missing.entrySet().iterator().next();
            throw new AddonLoadException("addon " + first.getKey() + " requires missing addon " + first.getValue());
        }
        return com.monkey.ultimatebot.common.util.ImmutableCollections.copyOf(ordered);
    }

    static Set<String> cyclicAddons(Map<String, DiscoveredAddon> addons) {
        Map<String, VisitState> states = new HashMap<>();
        Deque<String> path = new ArrayDeque<>();
        Set<String> cyclic = new HashSet<>();
        for (String addonId : addons.keySet()) {
            detectCycles(addonId, addons, states, path, cyclic);
        }
        return com.monkey.ultimatebot.common.util.ImmutableCollections.copyOf(cyclic);
    }

    private static void detectCycles(
            String addonId,
            Map<String, DiscoveredAddon> addons,
            Map<String, VisitState> states,
            Deque<String> path,
            Set<String> cyclic) {
        VisitState state = states.get(addonId);
        if (state == VisitState.VISITED) {
            return;
        }
        if (state == VisitState.VISITING) {
            boolean insideCycle = false;
            for (String pathId : path) {
                if (pathId.equals(addonId)) {
                    insideCycle = true;
                }
                if (insideCycle) {
                    cyclic.add(pathId);
                }
            }
            return;
        }
        DiscoveredAddon addon = addons.get(addonId);
        if (addon == null) {
            return;
        }
        states.put(addonId, VisitState.VISITING);
        path.addLast(addonId);
        Stream.concat(addon.descriptor().dependencies().stream(), addon.descriptor().softDependencies().stream())
                .filter(addons::containsKey)
                .forEach(dependency -> detectCycles(dependency, addons, states, path, cyclic));
        path.removeLast();
        states.put(addonId, VisitState.VISITED);
    }

    private static void visit(
            DiscoveredAddon addon,
            Map<String, DiscoveredAddon> addons,
            Set<String> visiting,
            Set<String> visited,
            List<DiscoveredAddon> ordered,
            Map<String, String> missing)
            throws AddonLoadException {
        String id = addon.descriptor().id();
        if (visited.contains(id)) {
            return;
        }
        if (!visiting.add(id)) {
            throw new AddonLoadException("cyclic addon dependency involving " + id);
        }
        for (String dependencyId : addon.descriptor().dependencies()) {
            DiscoveredAddon dependency = addons.get(dependencyId);
            if (dependency == null) {
                missing.putIfAbsent(id, dependencyId);
                continue;
            }
            visit(dependency, addons, visiting, visited, ordered, missing);
        }
        for (String dependencyId : addon.descriptor().softDependencies()) {
            DiscoveredAddon dependency = addons.get(dependencyId);
            if (dependency != null) {
                visit(dependency, addons, visiting, visited, ordered, missing);
            }
        }
        visiting.remove(id);
        visited.add(id);
        ordered.add(addon);
    }

    static final class DiscoveredAddon {
        private final AddonDescriptor descriptor;
        private final Path jar;

        DiscoveredAddon(AddonDescriptor descriptor, Path jar) {


            Objects.requireNonNull(descriptor, "descriptor");
            Objects.requireNonNull(jar, "jar");
            this.descriptor = descriptor;
            this.jar = jar;
        }

        public AddonDescriptor descriptor() {
            return descriptor;
        }
        public Path jar() {
            return jar;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof DiscoveredAddon)) {
                return false;
            }
            DiscoveredAddon other = (DiscoveredAddon) obj;
            return java.util.Objects.equals(descriptor, other.descriptor) && java.util.Objects.equals(jar, other.jar);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(descriptor, jar);
        }

        @Override
        public String toString() {
            return "DiscoveredAddon[descriptor=" + descriptor + ", jar=" + jar + "]";
        }
    }

    private enum VisitState {
        VISITING,
        VISITED
    }
}
