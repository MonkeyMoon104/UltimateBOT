package com.monkey.ultimatebot.api.addon;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Validated metadata read from {@code ultimatebot-addon.properties}. */
public record AddonDescriptor(
        String id,
        String name,
        String version,
        String apiVersion,
        String mainClass,
        List<String> authors,
        List<String> dependencies,
        List<String> softDependencies,
        Map<String, String> nativeProviders) {
    public AddonDescriptor {
        id = requireText(id, "id");
        name = requireText(name, "name");
        version = requireText(version, "version");
        apiVersion = requireText(apiVersion, "apiVersion");
        mainClass = requireText(mainClass, "mainClass");
        authors = List.copyOf(Objects.requireNonNull(authors, "authors"));
        dependencies = List.copyOf(Objects.requireNonNull(dependencies, "dependencies"));
        softDependencies = List.copyOf(Objects.requireNonNull(softDependencies, "softDependencies"));
        nativeProviders = Map.copyOf(Objects.requireNonNull(nativeProviders, "nativeProviders"));
    }

    private static String requireText(String value, String name) {
        String checked = Objects.requireNonNull(value, name).trim();
        if (checked.isEmpty()) {
            throw new IllegalArgumentException(name + " cannot be blank");
        }
        return checked;
    }
}
