package com.monkey.ultimatebot.common.model;

import java.util.List;
import java.util.Objects;
import com.monkey.ultimatebot.common.util.ImmutableCollections;
import org.jspecify.annotations.Nullable;

/** Platform-neutral installed-addon status exposed by the remote API and SDK. */
public final class AddonInfo {
    private final String id;
    private final String name;
    private final String version;
    private final String state;
    private final List<String> authors;
    private final List<String> dependencies;
    private final @Nullable String failure;

    public AddonInfo(
            String id,
            String name,
            String version,
            String state,
            List<String> authors,
            List<String> dependencies,
            @Nullable String failure) {
        this.id = requireText(id, "id");
        this.name = requireText(name, "name");
        this.version = requireText(version, "version");
        this.state = requireText(state, "state");
        this.authors = ImmutableCollections.copyOf(Objects.requireNonNull(authors, "authors"));
        this.dependencies = ImmutableCollections.copyOf(Objects.requireNonNull(dependencies, "dependencies"));
        this.failure = failure;
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

    public String state() {
        return state;
    }

    public List<String> authors() {
        return authors;
    }

    public List<String> dependencies() {
        return dependencies;
    }

    public @Nullable String failure() {
        return failure;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof AddonInfo)) {
            return false;
        }
        AddonInfo other = (AddonInfo) obj;
        return id.equals(other.id)
                && name.equals(other.name)
                && version.equals(other.version)
                && state.equals(other.state)
                && authors.equals(other.authors)
                && dependencies.equals(other.dependencies)
                && Objects.equals(failure, other.failure);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, version, state, authors, dependencies, failure);
    }

    @Override
    public String toString() {
        return "AddonInfo[id="
                + id
                + ", name="
                + name
                + ", version="
                + version
                + ", state="
                + state
                + ", authors="
                + authors
                + ", dependencies="
                + dependencies
                + ", failure="
                + failure
                + ']';
    }

    private static String requireText(String value, String name) {
        String checked = Objects.requireNonNull(value, name).trim();
        if (checked.isEmpty()) {
            throw new IllegalArgumentException(name + " cannot be blank");
        }
        return checked;
    }
}
