package com.monkey.ultimatebot.api.addon;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Validated metadata read from {@code ultimatebot-addon.properties}. */
public final class AddonDescriptor {
    private final String id;
    private final String name;
    private final String version;
    private final String apiVersion;
    private final String mainClass;
    private final List<String> authors;
    private final List<String> dependencies;
    private final List<String> softDependencies;
    private final Map<String, String> nativeProviders;

    public AddonDescriptor(
            String id,
            String name,
            String version,
            String apiVersion,
            String mainClass,
            List<String> authors,
            List<String> dependencies,
            List<String> softDependencies,
            Map<String, String> nativeProviders) {

        id = requireText(id, "id");
        name = requireText(name, "name");
        version = requireText(version, "version");
        apiVersion = requireText(apiVersion, "apiVersion");
        mainClass = requireText(mainClass, "mainClass");
        authors = com.monkey.ultimatebot.common.util.ImmutableCollections.copyOf(
                Objects.requireNonNull(authors, "authors"));
        dependencies = com.monkey.ultimatebot.common.util.ImmutableCollections.copyOf(
                Objects.requireNonNull(dependencies, "dependencies"));
        softDependencies = com.monkey.ultimatebot.common.util.ImmutableCollections.copyOf(
                Objects.requireNonNull(softDependencies, "softDependencies"));
        nativeProviders = com.monkey.ultimatebot.common.util.ImmutableCollections.copyOf(
                Objects.requireNonNull(nativeProviders, "nativeProviders"));
        this.id = id;
        this.name = name;
        this.version = version;
        this.apiVersion = apiVersion;
        this.mainClass = mainClass;
        this.authors = authors;
        this.dependencies = dependencies;
        this.softDependencies = softDependencies;
        this.nativeProviders = nativeProviders;
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String version() {
        return version;
    }

    public String apiVersion() {
        return apiVersion;
    }

    public String mainClass() {
        return mainClass;
    }

    public List<String> authors() {
        return authors;
    }

    public List<String> dependencies() {
        return dependencies;
    }

    public List<String> softDependencies() {
        return softDependencies;
    }

    public Map<String, String> nativeProviders() {
        return nativeProviders;
    }

    private static String requireText(String value, String name) {
        String checked = Objects.requireNonNull(value, name).trim();
        if (checked.isEmpty()) {
            throw new IllegalArgumentException(name + " cannot be blank");
        }
        return checked;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof AddonDescriptor)) {
            return false;
        }
        AddonDescriptor other = (AddonDescriptor) obj;
        return java.util.Objects.equals(id, other.id)
                && java.util.Objects.equals(name, other.name)
                && java.util.Objects.equals(version, other.version)
                && java.util.Objects.equals(apiVersion, other.apiVersion)
                && java.util.Objects.equals(mainClass, other.mainClass)
                && java.util.Objects.equals(authors, other.authors)
                && java.util.Objects.equals(dependencies, other.dependencies)
                && java.util.Objects.equals(softDependencies, other.softDependencies)
                && java.util.Objects.equals(nativeProviders, other.nativeProviders);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(
                id, name, version, apiVersion, mainClass, authors, dependencies, softDependencies, nativeProviders);
    }

    @Override
    public String toString() {
        return "AddonDescriptor[id=" + id + ", name=" + name + ", version=" + version + ", apiVersion=" + apiVersion
                + ", mainClass=" + mainClass + ", authors=" + authors + ", dependencies=" + dependencies
                + ", softDependencies=" + softDependencies + ", nativeProviders=" + nativeProviders + "]";
    }
}
