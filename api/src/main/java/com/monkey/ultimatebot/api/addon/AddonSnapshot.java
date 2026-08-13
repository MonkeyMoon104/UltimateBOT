package com.monkey.ultimatebot.api.addon;

import java.nio.file.Path;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/** Immutable status of one installed addon. */
public final class AddonSnapshot {
    private final AddonDescriptor descriptor;
    private final AddonState state;
    private final Path jar;
    private final @Nullable String failure;

    public AddonSnapshot(AddonDescriptor descriptor, AddonState state, Path jar, @Nullable String failure) {


        Objects.requireNonNull(descriptor, "descriptor");
        Objects.requireNonNull(state, "state");
        Objects.requireNonNull(jar, "jar");
        this.descriptor = descriptor;
        this.state = state;
        this.jar = jar;
        this.failure = failure;
    }

    public AddonDescriptor descriptor() {
        return descriptor;
    }
    public AddonState state() {
        return state;
    }
    public Path jar() {
        return jar;
    }
    public @Nullable String failure() {
        return failure;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof AddonSnapshot)) {
            return false;
        }
        AddonSnapshot other = (AddonSnapshot) obj;
        return java.util.Objects.equals(descriptor, other.descriptor) && java.util.Objects.equals(state, other.state) && java.util.Objects.equals(jar, other.jar) && java.util.Objects.equals(failure, other.failure);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(descriptor, state, jar, failure);
    }

    @Override
    public String toString() {
        return "AddonSnapshot[descriptor=" + descriptor + ", state=" + state + ", jar=" + jar + ", failure=" + failure + "]";
    }
}
