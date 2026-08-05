package com.monkey.ultimatebot.api.addon;

import java.nio.file.Path;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/** Immutable status of one installed addon. */
public record AddonSnapshot(
        AddonDescriptor descriptor,
        AddonState state,
        Path jar,
        @Nullable String failure) {
    public AddonSnapshot {
        Objects.requireNonNull(descriptor, "descriptor");
        Objects.requireNonNull(state, "state");
        Objects.requireNonNull(jar, "jar");
    }
}
